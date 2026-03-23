package cloud.xcan.angus.l2cache.synchronous;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class CacheMessageMixinTest {

  @Test
  void mixinDeserializesAlternatePropertyNames() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    mapper.addMixIn(CacheMessage.class, CacheMessageMixin.class);
    CacheMessage msg = mapper.readValue("{\"cacheName\":\"cn\",\"key\":\"kv\"}",
        CacheMessage.class);
    assertThat(msg.getCacheName()).isEqualTo("cn");
    assertThat(msg.getKey()).isEqualTo("kv");
  }

  @Test
  void mixinClassIsSerializableType() {
    assertThat(new CacheMessageMixin()).isInstanceOf(java.io.Serializable.class);
  }
}
