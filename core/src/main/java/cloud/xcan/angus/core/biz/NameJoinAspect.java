package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.core.utils.BeanFieldUtils.hasProperty;
import static cloud.xcan.angus.core.utils.CoreUtils.getAnnotationFieldName;
import static cloud.xcan.angus.core.utils.CoreUtils.getResourceId;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_RESOURCE_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_RESOURCE_NAME;

import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.remote.NameJoinField;
import cloud.xcan.angus.spec.annotations.NonNullable;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Slf4j
@Aspect
public class NameJoinAspect extends AbstractJoinAspect {

  /**
   * Vo.class -> filedName -> NameJoinField
   */
  private final Map<Class<?>, Map<String, NameJoinField>> classFieldJoinNameMap = new ConcurrentHashMap<>();
  /**
   * Merge queries by repo: Vo.class + "#" + nameJoin.repository() -> NameJoinRepository
   * <p>
   * Do not merge queries by repo (@Deprecated) : ->>> Vo.class + "#" + nameJoin.id() ->
   * NameJoinRepository
   */
  private final Map<String, NameJoinRepository<?, ?>> classRepositoryMap = new ConcurrentHashMap<>();
  /**
   * Vo.class -> Vo.id
   */
  private final Map<Class<?>, String> classResourceIdMap = new ConcurrentHashMap<>();
  /**
   * Vo.class -> Vo.name
   */
  private final Map<Class<?>, String> classResourceNameMap = new ConcurrentHashMap<>();

  public NameJoinAspect() {
  }

  @Pointcut("@annotation(cloud.xcan.angus.core.biz.NameJoin)")
  public void voJoinPointCut() {
  }

