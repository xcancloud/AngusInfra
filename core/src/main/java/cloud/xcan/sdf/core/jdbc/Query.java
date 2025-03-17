package cloud.xcan.sdf.core.jdbc;

import java.util.Collections;
import java.util.List;
import lombok.ToString;

/**
 * This class represents SQL query and parameters.
 */
@ToString
public class Query {

  private final String sql;
  private final List<Object> params;

  /**
   * Create new instance.
   */
  public Query(final String sql, final List<Object> params) {
    this.sql = sql;
    this.params = Collections.unmodifiableList(params);
  }

  /**
   * Get SQL string.
   */
  public String getSQL() {
    return sql;
  }

  /**
   * Get parameters.
   */
  public List<Object> getParameters() {
    return params;
  }
}
