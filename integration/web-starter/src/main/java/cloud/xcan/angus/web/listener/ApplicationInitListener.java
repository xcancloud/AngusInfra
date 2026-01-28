package cloud.xcan.angus.web.listener;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.app.AppBeanReadyInit;
import cloud.xcan.angus.core.app.AppPropertiesRegisterInit;
import cloud.xcan.angus.core.app.AppWorkspaceInit;
import cloud.xcan.angus.core.app.ApplicationInit;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.utils.AppEnvUtils;
import cloud.xcan.angus.spec.locale.MessageHolder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;

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

  @Override
  public void onApplicationEvent(ApplicationStartedEvent event) {
    // Init application
    initApplication(event);

    // Init international message.
    MessageSource messageSource = event.getApplicationContext().getBean(MessageSource.class);
    MessageHolder.setMessageSource(messageSource);

    // Initialization completed, ready to receive requests.
    ApplicationInfo.APP_READY = true;
    AppEnvUtils.APP_INIT_READY = true;
  }

  private void initApplication(ApplicationStartedEvent event) {
    ApplicationContext ac = event.getApplicationContext();
    Map<String, ApplicationInit> initsMap = ac.getBeansOfType(ApplicationInit.class);
    if (isNotEmpty(initsMap)) {
      List<ApplicationInit> inits = new ArrayList<>(initsMap.values());
      Collections.sort(inits);
      for (ApplicationInit init : inits) {
        try {
          init.init();
        } catch (Exception e) {
          System.out.println(e.getMessage());
          throw new RuntimeException(e);
        }
      }
    }
  }

}
