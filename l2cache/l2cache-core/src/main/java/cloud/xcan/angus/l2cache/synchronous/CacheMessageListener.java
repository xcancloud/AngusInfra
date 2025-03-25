package cloud.xcan.angus.l2cache.synchronous;


import cloud.xcan.angus.core.cache.CacheManagerClear;
import cloud.xcan.angus.spec.experimental.Assert;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

@Slf4j
public class CacheMessageListener implements MessageListener {

  private final CacheManagerClear cacheManagerClear;

  private final ObjectMapper objectMapper;

  public CacheMessageListener(CacheManagerClear cacheManagerClear, ObjectMapper objectMapper) {
    super();
    this.cacheManagerClear = cacheManagerClear;
    this.objectMapper = objectMapper;
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    log.debug("The l2cache[{}] topic message: {}",
        cacheManagerClear.getClass().getSimpleName(), new String(message.getBody()));
    // CacheMessage cacheMessage = JsonUtils.fromJson(message.getBody(), CacheMessage.class);
    CacheMessage cacheMessage;
    try {
      cacheMessage = objectMapper.readValue(new String(message.getBody()), CacheMessage.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Read cache message exception", e);
    }
    Assert.assertNotNull(cacheMessage,
        "The l2cache topic deserialized message is null, src message: {}", message.toString());
    log.debug("Received l2cache clear local cache message, cacheName: {}, key: {}",
        cacheMessage.getCacheName(), cacheMessage.getKey());
    cacheManagerClear.clearLocal(cacheMessage.getCacheName(), cacheMessage.getKey());
  }
}
