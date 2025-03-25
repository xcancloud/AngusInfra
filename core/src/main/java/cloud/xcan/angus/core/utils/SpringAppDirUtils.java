package cloud.xcan.angus.core.utils;

import cloud.xcan.angus.spec.experimental.BizConstant.AppDir;
import cloud.xcan.angus.spec.utils.AppDirUtils;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.io.File;
import org.springframework.boot.system.ApplicationHome;

/**
 * When the home.dir environment variable is not set, the root default path is the current project
 * workspace directory
 */
public class SpringAppDirUtils extends AppDirUtils {

  @Override
  public String getHomeDir() {
    String homeDir = System.getProperty(AppDir.HOME_DIR);
    if (ObjectUtils.isNotEmpty(homeDir)) {
      return homeDir.endsWith(File.separator) ? homeDir : homeDir + File.separator;
    }
    return new ApplicationHome().getDir().getAbsolutePath() + File.separator;
  }

}
