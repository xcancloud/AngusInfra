package cloud.xcan.angus.core.spring;

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

  private static ApplicationContext requireCtx() {
    if (ctx == null) {
      throw new IllegalStateException("ApplicationContext has not been set yet");
    }
    return ctx;
  }

  public static Object getBean(String name) {
    return requireCtx().getBean(name);
  }

  /**
   * Returns the bean or {@code null} if missing or if the context is not ready.
   */
  public static <T> T getBean(Class<T> clazz) {
    if (ctx == null) {
      return null;
    }
    try {
      return ctx.getBean(clazz);
    } catch (BeansException e) {
      return null;
    }
  }

  /**
   * Returns the bean or {@code null} if missing or if the context is not ready.
   */
  public static <T> T getBean(String name, Class<T> clazz) {
    if (ctx == null) {
      return null;
    }
    try {
      return ctx.getBean(name, clazz);
    } catch (BeansException e) {
      return null;
    }
  }

  public static ApplicationContext getCtx() {
    return ctx;
  }

  public static <T> T registerBean(Class<T> requiredType, String beanName) {
    ConfigurableApplicationContext context = (ConfigurableApplicationContext) requireCtx();
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

  private static final String PROFILE_PROD = "prod";

  public static boolean isProd() {
    if (ctx == null) {
      return false;
    }
    String[] profiles = ctx.getEnvironment().getActiveProfiles();
    for (String profile : profiles) {
      if (PROFILE_PROD.equalsIgnoreCase(profile)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isCloudService() {
    ApplicationInfo info = SpringContextHolder.getBean(ApplicationInfo.class);
    return info != null && info.isCloudServiceEdition();
  }

}
