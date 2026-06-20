package cloud.xcan.angus.feign;

import feign.Feign;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


//@Configuration
public class DefaultFeignBuilder {

  @Primary
  @Bean
  public Feign.Builder feignBuilder() {
    return Feign.builder()
        .queryMapEncoder(new FilterQueryMapEncoder())
        .retryer(Retryer.NEVER_RETRY);
  }
}
