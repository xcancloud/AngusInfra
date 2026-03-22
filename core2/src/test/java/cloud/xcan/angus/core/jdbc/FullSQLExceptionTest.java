package cloud.xcan.angus.core.jdbc;

import static cloud.xcan.angus.core.jdbc.JDBCUtilsTest.dbpassword;
import static cloud.xcan.angus.core.jdbc.JDBCUtilsTest.dburl;
import static cloud.xcan.angus.core.jdbc.JDBCUtilsTest.dbuser;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FullSQLExceptionTest {

  private Connection connection;

  @Before
  public void before() throws ClassNotFoundException, SQLException {
    connection = ConnectionFactory.mysql(dburl, dbuser, dbpassword);
  }

  @After
  public void after() throws SQLException {
    if (connection != null) {
      connection.close();
    }
  }

  @Test
  public void test() {
    String sql = "SELECT * FROM unknownTableName";
    try {
      try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.execute();
      }
    } catch (SQLException e) {
      final FullSQLException richSQLException = new FullSQLException(e, sql,
          Collections.emptyList());
      assertTrue("'" + richSQLException.getMessage() + "' contains query",
          richSQLException.getMessage().contains(sql));
    }
  }

}
