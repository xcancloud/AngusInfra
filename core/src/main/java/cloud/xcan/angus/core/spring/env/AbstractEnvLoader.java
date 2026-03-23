package cloud.xcan.angus.core.spring.env;

import static cloud.xcan.angus.api.enums.EditionType.COMMUNITY;
import static cloud.xcan.angus.core.spring.env.EnvKeys.APP_EDITION;
import static cloud.xcan.angus.core.spring.env.EnvKeys.APP_NAME;
import static cloud.xcan.angus.core.spring.env.EnvKeys.APP_VERSION;
import static cloud.xcan.angus.core.spring.env.EnvKeys.DEFAULT_DISABLE_SSL_VERIFICATION;
import static cloud.xcan.angus.core.spring.env.EnvKeys.DISABLE_SSL_VERIFICATION;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.COMMON_ENV_FILE;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.ENV_FILES_KEY;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.ENV_NAME_FORMAT;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.ENV_PROFILES;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.PRIVATE_ENV_NAME;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.api.enums.EditionType;
import cloud.xcan.angus.core.app.ProductInfo;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import cloud.xcan.angus.spec.annotations.Beta;
import cloud.xcan.angus.spec.utils.ssl.TrustAllSSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
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
 * 4. <strong>Extension Mechanism</strong>: - Subclass {@link AbstractEnvLoader} and register as
 * {@link EnvironmentPostProcessor} for custom loading logic - Example use cases: loading
 * additional variables from database/remote config center.
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
@Slf4j
public abstract class AbstractEnvLoader implements EnvironmentPostProcessor, Ordered {

  private final ResourceLoader resourceLoader = new DefaultResourceLoader();

  public static final Properties envs = new Properties();

  public static SpringAppDirUtils appDir;
  public static String appHomeDir;

  public static String appName;
  public static String appVersion;
  public static EditionType appEdition;

  public static ProductInfo productInfo;

  private String[] activeProfiles;

  protected AbstractEnvLoader() {
    appDir = new SpringAppDirUtils();
    appHomeDir = appDir.getHomeDir();
  }

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment,
      SpringApplication application) {
    loadApplicationInfo(environment);
    loadCommonEnvFile();
    loadAdditionalEnvFiles();
    loadOrRewriteFromExternalEnvFiles(environment, envs);
    environment.getPropertySources().addFirst(new PropertiesPropertySource("customEnv", envs));
    disableSslVerification();
    configureApplication(environment, application);
  }

  public abstract void loadOrRewriteFromExternalEnvFiles(ConfigurableEnvironment environment,
      Properties envs);

  public abstract void configureApplication(ConfigurableEnvironment environment,
      SpringApplication application);

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private void loadApplicationInfo(ConfigurableEnvironment environment) {
    appName = environment.getProperty(APP_NAME, "");
    appVersion = environment.getProperty(APP_VERSION, "");
    String editionRaw = environment.getProperty(APP_EDITION, COMMUNITY.getValue());
    try {
      appEdition = EditionType.valueOf(editionRaw.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      log.warn("Invalid {}='{}', using {}", APP_EDITION, editionRaw, COMMUNITY);
      appEdition = COMMUNITY;
    }
    activeProfiles = environment.getActiveProfiles();
  }

  public void loadCommonEnvFile() {
    Path envPath = Paths.get(appDir.getConfDir(), COMMON_ENV_FILE);
    if (Files.exists(envPath)) {
      System.out.printf("Loading common env file %s%n", envPath.getFileName());
      loadEnvFile(envPath);
    }
  }

  public void loadAdditionalEnvFiles() {
    List<String> filesToLoad = new ArrayList<>();
    collectNonProfileFilesFromEnvKey(filesToLoad);
    addEditionOrProfileEnvFile(filesToLoad);
    loadFirstExistingInConfDir(filesToLoad);
  }

  private void collectNonProfileFilesFromEnvKey(List<String> filesToLoad) {
    String envFiles = envs.getProperty(ENV_FILES_KEY, "");
    if (!isNotBlank(envFiles)) {
      return;
    }
    for (String token : splitCsv(envFiles)) {
      if (!referencesProfileInName(token)) {
        filesToLoad.add(token);
      }
    }
  }

  private static List<String> splitCsv(String raw) {
    return Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }

  private static boolean referencesProfileInName(String fileName) {
    return ENV_PROFILES.stream().anyMatch(fileName::contains);
  }

  private void addEditionOrProfileEnvFile(List<String> filesToLoad) {
    if (appEdition.isPrivatization()) {
      filesToLoad.add(PRIVATE_ENV_NAME);
    } else if (nonNull(activeProfiles)) {
      for (String activeProfile : activeProfiles) {
        if (ENV_PROFILES.contains(activeProfile)) {
          filesToLoad.add(format(ENV_NAME_FORMAT, activeProfile));
          break;
        }
      }
    }
  }

  private void loadFirstExistingInConfDir(List<String> filesToLoad) {
    String confDir = appDir.getConfDir();
    for (String file : filesToLoad) {
      Path filePath = Paths.get(confDir, file);
      if (Files.exists(filePath)) {
        System.out.printf("Loading env file %s%n", filePath.getFileName());
        loadEnvFile(filePath);
        break;
      }
    }
  }

  public void loadEnvFile(Path filePath) {
    Resource resource = resourceLoader.getResource("file:" + filePath);
    try (InputStream in = resource.getInputStream()) {
      envs.load(in);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load env file: " + filePath, e);
    }
  }

  @Beta
  public void disableSslVerification() {
    boolean disableSslVerification = EnvHelper.getBoolean(DISABLE_SSL_VERIFICATION,
        DEFAULT_DISABLE_SSL_VERIFICATION);
    if (disableSslVerification) {
      log.warn("Disabling SSL verification ({}=true)", DISABLE_SSL_VERIFICATION);
      TrustAllSSLSocketFactory.disableSSLVerification();
    }
  }
}
