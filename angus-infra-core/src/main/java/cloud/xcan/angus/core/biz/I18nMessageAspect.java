package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.spec.locale.SdfLocaleHolder.getLocale;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isBlank;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.remote.MessageJoinField;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Optional AOP assembly: after a {@link MessageJoin} method returns, fill VO fields annotated with
 * {@link MessageJoinField} via {@link I18nMessageResolver}.
 *
 * <p>Supports nested heterogeneous VOs and same-type trees (e.g. menu {@code children}). Missing
 * {@code @MessageJoinField} is a no-op (does not throw).</p>
 *
 * @author XiaoLong Liu
 */
@Slf4j
@Aspect
public class I18nMessageAspect extends AbstractJoinAspect {

  /**
   * Vo.class → fieldName → MessageJoinField
   */
  private final Map<Class<?>, Map<String, MessageJoinField>> cacheFieldNameMap =
      new ConcurrentHashMap<>();

  private final I18nMessageResolver messageResolver;
  private final I18nMessageProperties properties;

  public I18nMessageAspect(I18nMessageResolver messageResolver, I18nMessageProperties properties) {
    this.messageResolver = messageResolver;
    this.properties = properties;
  }

  @Pointcut("@annotation(cloud.xcan.angus.core.biz.MessageJoin)")
  public void voJoinPointCut() {
  }

  @Around("voJoinPointCut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    return aspect(joinPoint);
  }

  /**
   * Clear field-meta cache and message cache.
   */
  public void clearCache() {
    log.debug("Clear MessageJoin field meta and i18n message cache");
    this.cacheFieldNameMap.clear();
    if (messageResolver != null) {
      messageResolver.evictAll();
    }
  }

  /**
   * Diagnostics: current resolver cache snapshot.
   */
  public Map<String, Map<String, Map<String, I18nMessage>>> getMessagesMap() {
    if (messageResolver == null) {
      return Map.of();
    }
    return messageResolver.cacheSnapshot();
  }

  @Override
  public void joinArrayVoName(Object[] voArray) throws IllegalAccessException {
    if (messageResolver == null || properties == null || !properties.isEnabled()) {
      return;
    }
    if (isEmpty(voArray) || voArray[0] == null) {
      return;
    }

    Locale locale = getLocale();
    if (properties.isSkipDefaultLocale() && isDefaultLocale(locale)) {
      log.debug("Skip MessageJoin for default locale {}", properties.getDefaultLocale());
      return;
    }

    // Group root + nested VO instances by concrete class (tree + heterogeneous nesting)
    Map<Class<?>, List<Object>> vosByType = collectVosByType(voArray);
    if (isEmpty(vosByType)) {
      return;
    }

    for (Entry<Class<?>, List<Object>> typeEntry : vosByType.entrySet()) {
      Class<?> voClass = typeEntry.getKey();
      List<Object> instances = typeEntry.getValue();
      if (isEmpty(instances)) {
        continue;
      }
      Map<String, MessageJoinField> joinFields = findAndCacheJoinInfo(voClass);
      if (isEmpty(joinFields)) {
        continue;
      }
      Object[] flatVos = instances.toArray();
      for (Entry<String, MessageJoinField> entry : joinFields.entrySet()) {
        fillField(flatVos, voClass, entry.getKey(), entry.getValue(), locale);
      }
    }
  }

