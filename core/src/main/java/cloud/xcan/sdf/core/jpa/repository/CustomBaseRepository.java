package cloud.xcan.sdf.core.jpa.repository;

import cloud.xcan.sdf.api.search.SearchCriteria;
import java.util.Set;
import java.util.function.Function;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomBaseRepository<T> {

  default Page<T> find(Set<SearchCriteria> criterias, Pageable pageable,
      Class<T> mainClass, Function<? super Object[], T> mapper, String[] fifs) {
    return find0(criterias, pageable, mainClass, mapper, null, fifs);
  }

  default Page<T> find(Set<SearchCriteria> criterias, Pageable pageable,
      Class<T> mainClass, Function<? super Object[], T> mapper,
      Object[] params, String[] fifs) {
    return find0(criterias, pageable, mainClass, mapper, params, fifs);
  }

  default Page<T> find(Set<SearchCriteria> criterias, Pageable pageable, Class<T> mainClass,
      String[] fifs) {
    return find0(criterias, pageable, mainClass, null, null, fifs);
  }

  default Page<T> find(Set<SearchCriteria> criterias, Pageable pageable, Class<T> mainClass,
      Object[] params, String[] fifs) {
    return find0(criterias, pageable, mainClass, null, params, fifs);
  }

  Page<T> find0(Set<SearchCriteria> criterias, Pageable pageable, Class<T> mainClass,
      Function<? super Object[], T> mapper, Object[] params, String[] fifs);

  default StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... fifs) {
    return new StringBuilder();
  }

  default StringBuilder getCriteriaAliasCondition(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, String alias, SearchMode mode, Boolean notDeleted,
      String... fifs) {
    return new StringBuilder();
  }

  default SearchMode getSearchMode() {
    return SearchMode.LIKE;
  }

  default String getMatchFields(){
    return "";
  }
}
