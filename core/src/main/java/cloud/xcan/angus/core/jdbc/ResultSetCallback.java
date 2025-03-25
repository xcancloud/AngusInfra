package cloud.xcan.angus.core.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Callback function, that receives ResultSet.<br> I need to implement this interface because
 * ResultSetCallback accepts SQLException.<br> It makes easier to use JDBCUtils.
 *
 * @param <T>
 */
@FunctionalInterface
public interface ResultSetCallback<T> {

  T call(ResultSet rs) throws SQLException;
}
