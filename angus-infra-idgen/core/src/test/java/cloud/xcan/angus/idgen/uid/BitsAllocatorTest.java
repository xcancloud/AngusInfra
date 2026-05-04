package cloud.xcan.angus.idgen.uid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BitsAllocatorTest {

  @Test
  void allocatesAndExposesShifts() {
    BitsAllocator a = new BitsAllocator(28, 22, 13);
    assertThat(a.getTimestampBits()).isEqualTo(28);
    assertThat(a.getWorkerIdBits()).isEqualTo(22);
    assertThat(a.getSequenceBits()).isEqualTo(13);
    assertThat(a.getTimestampShift()).isEqualTo(35);
    assertThat(a.getWorkerIdShift()).isEqualTo(13);
    assertThat(a.getSignBits()).isEqualTo(1);

    long v = a.allocate(10L, 2L, 3L);
    assertThat(v).isGreaterThan(0L);
  }

  @Test
  void toStringContainsFields() {
    BitsAllocator a = new BitsAllocator(28, 22, 13);
    assertThat(a.toString()).contains("BitsAllocator");
  }

  @Test
  void invalidBitLayoutThrows() {
    assertThatThrownBy(() -> new BitsAllocator(10, 10, 10))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
