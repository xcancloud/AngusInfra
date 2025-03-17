package cloud.xcan.sdf.core.biz.cmd;

import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.spec.experimental.Assert.assertNotEmpty;
import static cloud.xcan.sdf.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_RESOURCE_ID;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_TEXT_SEARCH_COLUMN;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.sdf.spec.experimental.Assert;
import cloud.xcan.sdf.spec.experimental.Entity;
import cloud.xcan.sdf.spec.experimental.IdKey;
import cloud.xcan.sdf.spec.utils.StringUtils;
import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.util.ReflectionUtils;

/**
 * @param <T>  Entity Object
 * @param <ID> Entity ID
 * @author XiaoLong Liu
 */
public abstract class CommCmd<T extends Entity, ID extends Serializable> {

  @Resource
  @PersistenceContext
  protected EntityManager entityManager;

  @Resource
  protected CachedUidGenerator uidGenerator;

  @Autowired(required = false)
  protected CacheManager cacheManager;

  public Class<?> entityClazz;
  public Class<?> idClazz;

  public CommCmd() {
    ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
    Type[] typeArguments = type.getActualTypeArguments();
    if (nonNull(typeArguments) && typeArguments.length > 2) {
      this.entityClazz = (Class<?>) type.getActualTypeArguments()[0];
      this.idClazz = (Class<?>) type.getActualTypeArguments()[1];
    }
  }

  protected abstract BaseRepository<T, ID> getRepository();

  /**
   * Batch Insert
   *
   * @param entities Persistent entities
   */
  public void batchInsert0(Collection<T> entities) {
    assertNotEmpty(entities, "Batch insert entities is empty");
    Class<?> entityClass = entities.stream().findFirst().get().getClass();
    Field id = CoreUtils.getResourceIdFiled(entityClass, DEFAULT_RESOURCE_ID);
    Assert.assertNotNull(id, "ID column is required");
    id.setAccessible(true);

    Field extSearchMerge = CoreUtils.getFiled(entityClass, DEFAULT_TEXT_SEARCH_COLUMN);
    if (nonNull(extSearchMerge)) {
      extSearchMerge.setAccessible(true);
    }

    for (T entity : entities) {
      try {
        if (Objects.isNull(id.get(entity))) {
          long idValue = uidGenerator.getUID();
          id.set(entity, idValue);
        }
        if (nonNull(extSearchMerge)) {
          extSearchMerge.set(entity, String.valueOf(id.get(entity)));
        }
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Please check specifications, the id not found", e.getCause());
      }
    }
    getRepository().batchInsert(entities);
  }

  /**
   * Batch Insert
   *
   * @param entities Persistent entities
   * @return IdKey The IdKey only contains IDs, that is, the server returns a list of IDs
   * corresponding to the data sequence of the client. In this case, the client must ensure that the
   * batched data sequence is unchanged.
   */
  public List<IdKey<ID, Object>> batchInsert(Collection<T> entities) {
    assertNotEmpty(entities, "Batch insert entities is empty");
    List<IdKey<ID, Object>> ids = new ArrayList<>(entities.size());
    Class<?> entityClass = entities.stream().findFirst().get().getClass();
    Field id = CoreUtils.getResourceIdFiled(entityClass, DEFAULT_RESOURCE_ID);
    Assert.assertNotNull(id, "ID column is required");
    id.setAccessible(true);

    Field extSearchMerge = CoreUtils.getFiled(entityClass, DEFAULT_TEXT_SEARCH_COLUMN);
    if (nonNull(extSearchMerge)) {
      extSearchMerge.setAccessible(true);
    }

    for (T entity : entities) {
      try {
        if (Objects.isNull(id.get(entity))) {
          long idValue = uidGenerator.getUID();
          id.set(entity, idValue);
        }
        if (nonNull(extSearchMerge)) {
          extSearchMerge.set(entity, String.valueOf(id.get(entity)));
        }
        ids.add(new IdKey<ID, Object>().setId((ID) entity.identity()));
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Please check specifications, the id not found", e.getCause());
      }
    }
    getRepository().batchInsert(entities);
    return ids;
  }

