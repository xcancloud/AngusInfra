package cloud.xcan.sdf.core.jpa.repository;

import cloud.xcan.sdf.api.search.SearchCriteria;
import java.util.Set;
import org.hibernate.persister.entity.SingleTableEntityPersister;

public class SimpleSearchRepository<T> extends AbstractSearchRepository<T> {

  @Override
  public StringBuilder getSqlTemplate(SingleTableEntityPersister step,
      Set<SearchCriteria> criterias, Object[] params, String... fifs) {
    StringBuilder sql = new StringBuilder();
    String alias = "xc";
    sql.append("SELECT %s FROM ").append(step.getTableName()).append(" ").append(alias)
        .append(" WHERE 1=1")
        .append(getCriteriaAliasCondition(step, criterias, alias, SearchMode.MATCH, true, fifs));
    return sql;
  }

  @Override
  public SearchMode getSearchMode() {
    return SearchMode.MATCH;
  }
}