package cloud.xcan.sdf.core.spring.env;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.core.utils.SpringAppDirUtils;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.support.ResourcePropertySource;

@Slf4j
public abstract class CustomEnvironmentPostProcessor implements EnvironmentPostProcessor {

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
      SpringApplication application) {
    if (isNotEmpty(getEnvFiles())) {
      String confDir = new SpringAppDirUtils().getConfDir();
      MutablePropertySources propertySources = environment.getPropertySources();
      for (String envFile : getEnvFiles()) {
        try {
          propertySources.addFirst(
              new ResourcePropertySource(new FileUrlResource(confDir + envFile)));
        } catch (IOException e) {
          log.error("Exception reading configuration " + envFile, e);
        }
      }
    }
  }

  public abstract List<String> getEnvFiles();

}
