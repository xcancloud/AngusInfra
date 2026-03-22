package cloud.xcan.angus.core.jdbc;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.SQLParameterReplacer.replaceParameters;

import cloud.xcan.angus.spec.utils.FileUtils;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Utility functions for JDBC.
 */
public class JDBCUtils {

  /**
   * Execute query, and return stream.
   * <B>You must call .close() after using.</B> I recommend to use try-with-resources.
   *
   * @param connection JDBC connection
   * @param query      SQL query
   * @param callback   callback function. It will call every row.
   * @return Stream
   * @throws FullSQLException
   */
  public static <R> Stream<R> executeQueryStream(final Connection connection,
      final Query query, final ResultSetCallback<R> callback) throws FullSQLException {
    return JDBCUtils.executeQueryStream(connection, query.getSQL(), query.getParameters(),
        callback);
  }

  /**
   * Execute query, and return stream.
   * <B>You must call .close() after using.</B> I recommend to use try-with-resources.
   *
   * @param connection JDBC connection
   * @param sql        SQL query
   * @param params     parameters
   * @param callback   callback function. It will call every row.
   * @return Stream
   * @throws FullSQLException
   */
  public static <R> Stream<R> executeQueryStream(final Connection connection,
      final String sql, final List<Object> params, final ResultSetCallback<R> callback)
      throws FullSQLException {
    try {
      final PreparedStatement ps = connection.prepareStatement(sql);
      JDBCUtils.fillPreparedStatementParams(ps, params);
      final ResultSet rs = ps.executeQuery();
      final ResultSetIterator<R> iterator = new ResultSetIterator<>(rs, sql, params, callback);
      Spliterator<R> spliterator = Spliterators.spliteratorUnknownSize(
          iterator, Spliterator.NONNULL | Spliterator.ORDERED | Spliterator.SIZED);
      final Stream<R> stream = StreamSupport.stream(spliterator, false);
      stream.onClose(() -> {
        try {
          ps.close();
        } catch (SQLException e) {
          throw new UncheckedFullSQLException(e);
        }
        try {
          rs.close();
        } catch (SQLException e) {
          throw new UncheckedFullSQLException(e);
        }
      });
      return stream;
    } catch (final SQLException ex) {
      throw new FullSQLException(ex, sql, params);
    }
  }

  /**
   * Execute query with callback.
   *
   * @param connection JDBC connection
   * @param query      SQL query
   * @param callback   callback function. It will call every row.
   * @return Generated value from the callback
   * @throws FullSQLException
   */
  public static <R> R executeQuery(final Connection connection, final Query query,
      final ResultSetCallback<R> callback) throws FullSQLException {
    return JDBCUtils.executeQuery(connection, query.getSQL(), query.getParameters(), callback);
  }

  /**
   * Execute query with callback.
   *
   * @param connection JDBC connection
   * @param sql        SQL query
   * @param params     parameters
   * @param callback   callback function. It will call every row.
   * @return Generated value from the callback
   * @throws FullSQLException
   */
  public static <R> R executeQuery(final Connection connection, final String sql,
      final List<Object> params, final ResultSetCallback<R> callback) throws FullSQLException {
    try (final PreparedStatement ps = connection.prepareStatement(sql)) {
      JDBCUtils.fillPreparedStatementParams(ps, params);
      try (final ResultSet rs = ps.executeQuery()) {
        return callback.call(rs);
      }
    } catch (final SQLException ex) {
      throw new FullSQLException(ex, sql, params);
    }
  }

  /**
   * Execute query without callback. This method is useful when calling the SELECT query has side
   * effects, e.g. `SELECT GET_LOCK('hoge', 3)`.
   *
   * @param connection JDBC connection
   * @param sql        SQL query
   * @param params     parameters
   * @throws FullSQLException
   */
  public static void executeQuery(final Connection connection, final String sql,
      final List<Object> params) throws FullSQLException {
    try (final PreparedStatement ps = connection.prepareStatement(sql)) {
      JDBCUtils.fillPreparedStatementParams(ps, params);
      try (final ResultSet rs = ps.executeQuery()) {
      }
    } catch (final SQLException ex) {
      throw new FullSQLException(ex, sql, params);
    }
  }

  /**
   * Execute query without callback. This method returns results as
   * {@code List<Map<String, Object>>}.
   *
   * @param connection JDBC connection
   * @param sql        SQL query
   * @param params     parameters
   * @return Selected rows in list of maps.
   * @throws FullSQLException
   */
  public static List<Map<String, Object>> executeQueryMapList(final Connection connection,
      final String sql, final List<Object> params) throws FullSQLException {
    try (final PreparedStatement ps = connection.prepareStatement(sql)) {
      JDBCUtils.fillPreparedStatementParams(ps, params);
      try (final ResultSet rs = ps.executeQuery()) {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        List<Map<String, Object>> mapList = new ArrayList<>();
        while (rs.next()) {
          Map<String, Object> map = new HashMap<>();
          for (int i = 1; i <= columnCount; i++) {
            String name = metaData.getColumnLabel(i);
            map.put(name, rs.getObject(i));
          }
          mapList.add(map);
        }
        return mapList;
      }
    } catch (final SQLException ex) {
      throw new FullSQLException(ex, sql, params);
    }
  }

  /**
   * [EXPERIMENTAL] Execute a query and map the result to the bean.
   *
   * @param connection JDBC connection
   * @param sql        SQL query
   * @param params     parameters
   * @return Selected rows in list of beans.
   * @throws FullSQLException
   * @throws IntrospectionException
   * @throws IllegalAccessException
   * @throws InstantiationException
   * @throws InvocationTargetException
   * @throws IllegalArgumentException
   */
  public static <T> List<T> executeQueryForBean(final Connection connection,
      final String sql, final List<Object> params, final Class<T> valueClass)
      throws FullSQLException, IntrospectionException, InstantiationException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    // Get bean information first.
    BeanInfo beanInfo = Introspector.getBeanInfo(valueClass, Object.class);
    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