  /**
   * Batch Insert
   *
   * @param entities Persistent entities
   * @param keyName  Business identification field name
   * @return IdKey Return IdKey contains ID and business identification (keyName field) value
   */
  public List<IdKey<ID, Object>> batchInsert(Collection<T> entities, String keyName) {
    assertNotEmpty(entities, "Batch insert entities is empty");
    List<IdKey<ID, Object>> ids = new ArrayList<>(entities.size());
    Class<?> entityClass = entities.stream().findFirst().get().getClass();
    Field id = CoreUtils.getResourceIdFiled(entityClass, DEFAULT_RESOURCE_ID);
    Assert.assertNotNull(id, "ID column is required");
    id.setAccessible(true);

    Field key = ReflectionUtils.findField(entityClass, keyName);
    Assert.assertNotNull(key, "key column is required");
    key.setAccessible(true);

    Field extSearchMerge = CoreUtils.getFiled(entityClass, DEFAULT_TEXT_SEARCH_COLUMN);
    if (nonNull(extSearchMerge)) {
      extSearchMerge.setAccessible(true);
    }

    for (T entity : entities) {
      try {
        if (Objects.isNull(id.get(entity))) {
          long idValue = uidGenerator.getUID();
          id.set(entity, idValue);
        }
        if (nonNull(extSearchMerge)) {
          extSearchMerge.set(entity, String.valueOf(id.get(entity)));
        }
        ids.add(new IdKey<ID, Object>().setId((ID) entity.identity()).setKey(key.get(entity)));
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Please check specifications, the id and " + keyName + " not found", e.getCause());
      }
    }
    getRepository().batchInsert(entities);
    return ids;
  }

  /**
   * Insert
   *
   * @param entity  Persistent entity
   * @param keyName Business identification field name
   * @return IdKey Return IdKey contains ID and business identification (keyName field) value
   */
  public IdKey<ID, Object> insert(T entity, String keyName) {
    assertNotNull(entity, "Insert entity is empty");
    Field id = CoreUtils.getResourceIdFiled(entity.getClass(), DEFAULT_RESOURCE_ID);
    Assert.assertNotNull(id, "ID column is required");
    id.setAccessible(true);

    Field key = ReflectionUtils.findField(entity.getClass(), keyName);
    Assert.assertNotNull(key, "key column is required");
    key.setAccessible(true);

    Field extSearchMerge = CoreUtils.getFiled(entity.getClass(), DEFAULT_TEXT_SEARCH_COLUMN);
    if (nonNull(extSearchMerge)) {
      extSearchMerge.setAccessible(true);
    }

    try {
      if (Objects.isNull(id.get(entity))) {
        long idValue = uidGenerator.getUID();
        id.set(entity, idValue);
      }
      if (nonNull(extSearchMerge)) {
        extSearchMerge.set(entity, String.valueOf(id.get(entity)));
      }
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Please check specifications, the setId not found");
    }

    // Fix:: getRepository().save(entity); -> Will trigger a query statement!!
    getRepository().batchInsert(Collections.singleton(entity));

    try {
      return new IdKey<ID, Object>().setId((ID) entity.identity()).setKey(key.get(entity));
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException(
          "Please check specifications, the id and " + keyName + " not found", e.getCause());
    }
  }

  /**
   * Insert
   *
   * @param entity Persistent entity
   */
  public void insert0(T entity) {
    assertNotNull(entity, "Insert entity is empty");
    Field id = CoreUtils.getResourceIdFiled(entity.getClass(), DEFAULT_RESOURCE_ID);
    Assert.assertNotNull(id, "ID column is required");
    id.setAccessible(true);

    Field extSearchMerge = CoreUtils.getFiled(entity.getClass(), DEFAULT_TEXT_SEARCH_COLUMN);
    if (nonNull(extSearchMerge)) {
      extSearchMerge.setAccessible(true);
    }

    try {
      if (Objects.isNull(id.get(entity))) {
        long idValue = uidGenerator.getUID();
        id.set(entity, idValue);
      }
      if (nonNull(extSearchMerge)) {
        extSearchMerge.set(entity, String.valueOf(id.get(entity)));
      }
    } catch (IllegalAccessException e) {
      throw new IllegalArgumentException("Please check specifications, the setId not found");
    }

    // Fix:: getRepository().save(entity); -> Will trigger a query statement!!
    getRepository().batchInsert(Collections.singleton(entity));
  }

  /**
   * Insert
   *
   * @param entity Persistent entity
   * @return IdKey Return IdKey contains ID
   */
  public IdKey<ID, Object> insert(T entity) {
    assertNotNull(entity, "Insert entity is empty");
    if (Objects.isNull(entity.identity())) {
      Field id = CoreUtils.getResourceIdFiled(entity.getClass(), DEFAULT_RESOURCE_ID);
      Assert.assertNotNull(id, "ID column is required");
      id.setAccessible(true);

      Field extSearchMerge = CoreUtils.getFiled(entity.getClass(), DEFAULT_TEXT_SEARCH_COLUMN);
      if (nonNull(extSearchMerge)) {
        extSearchMerge.setAccessible(true);
      }
      try {
        if (Objects.isNull(id.get(entity))) {
          long idValue = uidGenerator.getUID();
          id.set(entity, idValue);
        }
        if (nonNull(extSearchMerge)) {
          extSearchMerge.set(entity, String.valueOf(id.get(entity)));
        }
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException("Please check specifications, the setId not found");
      }
    }
    // Fix:: getRepository().save(entity); -> Will trigger a query statement!!
    getRepository().batchInsert(Collections.singleton(entity));
    return new IdKey<ID, Object>().setId((ID) entity.identity());
  }

