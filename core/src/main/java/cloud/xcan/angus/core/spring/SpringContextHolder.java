package cloud.xcan.angus.core.spring;

import static cloud.xcan.angus.core.biz.AbstractJoinAspect.writePublicFileWhenNotExists;

import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.idgen.bid.impl.DefaultBidGenerator;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import java.io.File;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.typelevel.dcache.DCache;
import org.typelevel.dcache.DCacheManager;
import org.typelevel.dcache.impl.DCacheParamImpl;
import org.typelevel.dcache.impl.ParseParamImpl;
import org.typelevel.dcache.impl.XmlParamImpl;
import org.typelevel.v.Str0;

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

  public static DCache getDCache(String no, String path) throws Exception {
    XmlParamImpl keyStoreParam = getKeyStoreParam();
    DCacheManager lm = getDCacheManager(no, keyStoreParam);
    File file = keyStoreParam.getDCacheFile(path);
    lm.install(file);
    return lm.getCon();
  }

  public static DCache getDCache0(String no, String path) throws Exception {
    XmlParamImpl keyStoreParam = getKeyStoreParam();
    DCacheManager lm = getDCacheManager(no, keyStoreParam);
    File file = keyStoreParam.getDCacheFile(path);
    lm.installNoValidate(file);
    return lm.getCon();
  }

  public static @NotNull XmlParamImpl getKeyStoreParam() {
    return new XmlParamImpl(SpringContextHolder.class,
        writePublicFileWhenNotExists(),
        new Str0(new long[]{0x9075DEA4AA0771D7L, 0x935CFB439D332924L, 0xED9FF7237A6D5E81L,
            0x1001CC8579DB66F6L}).toString() /* => "XCanTest.publicCert" */,
        new Str0(new long[]{0x7869588FAE36AF5AL, 0xB856282BB4C6E64AL, 0x5B72D7AD4F157C91L,
            0x915A8841D1BB3865L}).toString() /* => "xcan@store@pub_cNui8V" */, null);
  }

  public static @NotNull DCacheManager getDCacheManager(String no, XmlParamImpl keyStoreParam) {
    ParseParamImpl cipherParam = new ParseParamImpl(no + new Str0(
        new long[]{0xBF2BA088E7A43113L, 0x5E55F8833FCC6697L, 0xCC2F4EF25303A77BL,
            0x19ED7C344E0032FAL}).toString() /* => ".435E9A3AB63ED118" */);
    DCacheParamImpl licenseParam = new DCacheParamImpl(
        new cloud.xcan.angus.api.obf.Str0( // Same with cloud.xcan.angus.License.SUBJECT
            new long[]{0x1E6B43C60AC22C6DL, 0x8423C94847116C1AL, 0xC4EE5C2A8A84EAB2L,
                0xD27DB35290719CD5L}).toString() /* => "XCan Product License" */, keyStoreParam,
        cipherParam);
    return new DCacheManager(licenseParam);
  }


}
