
package cloud.xcan.sdf.core.jpa.repository;

import cloud.xcan.sdf.api.search.SearchCriteria;
import java.util.Collection;
import java.util.List;
import java.util.Set;
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

  <T2, V> List<V> findProjectionByFilters(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters);

  <T2, V> List<V> findProjectionByFilters(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, Sort var2);

  long countAllByFilters(Set<SearchCriteria> filters);

  <T2, V> V sumByFilters(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String sumByField);

  <T2, V> List<V> countByFiltersAndGroup(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String groupByField, String countByField);

  <T2, V> List<V> sumByFiltersAndGroup(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String groupByField, String sumByField);

  <T2, V> V sumByFilters(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String sumByField, String sumByFieldAllis);

  <T2, V> List<V> countByFiltersAndGroup(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String groupByField, String countByField,
      String countByFieldAllis);

  <T2, V> List<V> sumByFiltersAndGroup(Class<T2> entityClz, Class<V> viewClz,
      Set<SearchCriteria> filters, String groupByField, String sumByField,
      String sumByFieldAllis);
}
