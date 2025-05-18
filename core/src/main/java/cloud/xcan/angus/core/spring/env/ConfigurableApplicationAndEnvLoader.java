package cloud.xcan.angus.core.spring.env;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getDCacheManager;
import static cloud.xcan.angus.core.spring.SpringContextHolder.getKeyStoreParam;
import static cloud.xcan.angus.core.spring.env.EnvHelper.getString;
import static cloud.xcan.angus.core.spring.env.EnvKeys.GM_ADMIN_FULL_NAME;
import static cloud.xcan.angus.core.utils.CoreUtils.exitApp;
import static cloud.xcan.angus.core.utils.CoreUtils.getResourceFileContent;
import static cloud.xcan.angus.core.utils.ValidatorUtils.isUrl;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAIN_APP_SERVICES;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.DEFAULT_GM_PORT;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.DEFAULT_PRIVATE_TENANT_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.DEFAULT_TESTER_PORT;
import static cloud.xcan.angus.spec.utils.JsonUtils.fromJson;
import static cloud.xcan.angus.spec.utils.NetworkUtils.getValidIpv4;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import cloud.xcan.angus.api.pojo.Pair;
import cloud.xcan.angus.core.app.ProductInfo;
import cloud.xcan.angus.spec.experimental.BizConstant.AppCache;
import cloud.xcan.angus.spec.utils.FileUtils;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.bootstrap.BootstrapConfigFileApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.typelevel.dcache.DCache;
import org.typelevel.dcache.DCacheManager;
import org.typelevel.dcache.impl.XmlParamImpl;

public class ConfigurableApplicationAndEnvLoader extends AbstractEnvLoader {

  public static final Map<String, Pair<String, DCache>> localDCaches = new HashMap<>();

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
      loadLicenseFromLocal();

      ServiceLoader<ConfigurableApplication>configurableServices
          = ServiceLoader.load(ConfigurableApplication.class);
      for (ConfigurableApplication configurableService : configurableServices) {
        configurableService.doConfigureApplication(environment, envs);
      }
    } catch (Exception e) {
      System.err.printf("Configure application failure, cause: %s\n", e.getMessage());
      exitApp();
    }
  }

  public static void loadLicenseFromLocal() {
    Collection<File> caches = listFiles(new File(appDir.getLicenceDir(appHomeDir)),
        new String[]{".lic"}, false);
    if (isNotEmpty(caches)) {
      for (File cache : caches) {
        try {
          loadDCacheByFile(cache);
        } catch (Exception e) {
          System.err.printf("---> License %s parsing or validate failed, cause: %s\n",
              cache.getName(), e.getMessage());
          throw new IllegalStateException(e.getMessage(), e);
        }
      }
    }
  }

  public static ProductInfo getProductInfo() {
    return fromJson(getResourceFileContent(String.format("installation/%s/product.json",
        appEdition.getValue().toLowerCase())), ProductInfo.class);
  }

  public static Long getFinalTenantId(DCache mainAppDCache) {
    return nonNull(mainAppDCache) ? mainAppDCache.getHid() : DEFAULT_PRIVATE_TENANT_ID;
  }

  public static String getFinalTenantName(DCache mainAppDCache) {
    return nonNull(mainAppDCache) ? mainAppDCache.getHol().getName()
        : getString(GM_ADMIN_FULL_NAME, "Unknown");
  }

  public static String getGMWebsite() {
    String website = EnvHelper.getString(EnvKeys.GM_WEBSITE);
    if (isNotBlank(website) && !isUrl(website)) {
      throw new IllegalStateException(String.format("Website %s is not valid", website));
    }
    return isNotBlank(website) ? website
        : String.format("http://%s:%s", getInstallGMHost(), getInstallGMPort());
  }

  public static String getInstallGMHost() {
    return EnvHelper.getString(EnvKeys.GM_HOST, getValidIpv4());
  }

  public static int getInstallGMPort() {
    return EnvHelper.getInt(EnvKeys.GM_PORT, DEFAULT_GM_PORT);
  }

  public static String getTesterWebsite() {
    String website = EnvHelper.getString(EnvKeys.TESTER_WEBSITE);
    if (isNotBlank(website) && !isUrl(website)) {
      throw new IllegalStateException(String.format("Website %s is not valid", website));
    }
    return isNotBlank(website) ? website
        : String.format("http://%s:%s", getInstallTesterHost(), getInstallTesterPort());
  }

  public static String getInstallTesterHost() {
    return EnvHelper.getString(EnvKeys.TESTER_HOST, getValidIpv4());
  }

  public static int getInstallTesterPort() {
    return EnvHelper.getInt(EnvKeys.TESTER_PORT, DEFAULT_TESTER_PORT);
  }

  private static void loadDCacheByFile(File cache) throws Exception {
    String no = cache.getName().split("\\.")[0];
    XmlParamImpl keyStoreParam = getKeyStoreParam();
    DCacheManager lm = getDCacheManager(no, keyStoreParam);
    File file = keyStoreParam.getDCacheFile(cache.getPath());
    lm.installNoValidate(file);
    System.out.printf("---> License %s parsing successfully\n", cache.getName());
    DCache dCache = lm.getCon();
    // Only verify the main application, plugin expiration should not affect the use of the main application.
    if (MAIN_APP_SERVICES.contains(dCache.getPco())) {
      lm.var();
      System.out.printf("---> License %s validate successfully\n", cache.getName());
      System.setProperty(AppCache.a, "installed");
    }
    localDCaches.put(no, Pair.of(dCache.getPco(), dCache));
  }
}
