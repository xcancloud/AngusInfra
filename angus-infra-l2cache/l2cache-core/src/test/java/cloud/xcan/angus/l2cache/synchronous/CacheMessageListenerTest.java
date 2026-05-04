package cloud.xcan.angus.l2cache.synchronous;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.l2cache.spring.CacheManagerClear;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.Message;

class CacheMessageListenerTest {

  @Test
  void onMessageDeserializesAndClearsLocal() throws Exception {
    CacheManagerClear mgr = mock(CacheManagerClear.class);
    ObjectMapper om = new ObjectMapper();
    CacheMessageListener listener = new CacheMessageListener(mgr, om);

    Message message = mock(Message.class);
    String json = om.writeValueAsString(CacheMessage.of("cacheA", "key1"));
    when(message.getBody()).thenReturn(json.getBytes(StandardCharsets.UTF_8));
    when(message.toString()).thenReturn("RedisMessage");

    listener.onMessage(message, null);

    verify(mgr).clearLocal("cacheA", "key1");
  }

  @Test
  void onMessageInvalidJsonThrows() {
    CacheManagerClear mgr = mock(CacheManagerClear.class);
    CacheMessageListener listener = new CacheMessageListener(mgr, new ObjectMapper());
    Message message = mock(Message.class);
    when(message.getBody()).thenReturn("{not-json".getBytes(StandardCharsets.UTF_8));

    assertThatThrownBy(() -> listener.onMessage(message, null))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Read cache message");
  }
}
