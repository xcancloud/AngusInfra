package cloud.xcan.sdf.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builder class for {@code Query} class.
 */
public class QueryBuilder {

  private final List<Object> parameters;
  private final StringBuilder query;
  private final String identifierQuoteString;

  public QueryBuilder(final String identifierQuoteString) {
    this.parameters = new ArrayList<>();
    this.query = new StringBuilder();
    this.identifierQuoteString = identifierQuoteString;
  }

  public QueryBuilder(final Connection connection) {
    try {
      this.parameters = new ArrayList<>();
      this.query = new StringBuilder();
      this.identifierQuoteString = connection.getMetaData()
          .getIdentifierQuoteString();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public QueryBuilder appendQuery(final String s) {
    this.query.append(s);
    return this;
  }

  public QueryBuilder appendQuery(final long l) {
    this.query.append("" + l);
    return this;
  }

  public QueryBuilder appendIdentifier(final String identifier) {
    this.query.append(JDBCUtils.quoteIdentifier(identifier, identifierQuoteString));
    return this;
  }

  public QueryBuilder append(final Query q) {
    this.query.append(q.getSQL());
    this.parameters.addAll(q.getParameters());
    return this;
  }

  public QueryBuilder addParameter(final Object o) {
    if (o instanceof List) {
      throw new IllegalArgumentException(
          "Do not pass the List to here. You may want to use addParameters().");
    }
    this.parameters.add(o);
    return this;
  }

  public <T> QueryBuilder addParameters(final Collection<T> o) {
    this.parameters.addAll(o);
    return this;
  }

  public Query build() {
    return new Query(new String(this.query), this.parameters);
  }

  public List<Object> getParameters() {
    return parameters;
  }

  public StringBuilder getQuery() {
    return query;
  }

  public QueryBuilder appendQueryAndParameter(String s, Object o) {
    if (o instanceof Collection) {
      throw new IllegalArgumentException(
          "Do not pass the Collection to here. You may want to use appendQueryAndParameters().");
    }
    this.appendQuery(s);
    this.addParameter(o);
    return this;
  }

  public QueryBuilder appendQueryAndParameters(String s, Collection<Object> o) {
    this.appendQuery(s);
    this.addParameters(o);
    return this;
  }

  public QueryBuilder in(Collection<?> objects) {
    this.appendQuery(" IN (" + objects.stream().map(it -> "?")
        .collect(Collectors.joining(",")) + ")");
    this.addParameters(objects);
    return this;
  }

  public QueryBuilder notIn(Collection<?> objects) {
    this.appendQuery(" NOT IN (" + objects.stream().map(it -> "?")
        .collect(Collectors.joining(",")) + ")");
    this.addParameters(objects);
    return this;
  }
}
