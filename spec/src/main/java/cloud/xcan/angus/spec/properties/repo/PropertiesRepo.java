package cloud.xcan.angus.spec.properties.repo;

import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.PRIVATE_ENV_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.PrivateAppConfig.PRIVATE_STATIC_ENV_NAME;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

import cloud.xcan.angus.spec.properties.encoding.IOFactory;
import java.io.File;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Configurations;

public class PropertiesRepo extends AbstractPropertiesRepo {

  private final File file;
  private final PropertiesConfiguration config;

  public PropertiesRepo(String envPath, String envFileName) throws Exception {
    this.file = new File(getEnvPath(envPath) + envFileName);
    Configurations configs = new Configurations();
    FileBasedConfigurationBuilder.setDefaultEncoding(PropertiesConfiguration.class, UTF_8);
    this.config = configs.properties(file);
    this.config.setIOFactory(new IOFactory()); // Fix write configuration2 character encoding issue
  }

  public static PropertiesRepo of(String envPath, String envFileName) throws Exception {
    return new PropertiesRepo(envPath, envFileName);
  }

  public static PropertiesRepo ofPrivate(String envPath) throws Exception {
    return new PropertiesRepo(envPath, PRIVATE_ENV_NAME);
  }

  public static PropertiesRepo ofPrivateStatics(String envPath) throws Exception {
    return new PropertiesRepo(envPath + "meta", PRIVATE_STATIC_ENV_NAME);
  }

  public PropertiesRepo save(String key, String value) {
    config.setProperty(key, value);
    return this;
  }

  @Override
  public PropertiesConfiguration getConfig() {
    return config;
  }

  @Override
  public File getFile() {
    return file;
  }
}
