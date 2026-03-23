package cloud.xcan.angus.core.spring.env;

import static cloud.xcan.angus.core.spring.env.EnvHelper.getString;
import static cloud.xcan.angus.core.spring.env.EnvKeys.GM_ADMIN_FULL_NAME;
import static cloud.xcan.angus.core.utils.CoreUtils.exitApp;
import static cloud.xcan.angus.core.utils.CoreUtils.getResourceFileContent;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.DEFAULT_GM_PORT;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.DEFAULT_PRIVATE_TENANT_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.DEFAULT_TESTER_PORT;
import static cloud.xcan.angus.spec.utils.JsonUtils.fromJson;
import static cloud.xcan.angus.spec.utils.NetworkUtils.getValidIpv4;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.core.app.ProductInfo;
import java.util.Properties;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.bootstrap.BootstrapConfigFileApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

@Slf4j
public class ConfigurableApplicationAndEnvLoader extends AbstractEnvLoader {

  public ConfigurableApplicationAndEnvLoader() {
  }

  @Override
  public int getOrder() {
    return BootstrapConfigFileApplicationListener.DEFAULT_ORDER + 1;
  }

  @Override
  public void loadOrRewriteFromExternalEnvFiles(ConfigurableEnvironment environment,
      Properties envs) {
    // NOOP — overrides may merge remote / external properties here
  }

  @Override
  public void configureApplication(ConfigurableEnvironment environment,
      SpringApplication application) {
    try {
      ServiceLoader<ConfigurableApplication> loader =
          ServiceLoader.load(ConfigurableApplication.class);
      for (ConfigurableApplication service : loader) {
        log.info("Configuring application with {}", service.getClass().getName());
        service.doConfigureApplication(environment, envs);
      }
    } catch (Exception e) {
      System.err.printf("Configure application failure, cause: %s%n", e.getMessage());
      log.error("Configure application failure", e);
      exitApp();
    }
  }

  public static ProductInfo getProductInfo() {
    return fromJson(getResourceFileContent(String.format("installation/%s/product.json",
        appEdition.getValue().toLowerCase())), ProductInfo.class);
  }

  public static Long getFinalTenantId(Object mainAppDCache) {
    return /*nonNull(mainAppDCache) ? mainAppDCache.getHid() :*/ DEFAULT_PRIVATE_TENANT_ID;
  }

  public static String getFinalTenantName(Object mainAppDCache) {
    return /*nonNull(mainAppDCache) ? mainAppDCache.getHol().getName()
        : */getString(GM_ADMIN_FULL_NAME, "Unknown");
  }

  public static String getGMApisUrlPrefix() {
    String apiUrlPrefix = EnvHelper.getString(EnvKeys.GM_APIS_URL_PREFIX);
    return isNotBlank(apiUrlPrefix) ? apiUrlPrefix : getGMWebsite();
  }

  public static String getGMWebsite() {
    return resolveWebsiteUrl(
        EnvHelper.getString(EnvKeys.GM_WEBSITE),
        getInstallGMHost(),
        getInstallGMPort());
  }

  public static String getInstallGMHost() {
    return EnvHelper.getString(EnvKeys.GM_HOST, getValidIpv4());
  }

  public static int getInstallGMPort() {
    return EnvHelper.getInt(EnvKeys.GM_PORT, DEFAULT_GM_PORT);
  }

  public static String getTesterApisUrlPrefix() {
    String apiUrlPrefix = EnvHelper.getString(EnvKeys.TESTER_APIS_SERVER_URL);
    return isNotBlank(apiUrlPrefix) ? apiUrlPrefix : getTesterWebsite();
  }

  public static String getTesterWebsite() {
    return resolveWebsiteUrl(
        EnvHelper.getString(EnvKeys.TESTER_WEBSITE),
        getInstallTesterHost(),
        getInstallTesterPort());
  }

  public static String getInstallTesterHost() {
    return EnvHelper.getString(EnvKeys.TESTER_HOST, getValidIpv4());
  }

  public static int getInstallTesterPort() {
    return EnvHelper.getInt(EnvKeys.TESTER_PORT, DEFAULT_TESTER_PORT);
  }

  /**
   * If {@code website} is set, returns it with an {@code http://} prefix when missing; otherwise
   * {@code http://{host}:{port}}. A value starting with {@code "http"} (including {@code https}) is
   * left unchanged.
   */
  private static String resolveWebsiteUrl(String website, String host, int port) {
    if (isNotBlank(website)) {
      String w = website.trim();
      return w.startsWith("http") ? w : "http://" + w;
    }
    return String.format("http://%s:%s", host, port);
  }
}
