
package cloud.xcan.sdf.core.fegin;

import feign.Feign;
import feign.Retryer;
import feign.querymap.BeanQueryMapEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


//@Configuration
public class DefaultFeignBuilder {

  @Primary
  @Bean
  public Feign.Builder feignBuilder() {
    return Feign.builder().queryMapEncoder(new BeanQueryMapEncoder()).retryer(Retryer.NEVER_RETRY);
  }
}
