package cloud.xcan.angus.core.spring.env;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Spring Boot environment variable loader for loading hierarchical environment configuration files
 * (.env and .xxx.env) from specified directories, ensuring proper priority of ${} placeholder
 * resolution in Spring configurations.
 *
 * <h3>Core Features:</h3>
 * 1. <strong>Multi-path Configuration Loading</strong>: - Priority loading from directories
 * specified by system ./conf.
 * <p>
 * 2. <strong>Hierarchical Configuration Loading</strong>: - First loads primary configuration file
 * (.env) containing ENV_FILES variable defining load order - Loads additional configuration files
 * in ENV_FILES order (later files override earlier ones) - Example ENV_FILES value:
 * .local.env,.prod.env
 * <p>
 * 3. <strong>Spring Configuration Integration</strong>: - Loaded variables are registered as
 * highest-priority property source - Supports ${variable:default} syntax in application.yml
 * <p>
 * 4. <strong>Extension Mechanism</strong>: - Implement {@link AbstractAngusEnvFileLoader} interface
 * and register as Spring Bean for custom loading logic - Example use cases: loading additional
 * variables from database/remote config center.
 *
 * <h3>Usage Examples:</h3>
 * <pre class="code">
 * // Specify config directory at startup
 * java -DCONF_DIR=/etc/app/conf -jar app.jar
 *
 * // .env file example
 * ENV_FILES=.local.env
 * DB_HOST=common-db-host
 * </pre>
 *
 * @see org.springframework.boot.env.EnvironmentPostProcessor
 * @see DefaultAngusEnvFileLoader
 */
public abstract class AbstractAngusEnvFileLoader implements EnvironmentPostProcessor, Ordered {

  private static final String ENV_FILES_KEY = "ENV_FILES";
  private static final String PRIMARY_ENV_FILE = ".env";
  private final ResourceLoader resourceLoader = new DefaultResourceLoader();

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
      SpringApplication application) {
    // Load the main environment file.
    Properties envProperties = loadPrimaryEnvFile();
    // Load other environment files.
    loadAdditionalEnvFiles(envProperties);
    // Load or overwrite external env files.
    loadOrRewriteFromExternalEnvFiles(envProperties);
    // Register the variable to the Spring Environment.
    environment.getPropertySources()
        .addFirst(new PropertiesPropertySource("customEnv", envProperties));
  }

  public Properties loadPrimaryEnvFile() {
    String searchDir = new SpringAppDirUtils().getConfDir();
    Path envPath = Paths.get(searchDir, PRIMARY_ENV_FILE);
    if (Files.exists(envPath)) {
      return loadEnvFile(envPath);
    }
    return new Properties();
  }

  public void loadAdditionalEnvFiles(Properties envProperties) {
    String envFiles = envProperties.getProperty(ENV_FILES_KEY, "");
    if (isNotBlank(envFiles)) {
      List<String> filesToLoad = Arrays.stream(envFiles.split(","))
          .map(String::trim).filter(s -> !s.isEmpty()).toList();
      String searchDir = new SpringAppDirUtils().getConfDir();
      for (String file : filesToLoad) {
        Path filePath = Paths.get(searchDir, file);
        if (Files.exists(filePath)) {
          envProperties.putAll(loadEnvFile(filePath));
          break;
        }
      }
    }
  }

  public abstract void loadOrRewriteFromExternalEnvFiles(Properties envProperties);

  public Properties loadEnvFile(Path filePath) {
    Properties props = new Properties();
    try {
      Resource resource = resourceLoader.getResource("file:" + filePath);
      props.load(resource.getInputStream());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load env file: " + filePath, e);
    }
    return props;
  }

  @Override
  public int getOrder() {
    // Ensure loading first
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
