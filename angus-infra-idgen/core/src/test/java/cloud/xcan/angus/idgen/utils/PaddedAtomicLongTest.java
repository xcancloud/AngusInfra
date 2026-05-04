package cloud.xcan.angus.idgen.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PaddedAtomicLongTest {

  @Test
  void incrementsAndSumPadding() {
    PaddedAtomicLong a = new PaddedAtomicLong(5L);
    assertThat(a.get()).isEqualTo(5L);
    assertThat(a.incrementAndGet()).isEqualTo(6L);
    assertThat(a.sumPaddingToPreventOptimization()).isGreaterThanOrEqualTo(7L);
  }
}
