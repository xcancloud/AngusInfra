package cloud.xcan.angus.sharding.table;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to match and extract table names from SQL statements.
 */
public final class SqlTableMatcher {

  private SqlTableMatcher() {
  }

  static final Pattern SELECT_PATTERN = Pattern.compile(
      "select\\s.+?from\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
  static final Pattern INSERT_PATTERN = Pattern.compile(
      "insert\\s+into\\s+(\\S+)", Pattern.CASE_INSENSITIVE);
  static final Pattern UPDATE_PATTERN = Pattern.compile(
      "update\\s+(\\S+)\\s+set\\s", Pattern.CASE_INSENSITIVE);
  static final Pattern DELETE_PATTERN = Pattern.compile(
      "delete\\s+from\\s+(\\S+)", Pattern.CASE_INSENSITIVE);

  /**
   * Extract the raw table token from the SQL statement, including any quote characters.
   *
   * @param sql the SQL statement
   * @return the raw table token (e.g. {@code `exec_sample`}), or null if not found
   */
  public static String matchTableToken(String sql) {
    if (sql == null || sql.isBlank()) {
      return null;
    }
    String trimmed = sql.trim();
    String prefix = trimmed.substring(0, Math.min(trimmed.length(), 7)).toUpperCase();
    Matcher matcher;
    if (prefix.startsWith("SELECT")) {
      matcher = SELECT_PATTERN.matcher(trimmed);
    } else if (prefix.startsWith("INSERT")) {
      matcher = INSERT_PATTERN.matcher(trimmed);
    } else if (prefix.startsWith("UPDATE")) {
      matcher = UPDATE_PATTERN.matcher(trimmed);
    } else if (prefix.startsWith("DELETE")) {
      matcher = DELETE_PATTERN.matcher(trimmed);
    } else {
      return null;
    }
    return matcher.find() ? matcher.group(1) : null;
  }

  /**
   * Strip surrounding backticks or double-quotes from a table name token.
   */
  public static String stripQuotes(String name) {
    if (name == null || name.length() < 2) {
      return name;
    }
    char first = name.charAt(0);
    if ((first == '`' || first == '"') && name.charAt(name.length() - 1) == first) {
      return name.substring(1, name.length() - 1);
    }
    return name;
  }

  /**
   * Detect the quote character used for a table token.
   *
   * @return the quote character as a string, or backtick as default
   */
  public static String detectQuoteChar(String rawToken) {
    if (rawToken != null && !rawToken.isEmpty()) {
      char first = rawToken.charAt(0);
      if (first == '`' || first == '"') {
        return String.valueOf(first);
      }
    }
    return "`";
  }
}
