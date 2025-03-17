package cloud.xcan.sdf.core.jdbc;

import static cloud.xcan.sdf.core.jdbc.JDBCUtilsTest.dbpassword;
import static cloud.xcan.sdf.core.jdbc.JDBCUtilsTest.dburl;
import static cloud.xcan.sdf.core.jdbc.JDBCUtilsTest.dbuser;
import static junit.framework.TestCase.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UncheckedFullSQLExceptionTest {

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
  public void testMessage() {
    String sql = "SELECT * FROM unknownTableName";
    try {
      try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.execute();
      }
    } catch (SQLException e) {
      final UncheckedFullSQLException richSQLException = new UncheckedFullSQLException(e, sql,
          Collections.emptyList());
      assertTrue("'" + richSQLException.getMessage() + "' contains query",
          richSQLException.getMessage().contains(sql));
      assertTrue(richSQLException.getCause() instanceof FullSQLException);
    }
  }

  @Test(expected = UncheckedFullSQLException.class)
  public void testUnchecked() {
    String sql = "SELECT * FROM unknownTableName";
    try {
      try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        preparedStatement.execute();
      }
    } catch (SQLException e) {
      final UncheckedFullSQLException richSQLException = new UncheckedFullSQLException(e, sql,
          Collections.emptyList());
      throw richSQLException;
    }
  }
}
