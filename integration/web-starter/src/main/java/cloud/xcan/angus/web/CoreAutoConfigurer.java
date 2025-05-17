package cloud.xcan.angus.web;

import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_LOCALE;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_TIME_ZONE;
import static cloud.xcan.angus.spec.SpecConstant.LOCALE_COOKIE_NAME;
import static cloud.xcan.angus.spec.utils.ObjectUtils.emptySafe;
import static org.springframework.boot.web.servlet.filter.OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER;
import static org.springframework.web.servlet.DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME;

import cloud.xcan.angus.core.app.AppBeanReadyInit;
import cloud.xcan.angus.core.app.AppPropertiesRegisterInit;
import cloud.xcan.angus.core.app.AppWorkspace;
import cloud.xcan.angus.core.app.AppWorkspaceInit;
import cloud.xcan.angus.core.app.ApplicationInit;
import cloud.xcan.angus.core.app.check.CheckAppExpirationAspect;
import cloud.xcan.angus.core.app.verx.VerxProperties;
import cloud.xcan.angus.core.biz.I18nMessageAspect;
import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.biz.NameJoinAspect;
import cloud.xcan.angus.core.exception.DefaultGlobalExceptionAdvice;
import cloud.xcan.angus.core.fegin.interceptor.FeignRequestInterceptor;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.spring.filter.GlobalHoldFilter;
import cloud.xcan.angus.core.spring.filter.GlobalProperties;
import cloud.xcan.angus.core.spring.security.PrincipalPermissionService;
import cloud.xcan.angus.swagger.ByteArrayToStringConverter;
import cloud.xcan.angus.validator.ValidatorProperties;
import cloud.xcan.angus.web.endpoint.AppWorkspaceEndpoint;
import cloud.xcan.angus.web.endpoint.MessageEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.MultipartConfigElement;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Slf4j
@EnableWebMvc
@Configuration(proxyBeanMethods = true)
@AutoConfigureAfter(CommonAutoConfigurer.class)
@ConditionalOnClass(WebMvcConfigurer.class)
@EnableConfigurationProperties({ApplicationInfo.class, GlobalProperties.class,
    VerxProperties.class, ValidatorProperties.class, MultipartProperties.class})
@ConditionalOnProperty(name = "xcan.core.enabled", havingValue = "true", matchIfMissing = false)
public class CoreAutoConfigurer implements WebMvcConfigurer {

  public CoreAutoConfigurer() {
    log.info("Application core auto configuration is enabled");
  }

  @Resource
  private ObjectMapper objectMapper;

  @Value("${server.servlet.context-path:'/'}")
  private String contextPath;

  @Autowired
  private WebProperties webProperties;

  @Bean("appWorkspaceInit")
  public ApplicationInit workspaceInit() {
    return new AppWorkspaceInit();
  }

  @Bean
  public AppWorkspace appWorkspace() {
    return new AppWorkspace();
  }

  @Bean("appPropertiesInit")
  public ApplicationInit appPropertiesInit() {
    return new AppPropertiesRegisterInit();
  }

  @Bean("appBeanReadyInit")
  public ApplicationInit appBeanReadyInit() {
    return new AppBeanReadyInit();
  }

  @Bean("messageSource")
  public MessageSource messageSource(ValidatorProperties validatorProperties) {
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames(validatorProperties.getAllI18ns());
    messageSource.setCacheSeconds(-1);
    //messageSource.setCacheSeconds((int) ShortTimeUnit.HOURS.toSeconds(2));
    messageSource.setDefaultEncoding(DEFAULT_ENCODING);
    messageSource.setConcurrentRefresh(true);
    messageSource.setDefaultLocale(DEFAULT_LOCALE);
    return messageSource;
  }

  @Bean
  public AppWorkspaceEndpoint appWorkspaceEndpoint(AppWorkspace appWorkspace) {
    return new AppWorkspaceEndpoint(appWorkspace);
  }

  @Bean
  public CheckAppExpirationAspect checkAppExpirationAspect() {
    return new CheckAppExpirationAspect();
  }

  @Bean("PPS")
  public PrincipalPermissionService principalPermissionService() {
    return new PrincipalPermissionService();
  }

  @Bean
  public SpringContextHolder applicationContextProvider() {
    return new SpringContextHolder();
  }

  @Bean
  public NameJoinAspect nameJoinAspect() {
    return new NameJoinAspect();
  }

