package cloud.xcan.angus.core.spring.env;

import java.util.Properties;
import org.springframework.core.env.ConfigurableEnvironment;

public interface ConfigurableApplication {

  void doConfigureApplication(ConfigurableEnvironment environment, Properties envs)
      throws Exception;

}
