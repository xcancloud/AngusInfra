package cloud.xcan.angus.idgen.uid.buffer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CalSlotIndexTest {

  static final class Exposed extends RingBuffer {

    Exposed() {
      super(8);
    }

    int idx(long seq) {
      return calSlotIndex(seq);
    }
  }

  @Test
  void masksToPowerOfTwo() {
    Exposed rb = new Exposed();
    assertThat(rb.idx(0)).isZero();
    assertThat(rb.idx(7)).isEqualTo(7);
    assertThat(rb.idx(8)).isZero();
  }
}
