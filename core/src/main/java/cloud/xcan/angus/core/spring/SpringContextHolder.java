package cloud.xcan.angus.core.spring;

import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.idgen.bid.impl.DefaultBidGenerator;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import javax.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringContextHolder implements ApplicationContextAware {

  private static ApplicationContext ctx;

  public static Object getBean(String name) {
    return ctx.getBean(name);
  }

  public static <T> T getBean(Class<T> clazz) {
    try {
      return ctx.getBean(clazz);
    } catch (Exception e) {
      return null;
    }
  }

  public static <T> T getBean(String name, Class<T> clazz) {
    try {
      return ctx.getBean(name, clazz);
    } catch (Exception e) {
      return null;
    }
  }

  public static ApplicationContext getCtx() {
    return ctx;
  }

  public static <T> T registerBean(Class<T> requiredType, String beanName) {
    ConfigurableApplicationContext context = (ConfigurableApplicationContext) ctx;
    DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) context
        .getAutowireCapableBeanFactory();
    BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(requiredType);
    defaultListableBeanFactory.registerBeanDefinition(beanName, builder.getBeanDefinition());
    return context.getBean(requiredType);
  }

  @Override
  public void setApplicationContext(@Nonnull ApplicationContext context) throws BeansException {
    SpringContextHolder.ctx = context;
  }

  public static CachedUidGenerator getCachedUidGenerator() {
    return SpringContextHolder.getBean(CachedUidGenerator.class);
  }

  public static DefaultBidGenerator getBidGenerator() {
    return SpringContextHolder.getBean(DefaultBidGenerator.class);
  }

  public static boolean isProd() {
    String[] profiles = SpringContextHolder.getCtx().getEnvironment().getActiveProfiles();
    if (profiles.length != 0) {
      for (String profile : profiles) {
        if (new Str0(new long[]{0xCAF92BDCBDAB7E47L, 0x8F884AE8DB0EC24CL})
            .toString() /* => "prod" */.equalsIgnoreCase(profile)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean isCloudService() {
    ApplicationInfo info = SpringContextHolder.getBean(ApplicationInfo.class);
    return new Str0(new long[]{0x23B748A968A51B4AL, 0x18317A1449BDB49L, 0x2478AB091CEF106L})
        .toString() /* => "CLOUD_SERVICE" */.equalsIgnoreCase(info.getEditionType());
  }

}
