package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.spec.locale.SdfLocaleHolder.getLocale;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isBlank;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.jpa.repository.I18nMessageJoinRepository;
import cloud.xcan.angus.remote.MessageJoinField;
import cloud.xcan.angus.spec.SpecConstant;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@Aspect
public class I18nMessageAspect extends AbstractJoinAspect {

  /**
   * Type -> Language -> defaultMessage -> i18nMessage
   */
  private final Map<String, Map<String, Map<String, I18nMessage>>> typeLanguageMessagesMap = new ConcurrentHashMap<>();
  /**
   * Vo.class -> filedName -> MessageJoinField
   */
  private final Map<Class<?>, Map<String, MessageJoinField>> classFieldNameJoinMap = new ConcurrentHashMap<>();

  @Autowired(required = false)
  private I18nMessageJoinRepository<? extends I18nMessage> i18nMessageJoinRepository;

  @Pointcut("@annotation(cloud.xcan.angus.core.biz.MessageJoin)")
  public void voJoinPointCut() {
  }

  @Around("voJoinPointCut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    return aspect(joinPoint);
  }

  public void clearCache() {
    this.classFieldNameJoinMap.clear();
    this.typeLanguageMessagesMap.clear();
  }

  public Map<String, Map<String, Map<String, I18nMessage>>> getTypeLanguageMessagesMap() {
    return typeLanguageMessagesMap;
  }

  @Override
  public void joinArrayVoName(Object[] voArray) throws IllegalAccessException {
    if (SpecConstant.DEFAULT_LOCALE.equals(getLocale())) {
      return;
    }
    Map<String, Set<String>> fieldNameAndValues = findFieldNameAndValues(voArray);
    Class<?> firstClass = voArray[0].getClass();
    for (String fieldName : fieldNameAndValues.keySet()) {
      MessageJoinField fieldNameJoin = classFieldNameJoinMap.get(firstClass).get(fieldName);

      // Find from cache
      Map<String, I18nMessage> messageMap = null;
      if (isNotEmpty(typeLanguageMessagesMap.get(fieldNameJoin.type()))) {
        Map<String, Map<String, I18nMessage>> languageMessagesMap = typeLanguageMessagesMap
            .get(fieldNameJoin.type());
        messageMap = languageMessagesMap.get(getLocale().getLanguage());
      }
      // Not found in cache
      if (isEmpty(messageMap)) {
        if (fieldNameJoin.enabledCache()) {
          // Cache after finding from the database
          List<? extends I18nMessage> i18nMessages = i18nMessageJoinRepository
              .findByType(fieldNameJoin.type());
          if (isEmpty(i18nMessages)) {
            continue;
          }
          cacheMessage(i18nMessages);
          if (Objects.isNull(typeLanguageMessagesMap.get(fieldNameJoin.type())) ||
              Objects.isNull(typeLanguageMessagesMap.get(fieldNameJoin.type())
                  .get(getLocale().getLanguage()))) {
            continue;
          }
          messageMap = typeLanguageMessagesMap.get(fieldNameJoin.type())
              .get(getLocale().getLanguage());
        } else {
          // Find from database
          if (isEmpty(fieldNameAndValues.get(fieldNameJoin.type()))) {
            continue;
          }
          List<? extends I18nMessage> i18nMessages = i18nMessageJoinRepository
              .findByTypeAndLanguageAndDefaultMessageIn(fieldNameJoin.type(),
                  getLocale().getLanguage(), fieldNameAndValues.get(fieldNameJoin.type()));
          if (isEmpty(i18nMessages)) {
            continue;
          }
          messageMap = i18nMessages.stream()
              .collect(Collectors.toMap(I18nMessage::getDefaultMessage, (p) -> p));
        }
      }
      if (isEmpty(messageMap)) {
        return;
      }
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
    Map<String, MessageJoinField> messageJoinFieldMap = classFieldNameJoinMap.get(first.getClass());
    if (MapUtils.isNotEmpty(messageJoinFieldMap)) {
      return messageJoinFieldMap;
    }
    messageJoinFieldMap = new HashMap<>();
    Field[] fields = first.getClass().getDeclaredFields();
    for (Field field : fields) {
      MessageJoinField messageJoin = field.getAnnotation(MessageJoinField.class);
      if (nonNull(messageJoin)) {
        if (isBlank(messageJoin.type())) {
          throw new IllegalArgumentException(
              "The MessageJoinField property type of " + field.getName() + " is empty");
        }
        messageJoinFieldMap.put(field.getName(), messageJoin);
      }
    }
    if (MapUtils.isEmpty(messageJoinFieldMap)) {
      throw new IllegalStateException(
          "MessageJoinField not found for class " + first.getClass().getName());
    }
    classFieldNameJoinMap.put(first.getClass(), messageJoinFieldMap);
    return messageJoinFieldMap;
  }

  private void cacheMessage(List<? extends I18nMessage> i18nMessages) {
    if (isEmpty(i18nMessages)) {
      return;
    }
    Map<String, List<I18nMessage>> typeMaps = i18nMessages.stream()
        .collect(Collectors.groupingBy(I18nMessage::getType));
    for (String type : typeMaps.keySet()) {
      Map<String, List<I18nMessage>> typeLanguageMap = typeMaps.get(type).stream()
          .collect(Collectors.groupingBy(I18nMessage::getLanguage));
      if (isNotEmpty(typeLanguageMap)) {
        for (String typeLanguage : typeLanguageMap.keySet()) {
          Map<String, I18nMessage> typeLanguageDefaultMap = typeLanguageMap.get(typeLanguage)
              .stream().collect(Collectors.toMap(I18nMessage::getDefaultMessage, (p) -> p));
          if (isNotEmpty(typeLanguageDefaultMap)) {
            Map<String, Map<String, I18nMessage>> defaultMaps = new HashMap<>();
            defaultMaps.put(typeLanguage, typeLanguageDefaultMap);
            typeLanguageMessagesMap.put(type, defaultMaps);
          }
        }
      }
    }
  }
}
