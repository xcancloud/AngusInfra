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
    File dir = new File(homeDir);
    try {
      if (!dir.exists() && !dir.mkdirs()) {
        throw new RuntimeException("Failed to create home directory");
      }
      log.info("Find home dir: {}", homeDir);
      initAppDir(appDirUtils.getTmpDir(homeDir), "Failed to create tmp directory",
          "Init tmp dir: {}", "Find tmp dir: {}");
      initAppDir(appDirUtils.getWorkDir(homeDir), "Failed to create work directory",
          "Init work dir: {}", "Find work dir: {}");
      initAppDir(appDirUtils.getDataDir(homeDir), "Failed to create data directory",
          "Init data dir: {}", "Find data dir: {}");
      initAppDir(appDirUtils.getLogsDir(homeDir), "Failed to create logs directory",
          "Init logs dir: {}", "Find logs dir: {}");
    } catch (Exception e) {
      log.error("Failed to create application workspace directory and exit, cause: {}",
          e.getMessage());
      SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
      System.exit(-1);
    }
  }

  private void initAppDir(String dir0, String s, String s2, String s3) {
    File dir = new File(dir0);
    if (!dir.exists()) {
      if (!dir.mkdirs()) {
        throw new RuntimeException(s);
      }
      log.info(s2, dir.getAbsolutePath());
    } else {
      log.info(s3, dir.getAbsolutePath());
    }
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