  @Bean
  public I18nMessageAspect i18nMessageAspect() {
    return new I18nMessageAspect();
  }

  @Bean
  public MessageEndpoint messageEndpoint() {
    return new MessageEndpoint();
  }

  @Bean
  public JoinSupplier nameJoinSupplier() {
    return new JoinSupplier();
  }

  /**
   * Fix:: java.nio.file.NoSuchFileException:
   * /tmp/undertow.1819.4153244082753775934/undertow6191513646749224630upload
   */
  @Bean
  public MultipartConfigElement multipartConfigElement(AppWorkspace appWorkspace,
      MultipartProperties multipartProperties) {
    MultipartConfigFactory factory = new MultipartConfigFactory();
    String uploadPath = emptySafe(multipartProperties.getLocation(),
        appWorkspace.getTmpDir() + "upload");
    File tmpFile = new File(uploadPath);
    if (!tmpFile.exists()) {
      tmpFile.mkdirs();
    }
    factory.setLocation(uploadPath);
    factory.setMaxFileSize(multipartProperties.getMaxFileSize());
    factory.setMaxRequestSize(multipartProperties.getMaxRequestSize());
    factory.setFileSizeThreshold(multipartProperties.getFileSizeThreshold());
    return factory.createMultipartConfig();
  }

  @Bean
  @ConditionalOnClass(FeignAutoConfiguration.class)
  public FeignRequestInterceptor requestInterceptor(ApplicationInfo applicationInfo) {
    return new FeignRequestInterceptor(applicationInfo);
  }

  @Bean
  public DefaultGlobalExceptionAdvice globalExceptionAdvice() {
    return new DefaultGlobalExceptionAdvice();
  }

  /**
   * //@see DispatcherServlet#initStrategies(ApplicationContext content)
   * <p>
   * //@see DispatcherServlet#buildLocaleContext(HttpServletRequest request)
   */
  @Bean(LOCALE_RESOLVER_BEAN_NAME)
  public LocaleResolver localeResolver(ApplicationInfo applicationInfo) {
    CookieLocaleResolver localeResolver = new CookieLocaleResolver();
    localeResolver.setCookieName(LOCALE_COOKIE_NAME);
    localeResolver.setDefaultLocale(DEFAULT_LOCALE);
    if (Objects.nonNull(applicationInfo.getTimezone())) {
      localeResolver.setDefaultTimeZone(TimeZone.getTimeZone(applicationInfo.getTimezone()));
    } else {
      localeResolver.setDefaultTimeZone(DEFAULT_TIME_ZONE);
    }
    localeResolver.setCookieMaxAge(24 * 60 * 60);
    return localeResolver;
  }

  @Bean
  public LocaleChangeInterceptor localeChangeInterceptor() {
    return new LocaleChangeInterceptor();
  }

  @Bean
  public FilterRegistrationBean<GlobalHoldFilter> registrationGlobalHoldFilterBean(
      ApplicationInfo applicationInfo, GlobalProperties globalProperties) {
    FilterRegistrationBean<GlobalHoldFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setName("globalHoldFilter");
    registrationBean.setFilter(new GlobalHoldFilter(applicationInfo, globalProperties));
    registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
    registrationBean.addUrlPatterns("/*");
    // Must be executed after RequestContextFilter(OrderedRequestContextFilter) to prevent being overwritten
    registrationBean.setOrder(REQUEST_WRAPPER_FILTER_MAX_ORDER - 100);
    return registrationBean;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(localeChangeInterceptor());
  }

  /**
   * It is important to configure configureMessageConverters() and specify a custom objectMapper
   * after implementing WebMvcConfigurer interface. Otherwise, an objectMapper will be created
   * internally, which will cause the custom serialization to not take effect.
   */
  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    converters.add(0, new ByteArrayToStringConverter());
    converters.add(new ResourceHttpMessageConverter());
    converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
  }


  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String baseUrl = StringUtils.trimTrailingCharacter(contextPath, '/');
    registry.addResourceHandler("/**")
        .addResourceLocations(webProperties.getResources().getStaticLocations())
        .resourceChain(true);
    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")
        .resourceChain(true);
    registry.addResourceHandler(baseUrl + "swagger-ui/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
        .resourceChain(true);
  }

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addViewController(contextPath + "swagger-ui/")
        .setViewName("forward:" + contextPath + "swagger-ui/index.html");
  }
}
