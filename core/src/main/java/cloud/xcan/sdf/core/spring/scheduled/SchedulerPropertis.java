package cloud.xcan.sdf.core.spring.scheduled;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "xcan.scheduler", ignoreUnknownFields = false)
public class SchedulerPropertis {

  private int threadPoolSize = 10;

}
