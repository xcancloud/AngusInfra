package cloud.xcan.angus.core.jpa.repository;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * <p>
 * Enhanced base repository interface that extends Spring Data JPA capabilities
 * with custom batch operations, projection queries, and advanced search functionality.
 * This interface provides a comprehensive set of database operations for all entities.
 * </p>
 * 
 * <p>
 * Key features:
 * - Batch insert and update operations for improved performance
 * - Advanced search capabilities with dynamic filtering
 * - Projection support for optimized data retrieval
 * - Aggregation operations (count, sum) with grouping
 * - Type-safe generic operations
 * - Integration with custom search criteria
 * </p>
 * 
 * <p>
 * This interface should be extended by all entity-specific repositories
 * to inherit common database operations. The @NoRepositoryBean annotation
 * prevents Spring Data from creating a concrete implementation of this interface.
 * </p>
 * 
 * <p>
 * Usage example:
 * <pre>
 * public interface UserRepository extends BaseRepository&lt;User, Long&gt; {
 *     // Entity-specific methods can be added here
 * }
 * </pre>
 * </p>
 * 
 * @param <T> the entity type
 * @param <ID> the entity ID type
 * @see JpaRepository
 * @see JpaSpecificationExecutor
 * @see GenericSpecification
 * @see SearchCriteria
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

  /**
   * <p>
   * Performs batch insert operation for multiple entities.
   * This method is optimized for inserting large numbers of entities efficiently.
   * </p>
   * 
   * <p>
   * Benefits of batch insertion:
   * - Reduced database round trips
   * - Improved performance for bulk operations
   * - Transaction efficiency
   * - Memory optimization
   * </p>
   *
   * @param entities the entities to insert
   * @return the inserted entities with generated IDs
   */
  Iterable<T> batchInsert(Iterable<T> entities);

  /**
   * <p>
   * Performs batch insert operation without returning the inserted entities.
   * This method is optimized for scenarios where the returned entities are not needed,
   * providing better memory efficiency.
   * </p>
   *
   * @param entities the entities to insert
   */
  void batchInsert0(Iterable<T> entities);

  /**
   * <p>
   * Performs batch update operation for multiple entities.
   * This method efficiently updates multiple entities in a single database operation.
   * </p>
   *
   * @param entities the entities to update
   * @return the updated entities
   */
  Iterable<T> batchUpdate(Iterable<T> entities);

  /**
   * <p>
   * Retrieves only the name field of an entity by its ID.
   * This method provides optimized data retrieval when only the name is needed.
   * </p>
   *
   * @param id the entity ID
   * @return the name of the entity, or null if not found
   */
  String findNameById(ID id);

  /**
   * <p>
   * Retrieves names of multiple entities by their IDs.
   * This method provides bulk name retrieval with optimized performance.
   * </p>
   *
   * @param ids the collection of entity IDs
   * @return list of entity names in the same order as the input IDs
   */
  List<String> findNameByIdIn(Collection<ID> ids);

  /**
   * <p>
   * Retrieves IDs of entities that exist in the database from the provided ID collection.
   * This method is useful for validating entity existence efficiently.
   * </p>
   *
   * @param ids the collection of IDs to check
   * @return list of existing IDs
   */
  List<ID> findIdByIdIn(Collection<ID> ids);

  /**
   * <p>
   * Finds all entities that match the provided search criteria.
   * This method enables dynamic querying based on flexible search conditions.
   * </p>
   *
   * @param filters the set of search criteria to apply
   * @return list of entities matching the criteria
   */
  List<T> findAllByFilters(Set<SearchCriteria> filters);

  /**
   * <p>
   * Finds all entities that match the provided search criteria with sorting.
   * </p>
   *
   * @param filters the set of search criteria to apply
   * @param sort the sort specification
   * @return sorted list of entities matching the criteria
   */
  List<T> findAllByFilters(Set<SearchCriteria> filters, Sort sort);

  /**
   * <p>
   * Retrieves projected views of entities based on search criteria.
   * This method enables efficient data transfer by selecting only required fields.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the projection view type
   * @param entityClass the entity class to query
   * @param viewClass the projection class defining the desired fields
   * @param filters the search criteria to apply
   * @return list of projected views
   */
  <E, V> List<V> findProjectionByFilters(Class<E> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters);

  /**
   * <p>
   * Retrieves projected views of entities based on generic specification.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the projection view type
   * @param entityClass the entity class to query
   * @param viewClass the projection class defining the desired fields
   * @param specification the generic specification to apply
   * @return list of projected views
   */
  <E, V> List<V> findProjectionByFilters(Class<E> entityClass, Class<V> viewClass,
      GenericSpecification<E> specification);

  /**
   * <p>
   * Retrieves sorted projected views of entities based on search criteria.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the projection view type
   * @param entityClass the entity class to query
   * @param viewClass the projection class defining the desired fields
   * @param filters the search criteria to apply
   * @param sort the sort specification
   * @return sorted list of projected views
   */
  <E, V> List<V> findProjectionByFilters(Class<E> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, Sort sort);

  /**
   * <p>
   * Retrieves sorted projected views of entities based on generic specification.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the projection view type
   * @param entityClass the entity class to query
   * @param viewClass the projection class defining the desired fields
   * @param specification the generic specification to apply
   * @param sort the sort specification
   * @return sorted list of projected views
   */
  <E, V> List<V> findProjectionByFilters(Class<E> entityClass, Class<V> viewClass,
      GenericSpecification<E> specification, Sort sort);

  /**
   * <p>
   * Retrieves paginated projected views of entities based on generic specification.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the projection view type
   * @param entityClass the entity class to query
   * @param viewClass the projection class defining the desired fields
   * @param specification the generic specification to apply
   * @param pageable the pagination information
   * @return page of projected views
   */
  <E, V> Page<V> findProjectionByFilters(Class<E> entityClass, Class<V> viewClass,
      GenericSpecification<E> specification, Pageable pageable);

  /**
   * <p>
   * Retrieves paginated and sorted projected views of entities.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the projection view type
   * @param entityClass the entity class to query
   * @param viewClass the projection class defining the desired fields
   * @param specification the generic specification to apply
   * @param pageable the pagination information
   * @param sort the sort specification
   * @return page of sorted projected views
   */
  <E, V> Page<V> findProjectionByFilters(Class<E> entityClass, Class<V> viewClass,
      GenericSpecification<E> specification, Pageable pageable, Sort sort);

  /**
   * <p>
   * Counts the number of entities that match the provided search criteria.
   * This method provides efficient counting without loading entity data.
   * </p>
   *
   * @param filters the search criteria to apply
   * @return the count of matching entities
   */
  long countAllByFilters(Set<SearchCriteria> filters);

  /**
   * <p>
   * Performs count aggregation with grouping based on search criteria.
   * This method enables analytical queries with custom count fields and aliases.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the result view type
   * @param entityClass the entity class to query
   * @param viewClass the result class for aggregated data
   * @param filters the search criteria to apply
   * @param groupByField the field to group results by
   * @param countByField the field to count
   * @param countByFieldAlias the alias for the count field in results
   * @return list of aggregated count results
   */
  <E, V> List<V> countByFiltersAndGroup(Class<E> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String countByField,
      String countByFieldAlias);

  /**
   * <p>
   * Performs count aggregation with grouping using default count field alias.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the result view type
   * @param entityClass the entity class to query
   * @param viewClass the result class for aggregated data
   * @param filters the search criteria to apply
   * @param groupByField the field to group results by
   * @param countByField the field to count
   * @return list of aggregated count results
   */
  <E, V> List<V> countByFiltersAndGroup(Class<E> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String countByField);

  /**
   * <p>
   * Performs sum aggregation based on search criteria.
   * This method calculates the sum of a numeric field for entities matching the criteria.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the result type
   * @param entityClass the entity class to query
   * @param viewClass the result class for the sum value
   * @param filters the search criteria to apply
   * @param sumByField the numeric field to sum
   * @return the sum result
   */
  <E, V> V sumByFilters(Class<E> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String sumByField);

  /**
   * <p>
   * Performs sum aggregation with grouping based on search criteria.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the result view type
   * @param entityClass the entity class to query
   * @param viewClass the result class for aggregated data
   * @param filters the search criteria to apply
   * @param groupByField the field to group results by
   * @param sumByField the numeric field to sum
   * @return list of aggregated sum results
   */
  <E, V> List<V> sumByFiltersAndGroup(Class<E> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String sumByField);

  /**
   * <p>
   * Performs sum aggregation with custom field alias.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the result type
   * @param entityClass the entity class to query
   * @param viewClass the result class for the sum value
   * @param filters the search criteria to apply
   * @param sumByField the numeric field to sum
   * @param sumByFieldAlias the alias for the sum field in results
   * @return the sum result
   */
  <E, V> V sumByFilters(Class<E> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String sumByField, String sumByFieldAlias);

  /**
   * <p>
   * Performs sum aggregation with grouping and custom field alias.
   * </p>
   *
   * @param <E> the entity type
   * @param <V> the result view type
   * @param entityClass the entity class to query
   * @param viewClass the result class for aggregated data
   * @param filters the search criteria to apply
   * @param groupByField the field to group results by
   * @param sumByField the numeric field to sum
   * @param sumByFieldAlias the alias for the sum field in results
   * @return list of aggregated sum results
   */
  <E, V> List<V> sumByFiltersAndGroup(Class<E> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String sumByField,
      String sumByFieldAlias);
}
