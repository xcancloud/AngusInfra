package cloud.xcan.angus.core.app;


import cloud.xcan.angus.api.obf.Str0;
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
        throw new RuntimeException(new Str0(
            new long[]{0xCD292CFA6E4036E8L, 0xC47669ED017BE052L, 0x6B2A85305118DA13L,
                0x9F7B25FE4E736652L, 0x971EEE7411337BEFL})
            .toString() /* => "Failed to create home directory" */);
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
      log.error(new Str0(new long[]{0xB8644B898378A3ECL, 0xF719B1BD07AE453L, 0x414A96013B19B9FBL,
              0x6AF9C0F6A2F1D99FL, 0x18ED246DE8BDFFF3L, 0x5646C57C477BFE4BL, 0xB5C0DFD35290259DL,
              0xB8D2B0C320A84AEAL, 0x98E6EF4F8D4437D1L, 0xA3C644BBE946790EL})
              .toString() /* => "Failed to create application workspace directory and exit, cause: {}" */,
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
