package cloud.xcan.angus.core.jpa.repository;

import static cloud.xcan.angus.core.utils.CoreUtils.getAnnotationFieldName;
import static cloud.xcan.angus.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_RESOURCE_NAME;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.biz.ResourceName;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.utils.BeanFieldUtils;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Table;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID>
    implements BaseRepository<T, ID> {

  @PersistenceContext
  private final EntityManager entityManager;

  private JpaEntityInformation<T, ?> jpaEntityInfo;

  private final String resourceName;

  public BaseRepositoryImpl(JpaEntityInformation<T, ?> jpaEntityInfo, EntityManager entityManager) {
    super(jpaEntityInfo, entityManager);
    this.entityManager = entityManager;
    this.jpaEntityInfo = jpaEntityInfo;
    this.resourceName = getAnnotationFieldName(this.jpaEntityInfo.getJavaType(),
        ResourceName.class);
  }

  public BaseRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
    super(domainClass, entityManager);
    this.entityManager = entityManager;
    this.resourceName = getAnnotationFieldName(this.jpaEntityInfo.getJavaType(),
        ResourceName.class);
  }

  @Override
  @Transactional
  public Iterable<T> batchInsert(Iterable<T> entities) {
    Iterator<T> iterator = entities.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      entityManager.persist(iterator.next());
      index++;
      if (index % MAX_BATCH_SIZE == 0) {
        entityManager.flush();
        entityManager.clear();
      }
    }
    if (index % MAX_BATCH_SIZE != 0) {
      entityManager.flush();
      entityManager.clear();
    }
    return entities;
  }

  @Override
  @Transactional
  public void batchInsert0(Iterable<T> entities) {
    Iterator<T> iterator = entities.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      entityManager.persist(iterator.next());
      index++;
      if (index % MAX_BATCH_SIZE == 0) {
        entityManager.flush();
        entityManager.clear();
      }
    }
    if (index % MAX_BATCH_SIZE != 0) {
      entityManager.flush();
      entityManager.clear();
    }
  }

  @Override
  @Transactional
  public Iterable<T> batchUpdate(Iterable<T> entities) {
    assertNotNull(entities, "Entities must not be null!");

    Iterator<T> iterator = entities.iterator();
    int index = 0;
    while (iterator.hasNext()) {
      entityManager.merge(iterator.next());
      index++;
      if (index % MAX_BATCH_SIZE == 0) {
        entityManager.flush();
        entityManager.clear();
      }
    }
    if (index % MAX_BATCH_SIZE != 0) {
      entityManager.flush();
      entityManager.clear();
    }
    return entities;
  }

  @Override
  public String findNameById(ID id) {
    String tableName = getTableName(jpaEntityInfo.getJavaType());
    StringBuilder sql = new StringBuilder();
    if (isNotEmpty(resourceName)) {
      sql.append("SELECT ").append(resourceName).append(" FROM ").append(tableName)
          .append(" WHERE id = :id");
    } else if (hasAttribute(entityManager.getMetamodel().entity(jpaEntityInfo.getJavaType()),
        DEFAULT_RESOURCE_NAME)) {
      sql.append("SELECT name FROM ").append(tableName).append(" WHERE id = :id");
    } else {
      throw new IllegalStateException(
          "The resource name was not found in the entity:" + jpaEntityInfo.getEntityName());
    }
    Query query = entityManager.createNativeQuery(sql.toString());
    query.setParameter("id", id);
    return (String) query.getSingleResult();
  }

  @Override
  public List<String> findNameByIdIn(Collection<ID> ids) {
    String tableName = getTableName(jpaEntityInfo.getJavaType());
    StringBuilder sql = new StringBuilder();
    if (StringUtils.isNotEmpty(resourceName)) {
      sql.append("SELECT ").append(resourceName).append(" FROM ").append(tableName)
          .append(" WHERE id in :ids");
    } else if (hasAttribute(entityManager.getMetamodel().entity(jpaEntityInfo.getJavaType()),
        DEFAULT_RESOURCE_NAME)) {
      sql.append("SELECT name FROM ").append(tableName).append(" WHERE id in :ids");
    } else {
      throw new IllegalStateException(
          "The resource name was not found in the entity:" + jpaEntityInfo.getEntityName());
    }
    Query query = entityManager.createNativeQuery(sql.toString());
    query.setParameter("ids", ids);
    return (List<String>) query.getResultList();
  }

  @Override
  public List<ID> findIdByIdIn(Collection<ID> ids) {
    String tableName = getTableName(jpaEntityInfo.getJavaType());
    String sql = "SELECT id FROM " + tableName + " WHERE id IN :ids";
    Query query = entityManager.createNativeQuery(sql);
    query.setParameter("ids", ids);
    return (List<ID>) query.getResultList();
  }

  @Override
  public List<T> findAllByFilters(Set<SearchCriteria> filters) {
    return findAll(new GenericSpecification<T>(filters));
  }

  @Override
  public List<T> findAllByFilters(Set<SearchCriteria> filters, Sort var2) {
    return findAll(new GenericSpecification<T>(filters), var2);
  }

  @Override
  public <T2, V> List<V> findProjectionByFilters(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<V> cq = cb.createQuery(viewClz);
    Root<T2> entity = cq.from(entityClz);
    List<String> fieldNames = BeanFieldUtils.getPropertyNames(viewClz);
    cq.select(
        cb.construct(viewClz, fieldNames.stream().map(entity::get).toArray(Selection[]::new)));
    // Where clause with dynamic filters
    cq.where(new GenericSpecification<T2>(filters).toPredicate(entity, cq, cb));
    return entityManager.createQuery(cq).getResultList();
  }

  @Override
  public <T2, V> List<V> findProjectionByFilters(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, Sort var2) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<V> cq = cb.createQuery(viewClz);
    Root<T2> entity = cq.from(entityClz);
    List<String> fieldNames = BeanFieldUtils.getPropertyNames(viewClz);
    cq.select(
        cb.construct(viewClz, fieldNames.stream().map(entity::get).toArray(Selection[]::new)));
    if (var2.isSorted()) {
      cq.orderBy(QueryUtils.toOrders(var2, entity, cb));
    }
    // Where clause with dynamic filters
    cq.where(new GenericSpecification<T2>(filters).toPredicate(entity, cq, cb));
    return entityManager.createQuery(cq).getResultList();
  }


  @Override
  public long countAllByFilters(Set<SearchCriteria> filters) {
    return count(new GenericSpecification<T>(filters));
  }

  @Override
  public <T2, V> V sumByFilters(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String sumByField) {
    return sumByFilters(entityClz, viewClz, filters, sumByField, null);
  }

  @Override
  public <T2, V> List<V> countByFiltersAndGroup(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String groupByField, String countByField) {
    return countByFiltersAndGroup(entityClz, viewClz, filters, groupByField, countByField, null);
  }

  @Override
  public <T2, V> List<V> sumByFiltersAndGroup(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String groupByField, String sumByField) {
    return sumByFiltersAndGroup(entityClz, viewClz, filters, groupByField, sumByField, null);
  }

  @Override
  public <T2, V> V sumByFilters(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String sumByField, String sumByFieldAllis) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<V> cq = cb.createQuery(viewClz);
    Root<T2> entity = cq.from(entityClz);
    // Select clause
    Path allisPath = entity.get(sumByField);
    allisPath.alias(nullSafe(sumByFieldAllis, "total"));
    cq.select(cb.construct(viewClz, /*entity.get(sumByField), */cb.sum(allisPath)));

    // Where clause with dynamic filters
    cq.where(new GenericSpecification<T2>(filters).toPredicate(entity, cq, cb));
    List<V> list = entityManager.createQuery(cq).getResultList();
    return isEmpty(list) ? null : list.get(0);
  }

  @Override
  public <T2, V> List<V> countByFiltersAndGroup(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String groupByField, String countByField,
      String countByFieldAllis) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<V> cq = cb.createQuery(viewClz);
    Root<T2> entity = cq.from(entityClz);
    // Select clause
    Path allisPath = entity.get(countByField);
    allisPath.alias(nullSafe(countByFieldAllis, "total"));
    cq.select(cb.construct(viewClz, entity.get(groupByField), cb.count(allisPath)));

    // Where clause with dynamic filters
    cq.where(new GenericSpecification<T2>(filters).toPredicate(entity, cq, cb));

    // Group By clause
    cq.groupBy(entity.get(groupByField));
    return entityManager.createQuery(cq).getResultList();
  }

  @Override
  public <T2, V> List<V> sumByFiltersAndGroup(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String groupByField, String sumByField,
      String sumByFieldAllis) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<V> cq = cb.createQuery(viewClz);
    Root<T2> entity = cq.from(entityClz);
    // Select clause
    Path allisPath = entity.get(sumByField);
    allisPath.alias(nullSafe(sumByFieldAllis, "total"));
    cq.select(cb.construct(viewClz, entity.get(groupByField), allisPath));

    // Where clause with dynamic filters
    cq.where(new GenericSpecification<T2>(filters).toPredicate(entity, cq, cb));

    // Group By clause
    cq.groupBy(entity.get(groupByField));
    return entityManager.createQuery(cq).getResultList();
  }

  public static String getTableName(Class<?> entityClass) {
    // Check if @Table annotation is used
    if (entityClass.isAnnotationPresent(Table.class)) {
      Table table = entityClass.getAnnotation(Table.class);
      if (!table.name().isEmpty()) {
        return table.name();
      }
    }
    // If no table name is specified, return the simple name of the entity class (default behavior)
    return entityClass.getSimpleName();
  }

  public static <T> boolean hasAttribute(EntityType<T> entityType, String propertyName) {
    return entityType.getDeclaredAttributes().stream()
        .anyMatch(attribute -> attribute.getName().equals(propertyName));
  }
}
