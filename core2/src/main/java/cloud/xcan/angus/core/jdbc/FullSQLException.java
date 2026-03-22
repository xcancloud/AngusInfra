package cloud.xcan.angus.core.jdbc;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Rich SQL exception.
 */
@Slf4j
public class FullSQLException extends Exception {

  private final String sql;
  private final List<Object> params;

  public FullSQLException(SQLException ex, String sql) {
    super("SQL Exception: " + ex.getMessage() + "\n\t:" + sql, ex);
    log.error("SQLException: {} {}", ex.getMessage(), sql);
    this.sql = sql;
    this.params = null;
  }

  public FullSQLException(SQLException ex, String sql, List<Object> params) {
    super("SQL Exception: " + ex.getMessage() + "\n\t:" + sql + "(" + params.toString() + ")", ex);
    log.error("SQLException: {} {} {}", ex.getMessage(), sql, params.toString());
    this.sql = sql;
    this.params = params;
  }

  public FullSQLException(final SQLException e) {
    this(e, "", Collections.emptyList());
  }

  public String getSql() {
    return sql;
  }

  public List<Object> getParams() {
    return params;
  }

}
