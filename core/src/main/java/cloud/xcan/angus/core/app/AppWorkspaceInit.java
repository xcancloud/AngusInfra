package cloud.xcan.angus.core.app;


import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.utils.SpringAppDirUtils;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;

@Slf4j
public class AppWorkspaceInit implements ApplicationInit {

  @Override
  public void init() {
    SpringAppDirUtils appDirUtils = new SpringAppDirUtils();
    String homeDir = appDirUtils.getHomeDir();
    File home = new File(homeDir);
    try {
      if (!home.exists() && !home.mkdirs()) {
        throw new RuntimeException("Failed to create home directory");
      }
      log.info("Find home dir: {}", homeDir);
      ensureDirectory(appDirUtils.getTmpDir(homeDir), "Failed to create tmp directory",
          "Init tmp dir: {}", "Find tmp dir: {}");
      ensureDirectory(appDirUtils.getWorkDir(homeDir), "Failed to create work directory",
          "Init work dir: {}", "Find work dir: {}");
      ensureDirectory(appDirUtils.getDataDir(homeDir), "Failed to create data directory",
          "Init data dir: {}", "Find data dir: {}");
      ensureDirectory(appDirUtils.getLogsDir(homeDir), "Failed to create logs directory",
          "Init logs dir: {}", "Find logs dir: {}");
    } catch (Exception e) {
      log.error("Failed to create application workspace directory and exit, cause: {}",
          e.getMessage(), e);
      SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
      System.exit(-1);
    }
  }

  private void ensureDirectory(String path, String mkdirFailMessage, String logCreatedFormat,
      String logExistsFormat) {
    File dir = new File(path);
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new RuntimeException(mkdirFailMessage);
      }
      log.info(logCreatedFormat, dir.getAbsolutePath());
    } else {
      log.info(logExistsFormat, dir.getAbsolutePath());
    }
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
