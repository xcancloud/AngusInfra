package cloud.xcan.angus.core.spring.env;

import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Minimal env loader: loads {@code .common.env} / profile files only, with no external overrides or
 * post-configuration.
 */
public class DefaultEarliestEnvLoader extends AbstractEnvLoader {

  @Override
  public void loadOrRewriteFromExternalEnvFiles(ConfigurableEnvironment environment,
      Properties envs) {
    // NOOP
  }

  @Override
  public void configureApplication(ConfigurableEnvironment environment,
      SpringApplication application) {
    // NOOP
  }
}
