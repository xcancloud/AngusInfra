package cloud.xcan.angus.sharding.table;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShardTableNameUtilsTest {

  @Test
  void buildName_withShardKeyOnly() {
    assertThat(ShardTableNameUtils.buildName("exec_sample", 100L))
        .isEqualTo("exec_sample-100");
  }

  @Test
  void buildName_withShardKeyAndTableIndex() {
    assertThat(ShardTableNameUtils.buildName("exec_sample", 100L, 3L))
        .isEqualTo("exec_sample-100-3");
  }

  @Test
  void buildName_zeroShardKey() {
    assertThat(ShardTableNameUtils.buildName("node_usage", 0L)).isEqualTo("node_usage-0");
  }

  @Test
  void buildName_zeroTableIndex() {
    assertThat(ShardTableNameUtils.buildName("node_usage", 5L, 0L))
        .isEqualTo("node_usage-5-0");
  }

  @Test
  void parseTemplateName_extractsBeforeFirstSeparator() {
    assertThat(ShardTableNameUtils.parseTemplateName("exec_sample-100-3"))
        .isEqualTo("exec_sample");
  }

  @Test
  void parseTemplateName_noSeparatorReturnsWholeString() {
    assertThat(ShardTableNameUtils.parseTemplateName("exec_sample")).isEqualTo("exec_sample");
  }

  @Test
  void parseTemplateName_singleHyphenAtStart() {
    assertThat(ShardTableNameUtils.parseTemplateName("-100")).isEmpty();
  }

  @Test
  void separator_isHyphen() {
    assertThat(ShardTableNameUtils.SEPARATOR).isEqualTo("-");
  }
}
