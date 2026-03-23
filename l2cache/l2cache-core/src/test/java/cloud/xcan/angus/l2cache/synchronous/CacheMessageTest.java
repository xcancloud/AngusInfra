package cloud.xcan.angus.l2cache.synchronous;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CacheMessageTest {

  @Test
  void ofSetsFields() {
    CacheMessage m = CacheMessage.of("region", "id-1");
    assertThat(m.getCacheName()).isEqualTo("region");
    assertThat(m.getKey()).isEqualTo("id-1");
  }

  @Test
  void settersChain() {
    CacheMessage m = new CacheMessage().setCacheName("c").setKey("k");
    assertThat(m.getCacheName()).isEqualTo("c");
    assertThat(m.getKey()).isEqualTo("k");
  }
}