  @Around("voJoinPointCut()")
  public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
    return aspect(joinPoint);
  }

  @Override
  public void joinArrayVoName(Object[] voArray) throws IllegalAccessException {
    Map<String, Map<String, Set>> repoAndFieldNameAndIdValues
        = findRepoAndFieldNameAndIdValues(voArray);
    Class<?> firstClass = voArray[0].getClass();
    for (String repo : repoAndFieldNameAndIdValues.keySet()) {
      Map<String, Set> fieldNameAndIdValues = repoAndFieldNameAndIdValues.get(repo);
      if (ObjectUtils.isEmpty(fieldNameAndIdValues)) {
        continue;
      }

      // Merge same repo corresponding ids
      Set allIds = new HashSet();
      for (Entry<String, Set> fieldNameAndIdValue : fieldNameAndIdValues.entrySet()) {
        allIds.addAll(fieldNameAndIdValue.getValue());
      }

      // Query all resource data based on ids
      Collection<?> entities = classRepositoryMap.get(firstClass + "#" + repo).findByIdIn(allIds);
      if (ObjectUtils.isEmpty(entities)) {
        log.warn("Class {} join repository#{} is empty by all ids in {}",
            firstClass.getSimpleName(), repo, allIds);
        log.warn("Class {} join repository#{} ignored", firstClass.getSimpleName(), repo);
        continue;
      }

      // Find resource id and name in entity, Convert entity id and name to map
      Object firstEntity = entities.stream().findFirst().get();
      Map<Object, String> idNames = getIdNamesMap(firstEntity, entities);

      for (Entry<String, Set> fieldNameAndIdValues0 : fieldNameAndIdValues.entrySet()) {
        // Join vo and entity by id
        Map<String, NameJoinField> nameJoinFieldMap = classFieldJoinNameMap.get(firstClass);
        for (Entry<String, NameJoinField> entry : nameJoinFieldMap.entrySet()) {
          if (fieldNameAndIdValues0.getKey().equals(entry.getValue().id())) {
            Field voNameField = FieldUtils.getField(firstClass, entry.getKey(), true);
            Field voIdField = FieldUtils.getField(firstClass, entry.getValue().id(), true);
            Object voIdValue;
            for (Object vo : voArray) {
              voIdValue = voIdField.get(vo);
              if (voIdValue == null) {
                continue;
              }
              voNameField.set(vo, idNames.get(voIdValue));
            }
          }
        }
      }
    }
  }

  private Map<String, Map<String, Set>> findRepoAndFieldNameAndIdValues(Object[] voArray)
      throws IllegalAccessException {
    Object first = voArray[0];
    Map<String, Map<String, Set>> repoAndFieldNameAndIdValues = new HashMap<>();
    Set idValues;
    Field idField;
    Map<String, NameJoinField> nameJoinFieldMap = findAndCacheJoinInfo(first);
    for (NameJoinField joinField : nameJoinFieldMap.values()) {
      idField = FieldUtils.getField(first.getClass(), joinField.id(), true);
      Object id;
      idValues = new HashSet<>();
      for (Object vo : voArray) {
        id = idField.get(vo);
        if (id != null) {
          idValues.add(id);
        }
      }
      if (ObjectUtils.isNotEmpty(idValues)) {
        if (repoAndFieldNameAndIdValues.containsKey(joinField.repository())) {
          repoAndFieldNameAndIdValues.get(joinField.repository()).put(idField.getName(), idValues);
        } else {
          Map<String, Set> fieldNameAndIdValues = new HashMap<>();
          fieldNameAndIdValues.put(idField.getName(), idValues);
          repoAndFieldNameAndIdValues.put(joinField.repository(), fieldNameAndIdValues);
        }
      }
    }
    return repoAndFieldNameAndIdValues;
  }

  private Map<String, NameJoinField> findAndCacheJoinInfo(Object first) {
    Map<String, NameJoinField> nameJoinFieldMap = classFieldJoinNameMap.get(first.getClass());
    if (MapUtils.isNotEmpty(nameJoinFieldMap)) {
      return nameJoinFieldMap;
    }

    nameJoinFieldMap = new HashMap<>();
    Field[] fields = first.getClass().getDeclaredFields();
    for (Field field : fields) {
      NameJoinField nameJoin = field.getAnnotation(NameJoinField.class);
      if (Objects.nonNull(nameJoin)) {
        if (StringUtils.isBlank(nameJoin.id())) {
          throw new IllegalArgumentException(
              "The NameJoinField property id of " + field.getName() + " is empty");
        }
        Field idField = FieldUtils.getField(first.getClass(), nameJoin.id(), true);
        if (Objects.isNull(idField)) {
          throw new IllegalArgumentException(
              "The NameJoinField id field " + nameJoin.id() + " not found");
        }
        if (StringUtils.isBlank(nameJoin.repository())) {
          throw new IllegalArgumentException(
              " The NameJoinField property repository of " + field.getName() + " is empty");
        }
        NameJoinRepository<?, ?> repository = SpringContextHolder
            .getBean(nameJoin.repository(), NameJoinRepository.class);
        Assert.assertNotNull(repository, nameJoin.repository() + " not found");
        classRepositoryMap.put(first.getClass() + "#" + nameJoin.repository(), repository);
        nameJoinFieldMap.put(field.getName(), nameJoin);
      }
    }
    if (MapUtils.isEmpty(nameJoinFieldMap)) {
      throw new IllegalStateException(
          "NameJoinField not found for class " + first.getClass().getName());
    }
    classFieldJoinNameMap.put(first.getClass(), nameJoinFieldMap);
    return nameJoinFieldMap;
  }

  @NonNullable
  private Map<Object, String> getIdNamesMap(Object firstEntity, Collection<?> entities)
      throws IllegalAccessException {
    // Find resource id and name in entity
    String resourceName = findEntityResourceName(firstEntity);
    Field nameField = FieldUtils.getField(firstEntity.getClass(), resourceName, true);
    String resourceId = findEntityResourceId(firstEntity);
    Field idField = FieldUtils.getField(firstEntity.getClass(), resourceId, true);

    // Convert entity id and name to map
    Map<Object, String> idNames = new HashMap<>();
    for (Object entity : entities) {
      idNames.put(idField.get(entity), nameField.get(entity).toString());
    }
    return idNames;
  }

  private String findEntityResourceId(Object entity) {
    String resourceId = classResourceIdMap.get(entity.getClass());
    if (StringUtils.isNotEmpty(resourceId)) {
      return resourceId;
    }

    resourceId = getAnnotationFieldName(entity.getClass(), ResourceId.class);
    if (StringUtils.isEmpty(resourceId)) {
      resourceId = getResourceId(entity.getClass());
      if (StringUtils.isEmpty(resourceId)) {
        if (hasProperty(entity, DEFAULT_RESOURCE_ID)) {
          resourceId = DEFAULT_RESOURCE_ID;
        }
      }
      if (StringUtils.isEmpty(resourceId)) {
        throw new IllegalStateException("The id of " + entity.getClass().getSimpleName()
            + " not found, please mark the id field with the @Id annotation");
      }
    }
    classResourceIdMap.put(entity.getClass(), resourceId);
    return resourceId;
  }

  private String findEntityResourceName(Object entity) {
    String resourceName = classResourceNameMap.get(entity.getClass());
    if (StringUtils.isNotEmpty(resourceName)) {
      return resourceName;
    }
    resourceName = getAnnotationFieldName(entity.getClass(), ResourceName.class);
    if (StringUtils.isEmpty(resourceName)) {
      if (hasProperty(entity, DEFAULT_RESOURCE_NAME)) {
        resourceName = DEFAULT_RESOURCE_NAME;
      }
    }
    if (StringUtils.isEmpty(resourceName)) {
      throw new IllegalStateException("The name of " + entity.getClass().getSimpleName()
          + " not found, please mark the name field with the @ResourceName annotation");
    }
    classResourceNameMap.put(entity.getClass(), resourceName);
    return resourceName;
  }
}