  /**
   * Collect nestable bean instances under roots, grouped by runtime class.
   *
   * <p>Walks object fields and collection/array elements; skips JDK types. Uses identity visit
   * tracking to avoid cycles.</p>
   */
  private Map<Class<?>, List<Object>> collectVosByType(Object[] roots)
      throws IllegalAccessException {
    Map<Class<?>, List<Object>> byType = new LinkedHashMap<>();
    Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Object root : roots) {
      collectNested(root, byType, visited);
    }
    return byType;
  }

  private void collectNested(Object node, Map<Class<?>, List<Object>> byType, Set<Object> visited)
      throws IllegalAccessException {
    if (node == null || !visited.add(node)) {
      return;
    }

    if (node instanceof Collection<?> collection) {
      for (Object element : collection) {
        collectNested(element, byType, visited);
      }
      return;
    }

    Class<?> clazz = node.getClass();
    if (clazz.isArray()) {
      if (!clazz.getComponentType().isPrimitive()) {
        int length = Array.getLength(node);
        for (int i = 0; i < length; i++) {
          collectNested(Array.get(node, i), byType, visited);
        }
      }
      return;
    }

    if (!isNestableBean(clazz)) {
      return;
    }

    byType.computeIfAbsent(clazz, key -> new ArrayList<>()).add(node);

    for (Field field : FieldUtils.getAllFields(clazz)) {
      if (shouldSkipField(field)) {
        continue;
      }
      field.setAccessible(true);
      collectNested(field.get(node), byType, visited);
    }
  }

  private static boolean shouldSkipField(Field field) {
    int modifiers = field.getModifiers();
    return Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers);
  }

  /**
   * Candidate VO / DTO beans: non-JDK, non-enum, non-primitive wrappers.
   */
  private static boolean isNestableBean(Class<?> clazz) {
    if (clazz.isPrimitive() || clazz.isEnum() || clazz.isInterface() || clazz.isAnnotation()) {
      return false;
    }
    String name = clazz.getName();
    return !name.startsWith("java.")
        && !name.startsWith("javax.")
        && !name.startsWith("jakarta.")
        && !name.startsWith("kotlin.")
        && !name.startsWith("sun.")
        && !name.startsWith("com.sun.");
  }

  private void fillField(Object[] voArray, Class<?> voClass, String fieldName,
      MessageJoinField joinMeta, Locale locale) throws IllegalAccessException {
    String type = joinMeta.type();
    Field targetField = FieldUtils.getField(voClass, fieldName, true);
    Field keyField = resolveKeyField(voClass, fieldName, joinMeta);

    Set<String> keys = new HashSet<>();
    for (Object vo : voArray) {
      if (vo == null) {
        continue;
      }
      Object keyValue = keyField.get(vo);
      if (keyValue != null && isNotEmpty(keyValue.toString())) {
        keys.add(keyValue.toString());
      }
    }
    if (isEmpty(keys)) {
      return;
    }

    Map<String, String> resolved = messageResolver.resolveBatch(type, keys, locale,
        joinMeta.enabledCache());

    for (Object vo : voArray) {
      if (vo == null) {
        continue;
      }
      Object keyValue = keyField.get(vo);
      if (keyValue == null) {
        continue;
      }
      String text = resolved.get(keyValue.toString());
      if (isNotEmpty(text)) {
        targetField.set(vo, text);
        syncPermissionMenuName(vo, voClass, fieldName, text);
      }
    }
  }

  /**
   * Keep nested {@code permission.menuName} in sync when menu {@code name} is translated.
   */
  private static void syncPermissionMenuName(Object vo, Class<?> voClass, String fieldName,
      String text) throws IllegalAccessException {
    if (!"name".equals(fieldName)) {
      return;
    }
    Field permissionField = FieldUtils.getField(voClass, "permission", true);
    if (permissionField == null) {
      return;
    }
    Object permission = permissionField.get(vo);
    if (permission == null) {
      return;
    }
    Field menuNameField = FieldUtils.getField(permission.getClass(), "menuName", true);
    if (menuNameField != null) {
      menuNameField.set(permission, text);
    }
  }

  private static Field resolveKeyField(Class<?> voClass, String fieldName,
      MessageJoinField joinMeta) {
    String keyFieldName = joinMeta.keyField();
    if (isBlank(keyFieldName)) {
      return FieldUtils.getField(voClass, fieldName, true);
    }
    Field keyField = FieldUtils.getField(voClass, keyFieldName, true);
    if (keyField == null) {
      throw new IllegalArgumentException("MessageJoinField keyField '" + keyFieldName
          + "' not found on " + voClass.getName());
    }
    return keyField;
  }

  private Map<String, MessageJoinField> findAndCacheJoinInfo(Class<?> clazz) {
    Map<String, MessageJoinField> cached = cacheFieldNameMap.get(clazz);
    if (cached != null) {
      return cached;
    }

    Map<String, MessageJoinField> messageJoinFieldMap = new HashMap<>();
    Field[] fields = FieldUtils.getAllFields(clazz);
    for (Field field : fields) {
      MessageJoinField messageJoin = field.getAnnotation(MessageJoinField.class);
      if (nonNull(messageJoin)) {
        if (isBlank(messageJoin.type())) {
          throw new IllegalArgumentException("The MessageJoinField property type of "
              + field.getName() + " is empty");
        }
        messageJoinFieldMap.put(field.getName(), messageJoin);
      }
    }
    // Cache empty map too — avoid re-scanning and avoid throwing
    cacheFieldNameMap.put(clazz, messageJoinFieldMap);
    return messageJoinFieldMap;
  }

  private boolean isDefaultLocale(Locale locale) {
    String configured = properties.getDefaultLocale();
    if (isBlank(configured) || locale == null) {
      return false;
    }
    String tag = DefaultI18nMessageResolver.toLanguageTag(locale);
    if (configured.equals(tag) || configured.equals(locale.toString())) {
      return true;
    }
    // Match SupportedLanguage only when the locale actually maps to a known value
    // (avoid treating unknown tags as the configured default).
    if (SupportedLanguage.contain(tag)) {
      return configured.equals(tag);
    }
    if (SupportedLanguage.contain(locale.toString())) {
      return configured.equals(locale.toString());
    }
    String language = locale.getLanguage();
    return SupportedLanguage.contain(language) && configured.equals(language);
  }
}
