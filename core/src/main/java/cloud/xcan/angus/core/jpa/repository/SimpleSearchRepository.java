package cloud.xcan.angus.core.jpa.repository;

import static cloud.xcan.angus.core.jpa.JpaMetadataUtils.getTableName;

import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class SimpleSearchRepository<T> extends AbstractSearchRepository<T> {

  @Override
  public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<T> mainClass,
      Object[] params, String... match) {
    StringBuilder sql = new StringBuilder();
    String alias = "xc";
    sql.append("SELECT %s FROM ").append(getTableName(entityManager, mainClass))
        .append(" ").append(alias).append(" WHERE 1=1")
        .append(getCriteriaAliasCondition(criteria, mainClass, alias,
            SearchMode.MATCH, true, match));
    return sql;
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }
}
