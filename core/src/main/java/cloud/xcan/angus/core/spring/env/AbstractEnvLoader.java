package cloud.xcan.angus.core.spring.env;

import static cloud.xcan.angus.api.enums.EditionType.COMMUNITY;
import static cloud.xcan.angus.core.spring.env.EnvKeys.APP_EDITION;
import static cloud.xcan.angus.core.spring.env.EnvKeys.APP_NAME;
import static cloud.xcan.angus.core.spring.env.EnvKeys.APP_VERSION;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.COMMON_ENV_FILE;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.ENV_FILES_KEY;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.ENV_NAME_FORMAT;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.ENV_PROFILES;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.PRIVATE_ENV_NAME;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.api.enums.EditionType;
import cloud.xcan.angus.core.app.ProductInfo;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
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
 * 4. <strong>Extension Mechanism</strong>: - Implement {@link AbstractEnvLoader} interface and
 * register as Spring Bean for custom loading logic - Example use cases: loading additional
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
 * @see EnvironmentPostProcessor
 * @see PropertySourcesPlaceholderConfigurer
 * @see DefaultEarliestEnvLoader
 */
public abstract class AbstractEnvLoader implements EnvironmentPostProcessor, Ordered {

  private final ResourceLoader resourceLoader = new DefaultResourceLoader();

  public static final Properties envs = new Properties();

  public static SpringAppDirUtils appDir;
  public static String appHomeDir;

  public static String appName;
  public static String appVersion;
  public static EditionType appEdition;
  private String[] activeProfiles;

  public static ProductInfo productInfo;

  public AbstractEnvLoader() {
    appDir = new SpringAppDirUtils();
    appHomeDir = appDir.getHomeDir();
  }

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
      SpringApplication application) {
    // Load the application info
    loadApplicationInfo(environment);
    // Load the main environment file.
    loadCommonEnvFile();
    // Load other environment files.
    loadAdditionalEnvFiles();
    // Load or overwrite external env files.
    loadOrRewriteFromExternalEnvFiles(environment, envs);
    // Register the variable to the Spring Environment.\
    environment.getPropertySources().addFirst(new PropertiesPropertySource("customEnv", envs));
    // Configure and install application
    configureApplication(environment, application);
  }

  public abstract void loadOrRewriteFromExternalEnvFiles(ConfigurableEnvironment environment,
      Properties envs);

  public abstract void configureApplication(ConfigurableEnvironment environment,
      SpringApplication application);

  @Override
  public int getOrder() {
    // Ensure loading first
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private void loadApplicationInfo(ConfigurableEnvironment environment) {
    appName = environment.getProperty(APP_NAME, "");
    appVersion = environment.getProperty(APP_VERSION, "");
    appEdition = EditionType.valueOf(environment.getProperty(APP_EDITION, COMMUNITY.getValue()));
    activeProfiles = environment.getActiveProfiles();
  }

  public void loadCommonEnvFile() {
    String searchDir = new SpringAppDirUtils().getConfDir();
    Path envPath = Paths.get(searchDir, COMMON_ENV_FILE);
    if (Files.exists(envPath)) {
      loadEnvFile(envPath);
    }
  }

  public void loadAdditionalEnvFiles() {
    List<String> filesToLoad = new ArrayList<>();

    String envFiles = envs.getProperty(ENV_FILES_KEY, "");
    if (isNotBlank(envFiles)) {
      List<String> envs = Arrays.stream(envFiles.split(","))
          .map(String::trim).filter(s -> !s.isEmpty()).toList();
      for (String env : envs) {
        if (nonNull(activeProfiles)) {
          for (String activeProfile : activeProfiles) {
            if (!String.format(ENV_NAME_FORMAT, activeProfile).equalsIgnoreCase(env)) {
              filesToLoad.add(env);
            }
          }
        } else {
          filesToLoad.addAll(envs);
        }
      }
    }

    if (appEdition.isPrivatization()) {
      filesToLoad.add(PRIVATE_ENV_NAME);
    } else if (nonNull(activeProfiles)) {
      for (String activeProfile : activeProfiles) {
        if (ENV_PROFILES.contains(activeProfile)) {
          filesToLoad.add(String.format(ENV_NAME_FORMAT, activeProfile));
          break;
        }
      }
    }

    String searchDir = new SpringAppDirUtils().getConfDir();
    for (String file : filesToLoad) {
      Path filePath = Paths.get(searchDir, file);
      if (Files.exists(filePath)) {
        loadEnvFile(filePath);
        break;
      }
    }
  }

  public void loadPrivateEnvFile() {
    String searchDir = new SpringAppDirUtils().getConfDir();
    Path envPath = Paths.get(searchDir, PRIVATE_ENV_NAME);
    if (Files.exists(envPath)) {
      loadEnvFile(envPath);
    }
  }

  public void loadEnvFile(Path filePath) {
    try {
      Resource resource = resourceLoader.getResource("file:" + filePath);
      envs.load(resource.getInputStream());
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load env file: " + filePath, e);
    }
  }

}
