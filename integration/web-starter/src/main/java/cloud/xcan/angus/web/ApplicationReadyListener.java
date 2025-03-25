package cloud.xcan.angus.web;

import static cloud.xcan.angus.core.spring.boot.ApplicationBanner.DECORATION_CHARD;
import static java.lang.management.ManagementFactory.getRuntimeMXBean;

import cloud.xcan.angus.api.obf.Str0;
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
    sb.append(DECORATION_CHARD).append("\n\n\t\t").append(new Str0(
        new long[]{0x28504F285BB20EEFL, 0x13BF414C6A04E5B1L, 0xB2B968E9111884DBL,
            0x214D273F4BE3D57L, 0x84626CCA9C3EB26CL, 0xF3AB2C118DDC7866L, 0x5301D5D92ACF0946L})
        .toString() /* => "Application i18n resources configuration success" */).append("\n");
    ServerProperties serverProperties = event.getApplicationContext()
        .getBean(ServerProperties.class);
    // Must reference {@code START_APP_SUCCESS_MESSAGE} value
    sb.append("\t\t\033[34m").append(new Str0(
            new long[]{0x2E75B997C55311ECL, 0x5F22316F331689B4L, 0xC667A0E23EC166E0L,
                0xEE3294F090BD912AL, 0x658B4C5890F884D2L, 0x37B511B0D653521FL}).toString())
        .append(getRuntimeMXBean().getName().split("@")[0]).append("]")
        .append(new Str0(new long[]{0x4B131D2FFB474086L, 0x542AA4909070F11AL, 0xAAAB16850DA3BCF0L,
            0x82E6F856191F0537L}).toString() /* => " and Http(s) port " */)
        .append(serverProperties.getPort()).append(
            new Str0(new long[]{0xDE8A7821AAA21FFCL, 0x19722CF1D5858191L, 0xE8D3E0CC7DC7F5L})
                .toString() /* => " is ready" */).append("\033[0m\n");
    log.info(sb.toString());
  }
}
