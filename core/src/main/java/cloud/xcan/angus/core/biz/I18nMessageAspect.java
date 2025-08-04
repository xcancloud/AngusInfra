package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.spec.locale.SdfLocaleHolder.getLocale;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isBlank;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.repository.I18nMessageJoinRepository;
import cloud.xcan.angus.remote.MessageJoinField;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Aspect
public class I18nMessageAspect extends AbstractJoinAspect {

  /**
   * Type -> Language -> i18nMessage
   */
  @Getter
  private final Map<String, Map<String, Map<String, I18nMessage>>> messagesMap = new ConcurrentHashMap<>();
  /**
   * Vo.class -> filedName -> MessageJoinField
   */
  private final Map<Class<?>, Map<String, MessageJoinField>> cacheFieldNameMap = new ConcurrentHashMap<>();

  @Autowired(required = false)
  private I18nMessageJoinRepository<? extends I18nMessage> messageRepository;

  @Pointcut("@annotation(cloud.xcan.angus.core.biz.MessageJoin)")
  public void voJoinPointCut() {
  }

  @Around("voJoinPointCut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    return aspect(joinPoint);
  }

  public void clearCache() {
    log.debug("Clear message cache");
    this.cacheFieldNameMap.clear();
    this.messagesMap.clear();
  }

  @Override
  public void joinArrayVoName(Object[] voArray) throws IllegalAccessException {
    if (Locale.CHINA.equals(getLocale())) {
      return;
    }
    String lang = getLocale().getLanguage();
    Map<String, Set<String>> fieldNameAndValues = findFieldNameAndValues(voArray);
    if (isEmpty(fieldNameAndValues)) {
      return;
    }
    Class<?> firstClass = voArray[0].getClass();
    for (String fieldName : fieldNameAndValues.keySet()) {
      MessageJoinField fieldNameJoin = cacheFieldNameMap.get(firstClass).get(fieldName);
      String type = fieldNameJoin.type();

      // Find from cache
      Map<String, I18nMessage> typeMessageMap = messagesMap.containsKey(type)
          ? messagesMap.get(type).get(lang) : null;

      // If not found in the cache, search from the database
      if (isEmpty(typeMessageMap)) {
        log.info("The message cache is empty, Find from the database");
        typeMessageMap = getI18nMessageMap(fieldNameJoin, type, lang, fieldNameAndValues);
      } else {
        log.info("The message is found form cache");
      }

      if (isEmpty(typeMessageMap)) {
        continue;
      }

      setFieldValueMessage(voArray, fieldName, firstClass, typeMessageMap);
    }
  }

  private @Nullable Map<String, I18nMessage> getI18nMessageMap(MessageJoinField fieldNameJoin,
      String type, String lang, Map<String, Set<String>> fieldNameAndValues) {
    Map<String, I18nMessage> messageMap;
    if (fieldNameJoin.enabledCache()) {
      log.info("Find from the database, cache is enabled");
      List<? extends I18nMessage> typeMessages = messageRepository.findByType(type);
      if (isEmpty(typeMessages)) {
        return null;
      }

      log.info("Cache after finding from database");
      cacheTypeMessage(type, typeMessages);

      messageMap = messagesMap.get(type).get(lang);
    } else {
      log.info("Look up the message in the database based on the type and default message");
      Set<String> fieldValues = fieldNameAndValues.get(type);
      messageMap = messageRepository
          .findByTypeAndLanguageAndDefaultMessageIn(type, lang, fieldValues)
          .stream().collect(Collectors.toMap(I18nMessage::getDefaultMessage, (p) -> p));
    }

    return messageMap;
  }

  private Map<String, Set<String>> findFieldNameAndValues(Object[] voArray)
      throws IllegalAccessException {
    Object first = voArray[0];
    Map<String, Set<String>> fieldNameAndValues = new HashMap<>();
    Set<String> values;
    Field messageField;
    Map<String, MessageJoinField> nameJoinFieldMap = findAndCacheJoinInfo(first);
    for (String fieldName : nameJoinFieldMap.keySet()) {
      messageField = FieldUtils.getField(first.getClass(), fieldName, true);
      Object defaultMessageValue;
      values = new HashSet<>();
      for (Object vo : voArray) {
        defaultMessageValue = messageField.get(vo);
        if (defaultMessageValue != null) {
          values.add(defaultMessageValue.toString());
        }
      }
      if (isNotEmpty(values)) {
        fieldNameAndValues.put(messageField.getName(), values);
      }
    }
    return fieldNameAndValues;
  }

  private Map<String, MessageJoinField> findAndCacheJoinInfo(Object first) {
    Map<String, MessageJoinField> messageJoinFieldMap = cacheFieldNameMap.get(first.getClass());
    if (MapUtils.isNotEmpty(messageJoinFieldMap)) {
      return messageJoinFieldMap;
    }
    messageJoinFieldMap = new HashMap<>();
    Field[] fields = first.getClass().getDeclaredFields();
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
    if (MapUtils.isEmpty(messageJoinFieldMap)) {
      throw new IllegalStateException("MessageJoinField not found for class "
          + first.getClass().getName());
    }
    cacheFieldNameMap.put(first.getClass(), messageJoinFieldMap);
    return messageJoinFieldMap;
  }

  private void cacheTypeMessage(String type, List<? extends I18nMessage> i18nMessages) {
    Map<String, List<I18nMessage>> typeLanguageMap = i18nMessages.stream()
        .collect(Collectors.groupingBy(I18nMessage::getLanguage));
    if (isNotEmpty(typeLanguageMap)) {
      for (String typeLanguage : typeLanguageMap.keySet()) {
        Map<String, I18nMessage> typeLanguageDefaultMap = typeLanguageMap.get(typeLanguage)
            .stream().collect(Collectors.toMap(I18nMessage::getDefaultMessage, (p) -> p));
        if (isNotEmpty(typeLanguageDefaultMap)) {
          Map<String, Map<String, I18nMessage>> defaultMaps = new HashMap<>();
          defaultMaps.put(typeLanguage, typeLanguageDefaultMap);
          messagesMap.put(type, defaultMaps);
        }
      }
    }
  }

  private static void setFieldValueMessage(Object[] voArray, String fieldName, Class<?> firstClass,
      Map<String, I18nMessage> messageMap) throws IllegalAccessException {
    Field voNameField = FieldUtils.getField(firstClass, fieldName, true);
    for (Object vo : voArray) {
      Object voValue = voNameField.get(vo);
      if (isNull(voValue)) {
        continue;
      }
      I18nMessage i18nMessage = messageMap.get(voValue.toString());
      if (isNull(i18nMessage) || isEmpty(i18nMessage.getI18nMessage())) {
        continue;
      }
      voNameField.set(vo, i18nMessage.getI18nMessage());
    }
  }
}
