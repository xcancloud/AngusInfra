package cloud.xcan.angus.sharding.table;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SqlTableMatcherTest {

  // ── matchTableToken ──────────────────────────────────────────────────────

  @Test
  void matchTableToken_selectStatement() {
    assertThat(SqlTableMatcher.matchTableToken("select * from exec_sample where id = 1"))
        .isEqualTo("exec_sample");
  }

  @Test
  void matchTableToken_insertStatement() {
    assertThat(SqlTableMatcher.matchTableToken("insert into node_usage (col1) values (1)"))
        .isEqualTo("node_usage");
  }

  @Test
  void matchTableToken_updateStatement() {
    assertThat(SqlTableMatcher.matchTableToken("update exec_sample set col=1 where id=2"))
        .isEqualTo("exec_sample");
  }

  @Test
  void matchTableToken_deleteStatement() {
    assertThat(SqlTableMatcher.matchTableToken("delete from jvm_service_usage where id=1"))
        .isEqualTo("jvm_service_usage");
  }

  @Test
  void matchTableToken_caseInsensitive() {
    assertThat(SqlTableMatcher.matchTableToken("SELECT * FROM MyTable"))
        .isEqualTo("MyTable");
  }

  @Test
  void matchTableToken_nullReturnsNull() {
    assertThat(SqlTableMatcher.matchTableToken(null)).isNull();
  }

  @Test
  void matchTableToken_emptyStringReturnsNull() {
    assertThat(SqlTableMatcher.matchTableToken("")).isNull();
  }

  @Test
  void matchTableToken_blankStringReturnsNull() {
    assertThat(SqlTableMatcher.matchTableToken("   ")).isNull();
  }

  @Test
  void matchTableToken_unknownSqlReturnsNull() {
    assertThat(SqlTableMatcher.matchTableToken("MERGE INTO table1 USING table2")).isNull();
  }

  @Test
  void matchTableToken_backtickQuotedTableName() {
    assertThat(SqlTableMatcher.matchTableToken("select * from `exec_sample`"))
        .isEqualTo("`exec_sample`");
  }

  // ── stripQuotes ──────────────────────────────────────────────────────────

  @Test
  void stripQuotes_backtickQuoted() {
    assertThat(SqlTableMatcher.stripQuotes("`my_table`")).isEqualTo("my_table");
  }

  @Test
  void stripQuotes_doubleQuoted() {
    assertThat(SqlTableMatcher.stripQuotes("\"my_table\"")).isEqualTo("my_table");
  }

  @Test
  void stripQuotes_unquoted() {
    assertThat(SqlTableMatcher.stripQuotes("my_table")).isEqualTo("my_table");
  }

  @Test
  void stripQuotes_nullReturnsNull() {
    assertThat(SqlTableMatcher.stripQuotes(null)).isNull();
  }

  @Test
  void stripQuotes_singleCharReturnsAsIs() {
    assertThat(SqlTableMatcher.stripQuotes("a")).isEqualTo("a");
  }

  @Test
  void stripQuotes_mismatchedQuotesNotStripped() {
    assertThat(SqlTableMatcher.stripQuotes("`table\"")).isEqualTo("`table\"");
  }

  // ── detectQuoteChar ──────────────────────────────────────────────────────

  @Test
  void detectQuoteChar_backtick() {
    assertThat(SqlTableMatcher.detectQuoteChar("`table`")).isEqualTo("`");
  }

  @Test
  void detectQuoteChar_doubleQuote() {
    assertThat(SqlTableMatcher.detectQuoteChar("\"table\"")).isEqualTo("\"");
  }

  @Test
  void detectQuoteChar_noQuoteDefaultsToBacktick() {
    assertThat(SqlTableMatcher.detectQuoteChar("table")).isEqualTo("`");
  }

  @Test
  void detectQuoteChar_nullDefaultsToBacktick() {
    assertThat(SqlTableMatcher.detectQuoteChar(null)).isEqualTo("`");
  }

  @Test
  void detectQuoteChar_emptyDefaultsToBacktick() {
    assertThat(SqlTableMatcher.detectQuoteChar("")).isEqualTo("`");
  }
}
