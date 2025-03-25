package cloud.xcan.angus.core.app.verx;

import static cloud.xcan.angus.core.spring.SpringContextHolder.isCloudService;
import static cloud.xcan.angus.core.utils.CoreUtils.runAtJar;

import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.spec.utils.StringUtils;
import org.springframework.boot.SpringApplication;
import org.typelevel.dcache.DCacheManager;
import org.typelevel.dcache.impl.DCacheParamImpl;
import org.typelevel.dcache.impl.ParseParamImpl;
import org.typelevel.dcache.impl.XmlParamImpl;
import org.typelevel.v.Str0;

public class VerxRegister {

  private static boolean install = false;

  private static DCacheManager dCacheManager;

  public VerxRegister() {
    if (isCloudService() && StringUtils.isEmpty(System.getProperty(
        new Str0(new long[]{0xCE4C2599BFB7CC70L, 0x667B073ED9D7005AL, 0x24E74F7C77DA3225L})
            .toString() /* => "MAIN_SUBJECT_KEY" */))) {
      System.setProperty(
          new Str0(new long[]{0xCE4C2599BFB7CC70L, 0x667B073ED9D7005AL, 0x24E74F7C77DA3225L})
              .toString() /* => "MAIN_SUBJECT_KEY" */, new Str0(
              new long[]{0x44E5A941738E54AEL, 0xDEA735453E24F29CL, 0xF3FA22B1B04C8349L,
                  0xCBB5BCEDBBE839C5L}).toString() /* => "XCan Product License" */);
    }
    VerxRegister.dCacheManager = initDCacheManager();
  }

  public DCacheManager initDCacheManager() {
    VerxProperties p0 = SpringContextHolder.getBean(VerxProperties.class);
    XmlParamImpl p1 = new XmlParamImpl(VerxRegister.class, p0.getKey(),
        p0.getName(), p0.getBuild(), null);
    ParseParamImpl p2 = new ParseParamImpl(System.getProperty(new Str0(
        new long[]{0x298837D6B7F96C96L, 0xA5F0B1232B7D56DDL, 0x9A5590E050ABBDBAL})
        .toString()/* => "LICENSE_PASS_KEY" */) + System.getProperty(new Str0(
        new long[]{0x86ED8F48CD37BB69L, 0xC2CC82E15799B904L, 0xA3F8E7C593114AC6L})
        .toString()/* => "METRICS_LOCK" */));
    DCacheParamImpl p3 = new DCacheParamImpl(System.getProperty(new Str0(
        new long[]{0x4E8822C8B23810B9L, 0x77975C81DEE569E6L, 0xDCE0377C3C71E8BBL})
        .toString()/* => "MAIN_SUBJECT_KEY" */), p1, p2);
    DCacheManager lm = new DCacheManager(p3);
    if (!install) {
      try {
        lm.install(p1.getDCacheFile(p0.getPath()));
        install = true;
      } catch (Exception e) {
        System.out.println("\n***********" + new Str0(
            new long[]{0xD255BE390CEB9156L, 0x656755D906194647L, 0x1FACAB47CC7F79CFL,
                0x3F882A2A75BD352BL, 0x683415EC27D1B744L, 0xA8BB3E63BDB24DDL,
                0xA6F47F6E87A0A579L, 0xA10C2BE9FFB5AFF3L})
            .toString()/* => "Application license installation failed and exited" */
            + "*************\n");
        if (runAtJar()) {
          SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
          System.exit(-1);
        }
      }
    }
    return lm;
  }

  public static DCacheManager cacheManager() {
    return dCacheManager;
  }

  public static boolean isInstall() {
    return install;
  }
}
