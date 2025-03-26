package cloud.xcan.angus.core.jpa.repository;

import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomBaseRepository<T> {

  default Page<T> find(Set<SearchCriteria> criteria, Pageable pageable,
      Class<T> mainClz, Function<? super Object[], T> mapper, String[] match) {
    return find0(criteria, pageable, mainClz, mapper, null, match);
  }

  default Page<T> find(Set<SearchCriteria> criteria, Pageable pageable,
      Class<T> mainClz, Function<? super Object[], T> mapper,
      Object[] params, String[] match) {
    return find0(criteria, pageable, mainClz, mapper, params, match);
  }

  default Page<T> find(Set<SearchCriteria> criteria, Pageable pageable, Class<T> mainClz,
      String[] match) {
    return find0(criteria, pageable, mainClz, null, null, match);
  }

  default Page<T> find(Set<SearchCriteria> criteria, Pageable pageable, Class<T> mainClz,
      Object[] params, String[] match) {
    return find0(criteria, pageable, mainClz, null, params, match);
  }

  Page<T> find0(Set<SearchCriteria> criteria, Pageable pageable, Class<T> mainClz,
      Function<? super Object[], T> mapper, Object[] params, String[] match);

  default StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<T> mainClz,
      Object[] params, String... match) {
    return new StringBuilder();
  }

  default StringBuilder getCriteriaAliasCondition(Set<SearchCriteria> criteria, Class<T> mainClz,
      String alias, SearchMode mode, Boolean notDeleted, String... match) {
    return new StringBuilder();
  }

  default SearchMode getSearchMode() {
    return SearchMode.LIKE;
  }

  default String getMatchFields() {
    return "";
  }
}
