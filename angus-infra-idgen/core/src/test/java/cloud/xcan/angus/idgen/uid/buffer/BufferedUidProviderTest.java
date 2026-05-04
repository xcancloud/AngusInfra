package cloud.xcan.angus.idgen.uid.buffer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class BufferedUidProviderTest {

  @Test
  void functionalInterface() {
    BufferedUidProvider p = second -> List.of(second * 10, second * 10 + 1);
    assertThat(p.provide(3L)).containsExactly(30L, 31L);
  }
}
