package cloud.xcan.angus.sharding.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HashShardingStrategyTest {

  private final HashShardingStrategy strategy = new HashShardingStrategy();

  // ── computeDbIndex ───────────────────────────────────────────────────────

  @Test
  void computeDbIndex_singleShard_alwaysZero() {
    assertThat(strategy.computeDbIndex(0, 1)).isEqualTo(0);
    assertThat(strategy.computeDbIndex(Long.MAX_VALUE, 1)).isEqualTo(0);
  }

  @Test
  void computeDbIndex_resultWithinRange() {
    for (long key = 0; key < 100; key++) {
      int idx = strategy.computeDbIndex(key, 4);
      assertThat(idx).isBetween(0, 3);
    }
  }

  @Test
  void computeDbIndex_zeroShardCount_throwsException() {
    assertThatThrownBy(() -> strategy.computeDbIndex(1L, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("shardDbCount");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -5})
  void computeDbIndex_negativeShardCount_throwsException(int count) {
    assertThatThrownBy(() -> strategy.computeDbIndex(1L, count))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void computeDbIndex_negativeKeyHandledGracefully() {
    int idx = strategy.computeDbIndex(-1L, 3);
    assertThat(idx).isBetween(0, 2);
  }

  @Test
  void computeDbIndex_distributesBetterThanModuloForSequentialIds() {
    // Sequential keys 0-99 should hit all 4 shards with hash strategy
    Set<Integer> indices = new HashSet<>();
    for (long key = 0; key < 100; key++) {
      indices.add(strategy.computeDbIndex(key, 4));
    }
    assertThat(indices).containsExactlyInAnyOrder(0, 1, 2, 3);
  }

  // ── computeTableIndex ────────────────────────────────────────────────────

  @Test
  void computeTableIndex_resultWithinRange() {
    for (long key = 0; key < 50; key++) {
      int idx = strategy.computeTableIndex(key, 10);
      assertThat(idx).isBetween(0, 9);
    }
  }

  @Test
  void computeTableIndex_zeroShardCount_throwsException() {
    assertThatThrownBy(() -> strategy.computeTableIndex(1L, 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("shardTableCount");
  }

  // ── mix ──────────────────────────────────────────────────────────────────

  @Test
  void mix_deterministicForSameInput() {
    assertThat(HashShardingStrategy.mix(42L)).isEqualTo(HashShardingStrategy.mix(42L));
  }

  @Test
  void mix_differentInputProducesDifferentOutput() {
    assertThat(HashShardingStrategy.mix(1L)).isNotEqualTo(HashShardingStrategy.mix(2L));
  }

  @Test
  void mix_zeroInput() {
    // should not throw
    long result = HashShardingStrategy.mix(0L);
    assertThat(result).isNotNull();
  }
}