  public List<T> batchUpdate0(Collection<T> entities) {
    assertNotEmpty(entities, "Batch update entities is empty");
    getRepository().batchUpdate(entities);
    return new ArrayList<>(entities);
  }

  /**
   * Batch update entities, throw 404 when it doesn't exist
   *
   * @param entities Persistent entities
   */
  public List<T> batchUpdateOrNotFound(Collection<T> entities) {
    assertNotEmpty(entities, "Batch update entities is empty");
    return batchUpdateOrNotFound0(entities);
  }

  /**
   * Batch update entities, throw 404 when it doesn't exist
   *
   * @param entities Persistent entities
   */
  public List<T> batchUpdateOrNotFound0(Collection<T> entities) {
    assertNotEmpty(entities, "Batch update entities is empty");
    List<ID> ids = entities.stream().map(entity -> (ID) entity.identity())
        .collect(Collectors.toList());
    List<T> dbEntities = getRepository().findAllById(ids);
    checkNotFound(entities, ids, dbEntities);
    getRepository().batchUpdate(
        CoreUtils.batchCopyPropertiesIgnoreNull(new ArrayList<>(entities), dbEntities));
    return dbEntities;
  }

  /**
   * Update entity, throw 404 when it doesn't exist
   *
   * @param entity Persistent entity
   */
  public T updateOrNotFound(T entity) {
    return updateOrNotFound0(entity);
  }

  /**
   * Update entity, throw 404 when it doesn't exist
   *
   * @param entity Persistent entity
   */
  public T updateOrNotFound0(T entity) {
    assertNotNull(entity, "Update entity is empty");
    assertNotNull(entity.identity(), "Update "
        + entity.getClass().getSimpleName() + " identity value is null");

    Optional<T> dbEntity = getRepository().findById((ID) entity.identity());
    if (dbEntity.isEmpty()) {
      throw ResourceNotFound.of(entity.identity().toString(), entity.getClass().getSimpleName());
    }
    CoreUtils.copyPropertiesIgnoreNull(entity, dbEntity.get());
    getRepository().save(dbEntity.get());
    return dbEntity.get();
  }

  public int updateBySelective(T entity, List<String> fields) {
    return updateBySelective0(entity, "id", fields);
  }

  /**
   * Warn:: Json type fields are not supported.
   */
  @SneakyThrows
  public int updateBySelective0(T entity, String idName, List<String> fields) {
    Assert.assertTrue(nonNull(idName) && nonNull(fields),
        "Parameter idName and fields is required");
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaUpdate update = cb.createCriteriaUpdate(entity.getClass());
    Root root = update.from(entity.getClass());
    for (String field : fields) {
      Field f = FieldUtils.getField(entity.getClass(), field, true);
      update.set(root.get(field), f.get(entity));
    }
    update.where(cb.equal(root.get(idName),
        FieldUtils.getField(entity.getClass(), idName, true).get(entity)));
    return entityManager.createQuery(update).executeUpdate();
  }

  private void checkNotFound(Collection<T> entities, List<ID> ids, List<T> dbEntities) {
    Class<?> entityClass = entities.stream().findFirst().get().getClass();
    if (ids.size() != dbEntities.size()) {
      List<ID> dbIds = dbEntities.stream().map(entity -> (ID) entity.identity())
          .collect(Collectors.toList());
      assertResourceNotFound(!dbIds.isEmpty(), StringUtils.join(ids, ","),
          entityClass.getSimpleName());
      List<ID> notFoundIds = ids.stream().filter(x -> !dbIds.contains(x))
          .collect(Collectors.toList());
      assertResourceNotFound(!notFoundIds.isEmpty(),
          StringUtils.join(notFoundIds, ","), entityClass.getSimpleName());
    }
  }

  /**
   * Batch replace entities, save object if it doesn't exist
   *
   * @param entities Persistent entities
   */
  public void batchReplaceOrInsert(Collection<T> entities) {
    assertNotEmpty(entities, "Batch replace or insert entities is empty");
    Class entityClass = entities.stream().findFirst().get().getClass();
    Field id = CoreUtils.getResourceIdFiled(entityClass, DEFAULT_RESOURCE_ID);
    Assert.assertNotNull(id, "ID column is required");
    id.setAccessible(true);

    Field extSearchMerge = CoreUtils.getFiled(entityClass, DEFAULT_TEXT_SEARCH_COLUMN);
    if (nonNull(extSearchMerge)) {
      extSearchMerge.setAccessible(true);
    }
    getRepository().batchUpdate(entities.stream().peek(entity -> {
      try {
        if (Objects.isNull(id.get(entity))) {
          long idValue = uidGenerator.getUID();
          id.set(entity, idValue);
        }
        if (nonNull(extSearchMerge)) {
          extSearchMerge.set(entity, String.valueOf(id.get(entity)));
        }
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException("Please check specifications, the setId not found");
      }
    }).collect(Collectors.toList()));
  }
}
