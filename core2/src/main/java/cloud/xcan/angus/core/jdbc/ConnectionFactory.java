package cloud.xcan.angus.core.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

  public static Connection getConnection(String dbType, String host, int port, String database,
      String username, String password) throws SQLException, ClassNotFoundException {
    if ("Postgres".equalsIgnoreCase(dbType)) {
      return postgres(host, port, database, username, password);
    }
    if ("MySQL".equalsIgnoreCase(dbType)) {
      return mysql(host, port, database, username, password);
    }
    throw new SQLException("Unsupported database " + dbType);
  }

  public static Connection getConnection(String dbType, String url, String username, String passd)
      throws SQLException, ClassNotFoundException {
    if ("Postgres".equalsIgnoreCase(dbType)) {
      return postgres(url, username, passd);
    }
    if ("MySQL".equalsIgnoreCase(dbType)) {
      return mysql(url, username, passd);
    }
    throw new SQLException("Unsupported database " + dbType);
  }

  public static Connection postgres(String host, int port, String database, String username,
      String passd) throws ClassNotFoundException, SQLException {
    String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);
    return postgres(url, username, passd);
  }

  public static Connection postgres(String url, String username, String passd)
      throws ClassNotFoundException, SQLException {
    Class.forName("org.postgresql.Driver");
    return DriverManager.getConnection(url, username, passd);
  }

  public static Connection mysql(String host, int port, String database, String username,
      String passd) throws ClassNotFoundException, SQLException {
    String url = String.format("jdbc:mysql://%s:%s/%s?useUnicode=true&characterEncoding=UTF-8",
        host, port, database);
    return mysql(url, username, passd);
  }

  public static Connection mysql(String url, String username, String passd)
      throws ClassNotFoundException, SQLException {
    Class.forName("com.mysql.cj.jdbc.Driver");
    return DriverManager.getConnection(url, username, passd);
  }
}
