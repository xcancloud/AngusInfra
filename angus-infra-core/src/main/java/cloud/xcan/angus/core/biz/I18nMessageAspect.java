package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.spec.locale.SdfLocaleHolder.getLocale;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isBlank;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.remote.MessageJoinField;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
 * <p>Missing {@code @MessageJoinField} is a no-op (does not throw).</p>
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

    // Flatten nested same-type trees (e.g. menu.children) so child nodes are also filled
    Object[] flatVos = flattenSameTypeTree(voArray);
    Map<String, MessageJoinField> joinFields = findAndCacheJoinInfo(flatVos[0]);
    if (isEmpty(joinFields)) {
      // No annotated fields — no-op (safe for mistakenly annotated methods)
      return;
    }

    Class<?> firstClass = flatVos[0].getClass();
    for (Entry<String, MessageJoinField> entry : joinFields.entrySet()) {
      String fieldName = entry.getKey();
      MessageJoinField joinMeta = entry.getValue();
      fillField(flatVos, firstClass, fieldName, joinMeta, locale);
    }
  }

  /**
   * Collect root VOs and nested collection elements of the same runtime class (tree nodes).
   */
  private static Object[] flattenSameTypeTree(Object[] roots) throws IllegalAccessException {
    Class<?> type = roots[0].getClass();
    List<Object> flat = new ArrayList<>();
    Set<Object> visited = new HashSet<>();
    for (Object root : roots) {
      collectSameTypeNodes(root, type, flat, visited);
    }
    return flat.toArray();
  }

  private static void collectSameTypeNodes(Object node, Class<?> type, List<Object> flat,
      Set<Object> visited) throws IllegalAccessException {
    if (node == null || !type.isAssignableFrom(node.getClass()) || !visited.add(node)) {
      return;
    }
    flat.add(node);
    for (Field field : FieldUtils.getAllFields(node.getClass())) {
      if (!Collection.class.isAssignableFrom(field.getType())) {
        continue;
      }
      field.setAccessible(true);
      Collection<?> children = (Collection<?>) field.get(node);
      if (isEmpty(children)) {
        continue;
      }
      for (Object child : children) {
        if (child != null && type.isAssignableFrom(child.getClass())) {
          collectSameTypeNodes(child, type, flat, visited);
        }
      }
    }
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

  private Map<String, MessageJoinField> findAndCacheJoinInfo(Object first) {
    Class<?> clazz = first.getClass();
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
    if (isBlank(configured)) {
      return false;
    }
    String tag = DefaultI18nMessageResolver.toLanguageTag(locale);
    if (configured.equals(tag)) {
      return true;
    }
    // Also accept SupportedLanguage enum equality
    try {
      SupportedLanguage current = SupportedLanguage.safeLanguage(locale);
      return configured.equals(current.getValue());
    } catch (Exception ignore) {
      return false;
    }
  }
}
