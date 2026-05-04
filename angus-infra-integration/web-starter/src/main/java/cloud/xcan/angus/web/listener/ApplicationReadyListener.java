package cloud.xcan.angus.web.listener;

import static cloud.xcan.angus.core.spring.boot.ApplicationBanner.DECORATION_CHARD;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;

import cloud.xcan.angus.core.spring.boot.ApplicationBanner;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

/**
 * @see ProjectInfoAutoConfiguration
 */
@Slf4j
public class ApplicationReadyListener implements ApplicationListener<ApplicationReadyEvent> {

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    ApplicationContext context = event.getApplicationContext();

    ApplicationBanner banner = context.getBean(ApplicationBanner.class);
    banner.printBanner();

    ApplicationInfo infos = event.getApplicationContext().getBean(ApplicationInfo.class);
    infos.printAppInfo();

    StringBuilder sb = new StringBuilder();
    sb.append(DECORATION_CHARD).append("\n\n\t\t")
        .append("Application i18n resources configuration success").append("\n");
    ServerProperties serverProperties = event.getApplicationContext()
        .getBean(ServerProperties.class);
    // Keep in sync with {@link cloud.xcan.angus.spec.experimental.BizConstant#START_APP_SUCCESS_MESSAGE} wording
    sb.append("\t\t\033[34m").append("Application started successfully [PID=")
        .append(getRuntimeMXBean().getName().split("@")[0]).append("]")
        .append(" and Http(s) port ")
        .append(serverProperties.getPort()).append(" is ready").append("\033[0m\n");
    log.info(sb.toString());
    System.out.println(sb.toString());
  }
}
