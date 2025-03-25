package cloud.xcan.angus.lettucex.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "xcan.redis", ignoreUnknownFields = false)
public class RedisProperties extends
    org.springframework.boot.autoconfigure.data.redis.RedisProperties {

  private Boolean enabled = false;

  private Deployment deployment = Deployment.SINGLE;

}
