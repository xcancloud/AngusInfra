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

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

  Iterable<T> batchInsert(Iterable<T> var1);

  void batchInsert0(Iterable<T> var1);

  Iterable<T> batchUpdate(Iterable<T> var1);

  String findNameById(ID id);

  List<String> findNameByIdIn(Collection<ID> ids);

  List<ID> findIdByIdIn(Collection<ID> ids);

  List<T> findAllByFilters(Set<SearchCriteria> filters);

  List<T> findAllByFilters(Set<SearchCriteria> filters, Sort var2);

  <T, V> List<V> findProjectionByFilters(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters);

  <T, V> List<V> findProjectionByFilters(Class<T> entityClass, Class<V> viewClass,
      GenericSpecification<T> specification);

  <T, V> List<V> findProjectionByFilters(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, Sort var2);

  <T, V> List<V> findProjectionByFilters(Class<T> entityClass, Class<V> viewClass,
      GenericSpecification<T> specification, Sort sort);

  <T2, V> Page<V> findProjectionByFilters(Class<T2> entityClass, Class<V> viewClass,
      GenericSpecification<T2> specification, Pageable pageable);

  <T2, V> Page<V> findProjectionByFilters(Class<T2> entityClass, Class<V> viewClass,
      GenericSpecification<T2> specification, Pageable pageable, Sort sort);

  long countAllByFilters(Set<SearchCriteria> filters);

  <T, V> List<V> countByFiltersAndGroup(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String countByField,
      String countByFieldAllis);

  <T, V> List<V> countByFiltersAndGroup(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String countByField);

  <T, V> V sumByFilters(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String sumByField);

  <T, V> List<V> sumByFiltersAndGroup(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String sumByField);

  <T, V> V sumByFilters(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String sumByField, String sumByFieldAllis);

  <T, V> List<V> sumByFiltersAndGroup(Class<T> entityClass, Class<V> viewClass,
      Set<SearchCriteria> filters, String groupByField, String sumByField,
      String sumByFieldAllis);
}
