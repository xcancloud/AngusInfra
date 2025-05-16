package cloud.xcan.angus.core.spring.env;


import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;

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

  @Override
  public int getOrder() {
    return super.getOrder();
  }
}
