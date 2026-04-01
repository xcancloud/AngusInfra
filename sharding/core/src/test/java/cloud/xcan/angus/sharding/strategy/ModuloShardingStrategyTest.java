package cloud.xcan.angus.sharding.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ModuloShardingStrategyTest {

  private final ModuloShardingStrategy strategy = new ModuloShardingStrategy();

  // ── computeDbIndex ──────────────────────────────────────────────────────

  @Test
  void computeDbIndex_singleShard_alwaysZero() {
    assertThat(strategy.computeDbIndex(0, 1)).isEqualTo(0);
    assertThat(strategy.computeDbIndex(999, 1)).isEqualTo(0);
  }

  @Test
  void computeDbIndex_distributesByModulo() {
    assertThat(strategy.computeDbIndex(0, 3)).isEqualTo(0);
    assertThat(strategy.computeDbIndex(1, 3)).isEqualTo(1);
    assertThat(strategy.computeDbIndex(2, 3)).isEqualTo(2);
    assertThat(strategy.computeDbIndex(3, 3)).isEqualTo(0);
  }

  @Test
  void computeDbIndex_negativeKeyUsesAbsValue() {
    int idx = strategy.computeDbIndex(-7, 3);
    assertThat(idx).isBetween(0, 2);
  }

  @Test
  void computeDbIndex_zeroShardCount_throwsException() {
    assertThatThrownBy(() -> strategy.computeDbIndex(1L, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("shardDbCount");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -10})
  void computeDbIndex_negativeShardCount_throwsException(int count) {
    assertThatThrownBy(() -> strategy.computeDbIndex(1L, count))
        .isInstanceOf(IllegalArgumentException.class);
  }

  // ── computeTableIndex ────────────────────────────────────────────────────

  @Test
  void computeTableIndex_singleShard_alwaysZero() {
    assertThat(strategy.computeTableIndex(12345, 1)).isEqualTo(0);
  }

  @Test
  void computeTableIndex_distributesByModulo() {
    assertThat(strategy.computeTableIndex(5, 5)).isEqualTo(0);
    assertThat(strategy.computeTableIndex(6, 5)).isEqualTo(1);
  }

  @Test
  void computeTableIndex_zeroShardCount_throwsException() {
    assertThatThrownBy(() -> strategy.computeTableIndex(1L, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("shardTableCount");
  }

  @Test
  void computeTableIndex_negativeKeyUsesAbsValue() {
    int idx = strategy.computeTableIndex(-4, 3);
    assertThat(idx).isBetween(0, 2);
  }
}
