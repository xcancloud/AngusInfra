package cloud.xcan.angus.core.jpa.repository;

import static cloud.xcan.angus.core.jpa.JpaMetadataUtils.getTableName;
import static cloud.xcan.angus.core.jpa.JpaMetadataUtils.hasAttribute;
import static cloud.xcan.angus.core.utils.CoreUtils.getAnnotationFieldName;
import static cloud.xcan.angus.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_RESOURCE_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.data.support.PageableExecutionUtils.getPage;

import cloud.xcan.angus.core.biz.ResourceName;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.utils.BeanFieldUtils;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.jpa.support.PageableUtils;
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
    String tableName = getTableName(entityManager, jpaEntityInfo.getJavaType());
    EntityType<T> entityType = entityManager.getMetamodel().entity(jpaEntityInfo.getJavaType());

    StringBuilder sql = getSingleNameResultSql(tableName, entityType);
    Query query = entityManager.createNativeQuery(sql.toString());
    query.setParameter("id", id);
    return (String) query.getSingleResult();
  }


  @Override
  public List<String> findNameByIdIn(Collection<ID> ids) {
    String tableName = getTableName(entityManager, jpaEntityInfo.getJavaType());
    EntityType<T> entityType = entityManager.getMetamodel().entity(jpaEntityInfo.getJavaType());

    StringBuilder sql = getNameResultSql(tableName, entityType);
    Query query = entityManager.createNativeQuery(sql.toString());
    query.setParameter("ids", ids);
    return (List<String>) query.getResultList();
  }

  @Override
  public List<ID> findIdByIdIn(Collection<ID> ids) {
    String tableName = getTableName(entityManager, jpaEntityInfo.getJavaType());
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
  public <T, V> List<V> findProjectionByFilters(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters) {
    return this.findProjectionByFilters(entityClass, viewClass,
        new GenericSpecification<>(filters));
  }

  /**
   * Executes a dynamic projection query with given filters
   *
   * @param entityClass   JPA entity class (must be managed)
   * @param viewClass     DTO/view class with matching constructor
   * @param specification optional query filters
   * @return list of projected view objects
   * @throws IllegalArgumentException for invalid field mapping
   */
  @Override
  public <T, V> List<V> findProjectionByFilters(
      Class<T> entityClass, Class<V> viewClass, @Nullable GenericSpecification<T> specification) {
    return findProjectionByFilters(entityClass, viewClass, specification, (Sort) null);
  }

  @Override
  public <T, V> List<V> findProjectionByFilters(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, Sort sort) {
    return this.findProjectionByFilters(entityClass, viewClass, new GenericSpecification<>(filters),
        sort);
  }

  @Override
  public <T, V> List<V> findProjectionByFilters(Class<T> entityClass, Class<V> viewClass,
      GenericSpecification<T> spec, Sort sort) {
    CriteriaQuery<V> query = getCriteriaQuery(entityClass, viewClass, spec, sort);
    return entityManager.createQuery(query).getResultList();
  }

  @Override
  public <T, V> Page<V> findProjectionByFilters(Class<T> entityClass, Class<V> viewClass,
      GenericSpecification<T> specification, Pageable pageable) {
    return findProjectionByFilters(entityClass, viewClass, specification, pageable, null);
  }

  @Override
  public <T, V> Page<V> findProjectionByFilters(Class<T> entityClass, Class<V> viewClass,
      GenericSpecification<T> spe, Pageable pageable, Sort sort) {
    CriteriaQuery<V> query = getCriteriaQuery(entityClass, viewClass, spe, sort);

    TypedQuery<V> typedQuery = entityManager.createQuery(query);
    if (pageable.isUnpaged()) {
      return new PageImpl<>(typedQuery.getResultList());
    }

    if (pageable.isPaged()) {
      typedQuery.setFirstResult(PageableUtils.getOffsetAsInteger(pageable));
      typedQuery.setMaxResults(pageable.getPageSize());
    }

    return getPage(typedQuery.getResultList(), pageable, () -> nonNull(spe)
        ? count(new GenericSpecification<>(spe.getCriteria(), spe.isDistinct())) : count());
  }

  @Override
  public long countAllByFilters(Set<SearchCriteria> filters) {
    return count(new GenericSpecification<>(filters));
  }

  @Override
  public <T, V> V sumByFilters(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String sumByField) {
    return sumByFilters(entityClass, viewClass, filters, sumByField, null);
  }

  @Override
  public <T, V> List<V> countByFiltersAndGroup(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String countByField) {
    return countByFiltersAndGroup(entityClass, viewClass, filters, groupByField, countByField,
        null);
  }

  @Override
  public <T, V> List<V> sumByFiltersAndGroup(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String sumByField) {
    return sumByFiltersAndGroup(entityClass, viewClass, filters, groupByField, sumByField, null);
  }

  @Override
  public <T, V> List<V> countByFiltersAndGroup(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String countByField,
      String countByFieldAllis) {

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<V> query = cb.createQuery(viewClass);
    Root<T> entity = query.from(entityClass);

    // Select clause
    Path<?> allisPath = entity.get(countByField);
    allisPath.alias(nullSafe(countByFieldAllis, "total"));
    query.select(cb.construct(viewClass, entity.get(groupByField), cb.count(allisPath)));

    // Where clause with dynamic filters
    query.where(new GenericSpecification<T>(filters).toPredicate(entity, query, cb));

    // Group By clause
    query.groupBy(entity.get(groupByField));
    return entityManager.createQuery(query).getResultList();
  }

  @Override
  public <T, V> V sumByFilters(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String sumByField, String sumByFieldAllis) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<V> cq = cb.createQuery(viewClass);
    Root<T> entity = cq.from(entityClass);

    // Select clause
    Path allisPath = entity.get(sumByField);
    allisPath.alias(nullSafe(sumByFieldAllis, "total"));
    cq.select(cb.construct(viewClass, cb.sum(allisPath)));

    // Where clause with dynamic filters
    cq.where(new GenericSpecification<T>(filters).toPredicate(entity, cq, cb));
    List<V> list = entityManager.createQuery(cq).getResultList();
    return isEmpty(list) ? null : list.get(0);
  }

  @Override
  public <T, V> List<V> sumByFiltersAndGroup(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String sumByField,
      String sumByFieldAllis) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<V> cq = cb.createQuery(viewClass);
    Root<T> entity = cq.from(entityClass);

    // Select clause
    Path allisPath = entity.get(sumByField);
    allisPath.alias(nullSafe(sumByFieldAllis, "total"));
    cq.select(cb.construct(viewClass, entity.get(groupByField), allisPath));

    // Where clause with dynamic filters
    cq.where(new GenericSpecification<T>(filters).toPredicate(entity, cq, cb));

    // Group By clause
    cq.groupBy(entity.get(groupByField));
    return entityManager.createQuery(cq).getResultList();
  }

  private @NotNull StringBuilder getSingleNameResultSql(String tableName,
      EntityType<T> entityType) {
    StringBuilder sql = new StringBuilder();
    if (isNotEmpty(resourceName)) {
      sql.append("SELECT ").append(resourceName).append(" FROM ")
          .append(tableName).append(" WHERE id = :id");
    } else if (hasAttribute(entityType, DEFAULT_RESOURCE_NAME)) {
      sql.append("SELECT name FROM ").append(tableName).append(" WHERE id = :id");
    } else {
      throw new IllegalStateException("The resource name was not found in the entity:"
          + jpaEntityInfo.getEntityName());
    }
    return sql;
  }

  private @NotNull StringBuilder getNameResultSql(String tableName, EntityType<T> entityType) {
    StringBuilder sql = new StringBuilder();
    if (isNotEmpty(resourceName)) {
      sql.append("SELECT ").append(resourceName).append(" FROM ")
          .append(tableName).append(" WHERE id in :ids");
    } else if (hasAttribute(entityType, DEFAULT_RESOURCE_NAME)) {
      sql.append("SELECT name FROM ").append(tableName).append(" WHERE id in :ids");
    } else {
      throw new IllegalStateException("The resource name was not found in the entity:"
          + jpaEntityInfo.getEntityName());
    }
    return sql;
  }

  private <T, V> @NotNull CriteriaQuery<V> getCriteriaQuery(Class<T> entityClass,
      Class<V> viewClass, GenericSpecification<T> specification, Sort sort) {
    validateEntityClass(entityClass);
    List<String> fields = getValidatedFields(entityClass, viewClass);

    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<V> query = cb.createQuery(viewClass);
    Root<T> root = query.from(entityClass);

    // Build selection arguments
    List<Selection<?>> selections = fields.stream().map(root::get).collect(Collectors.toList());

    // Construct view class
    try {
      query.select(cb.construct(viewClass, selections.toArray(new Selection[0])));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
          "View class constructor mismatch. Required parameters: " + fields, e);
    }

    if (nonNull(sort) && sort.isSorted()) {
      query.orderBy(QueryUtils.toOrders(sort, root, cb));
    }

    // Apply specification predicates
    if (specification != null) {
      Predicate predicate = specification.toPredicate(root, query, cb);
      query.where(predicate);
    }
    return query;
  }

  /**
   * Verify entity class is JPA managed
   */
  private <T> void validateEntityClass(Class<T> entityClass) {
    if (entityManager.getMetamodel().getEntities().stream()
        .noneMatch(e -> e.getJavaType().equals(entityClass))) {
      throw new IllegalArgumentException("Class " + entityClass.getSimpleName()
          + " is not a JPA managed entity");
    }
    if (entityClass.isInterface()) {
      throw new IllegalArgumentException(
          "The entityClass must be a class, not an interface or annotation type.");
    }
  }

  /**
   * Validate view class fields against entity attributes
   */
  private <T, V> List<String> getValidatedFields(Class<T> entityClass, Class<V> viewClass) {
    List<String> entityFields = entityManager.getMetamodel()
        .entity(entityClass)
        .getAttributes().stream()
        .map(Attribute::getName).toList();

    List<String> viewFields = BeanFieldUtils.getPropertyNames(viewClass);

    List<String> invalidFields = viewFields.stream()
        .filter(f -> !entityFields.contains(f)).toList();

    if (!invalidFields.isEmpty()) {
      throw new IllegalArgumentException("Invalid projection fields: " + invalidFields
          + " in entity: " + entityClass.getSimpleName());
    }
    return viewFields;
  }

}
