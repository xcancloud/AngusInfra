package cloud.xcan.angus.core.spring.env;

import java.util.Properties;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * SPI loaded via {@link java.util.ServiceLoader} from {@link ConfigurableApplicationAndEnvLoader}
 * to run product-specific setup after env files are merged into {@link AbstractEnvLoader#envs}.
 */
@FunctionalInterface
public interface ConfigurableApplication {

  void doConfigureApplication(ConfigurableEnvironment environment, Properties envs) throws Exception;
}
