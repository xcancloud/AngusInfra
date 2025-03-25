package cloud.xcan.angus.l2cache.synchronous;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class CacheMessage implements Serializable {

  private String cacheName;

  private String key;

  public static CacheMessage of(String cacheName, String key) {
    return new CacheMessage().setCacheName(cacheName).setKey(key);
  }
}
