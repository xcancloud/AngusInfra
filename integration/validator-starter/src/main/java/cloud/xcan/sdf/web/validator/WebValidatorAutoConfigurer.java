
package cloud.xcan.sdf.web.validator;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.executable.ExecutableValidator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.messageinterpolation.AbstractMessageInterpolator;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.AggregateResourceBundleLocator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.validation.beanvalidation.FilteredMethodValidationPostProcessor;
import org.springframework.boot.validation.beanvalidation.MethodValidationExcludeFilter;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.LocaleContextMessageInterpolator;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * @author liuxiaolong
 * @see ValidationAutoConfiguration
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
//@AutoConfigureOrder(1)
@EnableConfigurationProperties({ValidatorProperties.class})
@ConditionalOnClass(ExecutableValidator.class)
@ConditionalOnResource(resources = "classpath:META-INF/services/javax.validation.spi.ValidationProvider")
@Import(PrimaryDefaultValidatorPostProcessor.class)
@ConditionalOnProperty(name = "xcan.validator.enabled", havingValue = "true", matchIfMissing = false)
public class WebValidatorAutoConfigurer {

  public WebValidatorAutoConfigurer() {
    log.info("Web validator auto configuration is enabled");
  }

  /**
   * Debug interpolate bundle message see {@link AbstractMessageInterpolator#interpolate(Context,
   * Locale, String)}
   * <p>
   * Note:: Spring inject locale to bundle message see {@link LocaleContextMessageInterpolator#interpolate(String,
   * Context)}
   */
  @Bean
  public Validator validator(ValidatorProperties validatorProperties) {
    ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
        .configure()
        .addProperty("hibernate.validator.fail_fast", "true")
        // Note: Testing classpath:/ in xcan-angus is not supported！ Supported in spring MessageSource?
        .messageInterpolator(new ResourceBundleMessageInterpolator(
            new AggregateResourceBundleLocator(List.of(validatorProperties.getAllI18ns()).stream()
                .map(x -> x.replaceFirst("classpath:/", "")).collect(
                    Collectors.toList()))))
        .buildValidatorFactory();
    return validatorFactory.getValidator();
  }

  @Primary
  @Bean
  public LocalValidatorFactoryBean defaultValidator(MessageSource messageSource) {
    LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean();
    //    MessageInterpolatorFactory interpolatorFactory = new MessageInterpolatorFactory();
    //    factoryBean.setMessageInterpolator(interpolatorFactory.getObject());
    factoryBean.getValidationPropertyMap().put("hibernate.validator.fail_fast", "true");
    factoryBean.setValidationMessageSource(messageSource);
    return factoryBean;
  }

  @Bean
  @ConditionalOnMissingBean
  public MethodValidationPostProcessor methodValidationPostProcessor(Environment environment,
      @Lazy Validator validator,
      ObjectProvider<MethodValidationExcludeFilter> excludeFilters) {
    FilteredMethodValidationPostProcessor processor = new FilteredMethodValidationPostProcessor(
        excludeFilters.orderedStream());
    boolean proxyTargetClass = environment
        .getProperty("spring.aop.proxy-target-class", Boolean.class, true);
    processor.setProxyTargetClass(proxyTargetClass);
    processor.setValidator(validator);
    return processor;
  }


}
