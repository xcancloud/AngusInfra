package cloud.xcan.angus.spec.properties.repo;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import cloud.xcan.angus.spec.utils.AppDirUtils;
import java.io.File;
import java.util.Objects;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

public abstract class AbstractPropertiesRepo {

  public void saveToDisk() throws ConfigurationException {
    FileHandler fileHandler = new FileHandler(getConfig());
    fileHandler.save(getFile());
  }

  public abstract PropertiesConfiguration getConfig();

  public abstract File getFile();

  public String getEnvPath(String envPath) {
    return isNotEmpty(envPath) ? envPath + File.separator : new AppDirUtils().getConfDir();
  }

  public String getString(String key) {
    return getConfig().getString(key);
  }

  public String getString(String key, String defaultValue) {
    String result = getConfig().getString(key, defaultValue);
    return isEmpty(result) ? defaultValue : result;
  }

  public int getInt(String key) {
    return getConfig().getInt(key);
  }

  public int getInt(String key, int defaultValue) {
    Integer result = getConfig().getInteger(key, null);
    return Objects.isNull(result) ? defaultValue : result;
  }

  public boolean getBoolean(String key) {
    return getConfig().getBoolean(key);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    Boolean result = getConfig().getBoolean(key, null);
    return Objects.isNull(result) ? defaultValue : result;
  }

}
