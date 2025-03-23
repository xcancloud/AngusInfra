package cloud.xcan.angus.web;

import static cloud.xcan.sdf.core.app.verx.VerxRegister.cacheManager;
import static cloud.xcan.sdf.core.utils.AppEnvUtils.initSneakyLogDir;
import static cloud.xcan.sdf.core.utils.CoreUtils.runAtJar;
import static java.lang.System.exit;

import cloud.xcan.sdf.core.app.AppBeanReadyInit;
import cloud.xcan.sdf.core.app.AppPropertiesRegisterInit;
import cloud.xcan.sdf.core.app.AppWorkspaceInit;
import cloud.xcan.sdf.core.app.ApplicationInit;
import cloud.xcan.sdf.core.app.verx.VerxRegister;
import cloud.xcan.sdf.core.spring.SpringContextHolder;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import cloud.xcan.sdf.core.utils.AppEnvUtils;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.typelevel.v.Str0;

/**
 * Built-in init order.
 *
 * <p>
 * 1)、{@link AppWorkspaceInit}: Init application workspace.
 * <p>
 * 2)、{@link `LicenseInit` In store service}：1-Write lic to workdir(./lic); 2-Write main lic to
 * system properties.
 * <p>
 * 3)、{@link `PluginInit` In store service}：1-Write plugin to workdir(.plugins); 2-Load and start
 * plugins.
 * <p>
 * 4)、{@link AppPropertiesRegisterInit}: Init application system properties.
 * <p>
 * 5)、{@link AppBeanReadyInit}: Init application beans.
 */
public class ApplicationInitListener implements ApplicationListener<ApplicationStartedEvent> {


  private static final String IM = new Str0(
      new long[]{0xED65BFA117257848L, 0xEE4E8C602956F9ACL, 0x9F21B20CFB43625DL, 0x61CA7D2783B23B54L,
          0x89A44F3C38612F4FL, 0x84386D3F37D663L, 0x6A4392AFC8E0CB66L})
      .toString()/* => "Application license has expired or is invalid" */;

  private static final String AM = new Str0(
      new long[]{0xB1783D42F7D06A31L, 0x3DCF42D8DE825DBEL, 0xC44151429334E3D9L, 0xFD3394BFEC56B238L,
          0x28D5FF91AC50F38DL}).toString() /* => "Application init exception" */;

  /**
   * TODO 1、Timer check ? 2、更多组件内部构造强制依赖 VA:dcacheManager ?
   */
  @Override
  public void onApplicationEvent(ApplicationStartedEvent event) {
    // Init application
    initApplication(event);

    // Note: Register the main app license manager, must be initialized at the beginning.
    // Used by application initialization verification
    SpringContextHolder.registerBean(VerxRegister.class, new Str0(
        new long[]{0x34CC9A19DE6ECD99L, 0x9D0E22B0DC98A9ADL, 0xB94135F40269A5C7L})
        .toString() /* => "dCacheManager" */);

    // Check lcs
    try {
      cacheManager().var();
    } catch (Exception e) {
      System.out.println("\n***********" + IM + "*************\n" + "\t cause: " + e.getMessage());
      if (runAtJar()) {
        SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
        exit(-1);
      }
      // TODO 提供基于事件触发加载 VerxRegister 调用验证许可有效性
    }

    // Init international message.
    MessageSource messageSource = event.getApplicationContext().getBean(MessageSource.class);
    MessageHolder.setMessageSource(messageSource);

    // Init sneaky lcs check
    try {
      initSneakyLogDir();
    } catch (Exception e) {
      System.out.println("\n***********" + AM + "*************\n" + "\t cause: " + e.getMessage());
      if (runAtJar()) {
        SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
        exit(-1);
      }
    }

    // Initialization completed, ready to receive requests.
    ApplicationInfo.APP_READY = true;
    AppEnvUtils.APP_INIT_READY = true;
  }

  private void initApplication(ApplicationStartedEvent event) {
    ApplicationContext ac = event.getApplicationContext();
    Map<String, ApplicationInit> initsMap = ac.getBeansOfType(ApplicationInit.class);
    if (ObjectUtils.isNotEmpty(initsMap)) {
      List<ApplicationInit> inits = new ArrayList<>(initsMap.values());
      Collections.sort(inits);
      for (ApplicationInit init : inits) {
        try {
          init.init();
        } catch (Exception e) {
          System.out.println(AM + ": " + e.getMessage());
          throw new RuntimeException(e);
        }
      }
    }
  }

}
