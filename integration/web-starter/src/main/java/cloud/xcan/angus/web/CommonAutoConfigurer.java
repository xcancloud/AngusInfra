
package cloud.xcan.angus.web;

import cloud.xcan.sdf.api.obf.Str0;
import cloud.xcan.sdf.core.jpa.auditor.SecurityAuditorAware;
import cloud.xcan.sdf.core.meter.DiskMetrics;
import cloud.xcan.sdf.core.spring.boot.ApplicationBanner;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import cloud.xcan.sdf.core.spring.filter.GlobalProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = true)
@EnableConfigurationProperties({ApplicationInfo.class, GlobalProperties.class})
@ConditionalOnProperty(name = "xcan.common.enabled", havingValue = "true", matchIfMissing = true)
public class CommonAutoConfigurer implements ApplicationContextAware {

  @Getter
  private static ApplicationContext applicationContext;

  public CommonAutoConfigurer() {
    log.info(new Str0(
        new long[]{0x2F6882DAF2C7432EL, 0x72F11BBA53040BE8L, 0x93AF8A2C7B2C4EEBL,
            0x84A9F35AAA5C0BAAL,
            0xFCF62F11E9D7FEEDL, 0x7B5D8C4B9689046BL, 0xE7C2520ACB9F260BL})
        .toString() /* => "Application common auto configuration is enabled" */);
  }

  @Bean
  public ApplicationBanner applicationBanner() {
    return new ApplicationBanner();
  }

  @Bean
  public SecurityAuditorAware auditorAware() {
    return new SecurityAuditorAware();
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    CommonAutoConfigurer.applicationContext = applicationContext;
    ApplicationInfo info = applicationContext.getBean(ApplicationInfo.class);
    if (new Str0(new long[]{0x36BCF086E1EDEC70L, 0xC59B6406C1E4E8A3L, 0xD36A79AF5F986BC1L})
        .toString() /* => "CLOUD_SERVICE" */.equalsIgnoreCase(info.getEditionType())) {
      System.setProperty(
          new Str0(new long[]{0xF9DC9A8CDDAA2852L, 0x7E695CBC8461AAAL, 0x653F27E3373BEC85L})
              .toString() /* => "LICENSE_PASS_KEY" */,
          new Str0(new long[]{0x26660259AC059E29L, 0x874CC109D814FC60L, 0x40647729D23B2DCDL,
              0x4632A9FCD988251DL}).toString() /* => "BBQQ-G8HZ-NK2M-QKNA-XQ7U" */);
    }
  }

  @Configuration(proxyBeanMethods = false)
  static class MeterRegistryConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DiskMetrics diskMetrics() {
      System.setProperty(new Str0(
              new long[]{0x558AEFD024489453L, 0x1C7FC886B8D9781AL, 0xD06CAE7649FADCF1L})
              .toString() /* METRICS_LOCK */,
          new Str0(new long[]{0x6962982E78E59713L, 0x49192B610A880792L, 0xDAD2C8901F14D6AL,
              0x4B2BA3993A0B88B6L}).toString() /* => ".435E9A3AB63ED118" */
      ); /* Salt */
      return new DiskMetrics();
    }
  }

}
