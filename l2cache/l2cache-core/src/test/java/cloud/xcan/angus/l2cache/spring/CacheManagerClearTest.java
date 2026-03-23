package cloud.xcan.angus.l2cache.spring;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class CacheManagerClearTest {

  @Test
  void defaultMethodsAreNoOp() {
    CacheManagerClear c = new CacheManagerClear() {
    };
    assertThatCode(() -> {
      c.clearLocal("any", "k");
      c.evict("any", Collections.emptyList());
    }).doesNotThrowAnyException();
  }
}
