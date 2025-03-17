package cloud.xcan.sdf.l2cache.synchronous;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class CacheMessageMixin implements Serializable {

  @JsonProperty("cacheName")
  private String cacheName;

  @JsonProperty("key")
  private Object key;

}