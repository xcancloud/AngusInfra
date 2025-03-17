package cloud.xcan.sdf.core.jdbc;

import java.io.Closeable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

public class ResultSetIterator<T> implements Closeable, Iterator<T> {

  private final ResultSet resultSet;
  private final String query;
  private final List<Object> params;
  private final ResultSetCallback<T> callback;
  private boolean loaded;
  private boolean hasNext;

  public ResultSetIterator(ResultSet resultSet, String query, List<Object> params,
      ResultSetCallback<T> callback) {
    this.resultSet = resultSet;
    this.query = query;
    this.params = params;
    this.callback = callback;
  }

  @Override
  public void close() {
    if (resultSet != null) {
      try {
        resultSet.close();
      } catch (SQLException e) {
        throw new UncheckedFullSQLException(e);
      }
    }
  }

  @Override
  public boolean hasNext() {
    try {
      if (!this.loaded) {
        this.hasNext = this.resultSet.next();
        this.loaded = true;
      }
      return this.hasNext;
    } catch (SQLException e) {
      throw new UncheckedFullSQLException(e, query, params);
    }
  }

  @Override
  public T next() {
    try {
      if (!this.loaded) {
        this.hasNext = this.resultSet.next();
      }
      T retval = callback.call(this.resultSet);
      this.loaded = false;
      return retval;
    } catch (SQLException e) {
      throw new UncheckedFullSQLException(e, query, params);
    }
  }
}