    try (final PreparedStatement ps = connection.prepareStatement(sql)) {
      JDBCUtils.fillPreparedStatementParams(ps, params);
      try (final ResultSet rs = ps.executeQuery()) {
        List<T> valueList = new ArrayList<>();
        while (rs.next()) {
          T row = valueClass.newInstance();
          for (PropertyDescriptor prop : propertyDescriptors) {
            Method writeMethod = prop.getWriteMethod();
            if (writeMethod != null) {
              String name = prop.getName();
              Object value = rs.getObject(name);
              if (value != null) {
                writeMethod.invoke(row, value);
              }
            }
          }
          valueList.add(row);
        }
        return valueList;
      }
    } catch (final SQLException ex) {
      throw new FullSQLException(ex, sql, params);
    }
  }

  /**
   * Execute query.
   *
   * @param connection JDBC connection
   * @param query      SQL query
   * @return Affected rows.
   * @throws FullSQLException
   */
  public static int executeUpdate(final Connection connection, final Query query)
      throws FullSQLException {
    return JDBCUtils.executeUpdate(connection, query.getSQL(), query.getParameters());
  }

  /**
   * Execute query.
   *
   * @param connection JDBC connection
   * @param sql        SQL query
   * @param params     parameters
   * @return Affected rows.
   * @throws FullSQLException
   */
  public static int executeUpdate(final Connection connection, final String sql,
      final List<Object> params) throws FullSQLException {
    try (final PreparedStatement ps = connection.prepareStatement(sql)) {
      JDBCUtils.fillPreparedStatementParams(ps, params);
      return ps.executeUpdate();
    } catch (final SQLException ex) {
      throw new FullSQLException(ex, sql, params);
    }
  }

  /**
   * Shorthand method.
   *
   * @param connection JDBC connection
   * @param sql        SQL query
   * @return updated num
   * @throws FullSQLException
   */
  public static int executeUpdate(final Connection connection, final String sql)
      throws FullSQLException {
    return JDBCUtils.executeUpdate(connection, sql, Collections.emptyList());
  }

  /**
   * Only execute sql.
   *
   * @param connection JDBC connection
   * @param sql        SQL query
   * @throws FullSQLException
   */
  public static void execute(final Connection connection, final String sql)
      throws FullSQLException {
    try (final Statement stmt = connection.createStatement()) {
      stmt.execute(sql);
    } catch (final SQLException ex) {
      throw new FullSQLException(ex, sql);
    }
  }

  /**
   * Only execute sql script.
   *
   * @param connection JDBC connection
   * @param scriptFile SQL script file
   * @throws FullSQLException
   */
  public static void executeScript(final Connection connection, final String scriptFile)
      throws FullSQLException, FileNotFoundException {
    executeScript(connection, new File(scriptFile));
  }

  /**
   * Only execute sql script.
   *
   * @param connection JDBC connection
   * @param scriptFile SQL script file
   */
  public static void executeScript(final Connection connection, final File scriptFile)
      throws FullSQLException, FileNotFoundException {
    executeScript(connection, FileUtils.readFile(new FileInputStream(scriptFile)), null);
  }

  /**
   * Only execute sql script.
   *
   * @param connection JDBC connection
   * @param sqlScript  SQL script file
   */
  public static void executeScript(final Connection connection, final String sqlScript,
      Map<String, ?> variables) throws FullSQLException {
    String sql0 = null;
    try (BufferedReader reader = new BufferedReader(new StringReader(sqlScript))) {
      StringBuilder sql = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("--")) {
          sql.append(line);
          if (line.trim().endsWith(";")) {
            sql0 = sql.toString();
            if (!sql0.trim().isEmpty()) {
              try (Statement statement = connection.createStatement()) {
                if (isNotEmpty(variables)) {
                  statement.execute(replaceParameters(sql0, variables));
                } else {
                  statement.execute(sql0);
                }
              }
            }
            sql = new StringBuilder();
          }
        }
      }
    } catch (Exception ex) {
      throw new FullSQLException(new SQLException(ex), sql0);
    }
  }


  /**
   * Fill parameters for prepared statement.
   *
   * <pre>
   * <code>JDBCUtils.fillPreparedStatementParams(preparedStatement, ImmutableList.of(1,2,3));</code>
   * </pre>
   *
   * @param preparedStatement Prepared Statement
   * @param params            parameters
   * @throws SQLException
   */
  public static void fillPreparedStatementParams(final PreparedStatement preparedStatement,
      final List<Object> params) throws SQLException {
    for (int i = 0; i < params.size(); ++i) {
      preparedStatement.setObject(i + 1, params.get(i));
    }
  }

  /**
   * Quote SQL identifier. You should get identifierQuoteString from DatabaseMetadata.
   *
   * @param identifier
   * @param identifierQuoteString
   * @return Escaped identifier.
   */
  public static String quoteIdentifier(final String identifier,
      final String identifierQuoteString) {
    return identifierQuoteString + identifier.replace(identifierQuoteString,
        identifierQuoteString + identifierQuoteString) + identifierQuoteString;
  }

  /**
   * Quote SQL identifier.
   *
   * @param identifier
   * @param connection
   * @return
   * @throws SQLException
   */
  public static String quoteIdentifier(final String identifier, final Connection connection)
      throws SQLException {
    if (connection == null) {
      throw new NullPointerException();
    }
    String identifierQuoteString = connection.getMetaData().getIdentifierQuoteString();
    return quoteIdentifier(identifier, identifierQuoteString);
  }

}
