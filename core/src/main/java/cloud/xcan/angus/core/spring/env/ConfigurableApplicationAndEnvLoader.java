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
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.bootstrap.BootstrapConfigFileApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

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
  }

  @Override
  public void configureApplication(ConfigurableEnvironment environment,
      SpringApplication application) {
    try {
      ServiceLoader<ConfigurableApplication> configurableServices
          = ServiceLoader.load(ConfigurableApplication.class);
      for (ConfigurableApplication configurableService : configurableServices) {
        System.out.println(
            "Configuring application with " + configurableService.getClass().getName());
        configurableService.doConfigureApplication(environment, envs);
      }
    } catch (Exception e) {
      System.err.printf("Configure application failure, cause: %s\n", e.getMessage());
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
    // Allow use http override https
    String apiUrlPrefix = EnvHelper.getString(EnvKeys.GM_APIS_URL_PREFIX);
    return isNotBlank(apiUrlPrefix) ? apiUrlPrefix : getGMWebsite();
  }

  public static String getGMWebsite() {
    String website = EnvHelper.getString(EnvKeys.GM_WEBSITE);
    return isNotBlank(website)
        ? (website.startsWith("http") ? website : String.format("http://%s", website))
        : (String.format("http://%s:%s", getInstallGMHost(), getInstallGMPort()));
  }

  public static String getInstallGMHost() {
    return EnvHelper.getString(EnvKeys.GM_HOST, getValidIpv4());
  }

  public static int getInstallGMPort() {
    return EnvHelper.getInt(EnvKeys.GM_PORT, DEFAULT_GM_PORT);
  }

  public static String getTesterApisUrlPrefix() {
    // Allow use http override https
    String apiUrlPrefix = EnvHelper.getString(EnvKeys.TESTER_APIS_SERVER_URL);
    return isNotBlank(apiUrlPrefix) ? apiUrlPrefix : getTesterWebsite();
  }

  public static String getTesterWebsite() {
    String website = EnvHelper.getString(EnvKeys.TESTER_WEBSITE);
    return isNotBlank(website)
        ? (website.startsWith("http") ? website : String.format("http://%s", website))
        : (String.format("http://%s:%s", getInstallTesterHost(), getInstallTesterPort()));
  }

  public static String getInstallTesterHost() {
    return EnvHelper.getString(EnvKeys.TESTER_HOST, getValidIpv4());
  }

  public static int getInstallTesterPort() {
    return EnvHelper.getInt(EnvKeys.TESTER_PORT, DEFAULT_TESTER_PORT);
  }

}
