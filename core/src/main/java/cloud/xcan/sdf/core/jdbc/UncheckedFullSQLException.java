package cloud.xcan.sdf.core.jdbc;

import java.sql.SQLException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UncheckedFullSQLException extends RuntimeException {

  private final FullSQLException exception;

  public UncheckedFullSQLException(SQLException ex, String sql, List<Object> params) {
    this(new FullSQLException(ex, sql, params));
  }

  public UncheckedFullSQLException(FullSQLException e) {
    super(e.getMessage(), e);
    this.exception = e;
  }

  public UncheckedFullSQLException(SQLException e) {
    this(new FullSQLException(e));
  }

  public String getSql() {
    return this.exception.getSql();
  }

  public List<Object> getParams() {
    return this.exception.getParams();
  }

}
