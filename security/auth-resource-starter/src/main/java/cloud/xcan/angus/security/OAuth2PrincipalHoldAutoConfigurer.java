package cloud.xcan.angus.security;

import static org.springframework.boot.web.servlet.filter.OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER;

import cloud.xcan.angus.security.principal.HoldPrincipalFilter;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author XiaoLong Liu
 */
@Configuration
class OAuth2PrincipalHoldAutoConfigurer {

  public static final String[] AUTH_RESOURCES = new String[]{
      "/api/*", "/openapi2p/*", "/view/*"};

  OAuth2PrincipalHoldAutoConfigurer() {
  }

  @Bean
  public FilterRegistrationBean<HoldPrincipalFilter> registrationPrincipalFilterBean(
      ApplicationContext applicationContext) {
    FilterRegistrationBean<HoldPrincipalFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setName("holdPrincipalFilter");
    registrationBean.setFilter(new HoldPrincipalFilter(applicationContext));
    registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
    registrationBean.addUrlPatterns(AUTH_RESOURCES);
    registrationBean.setOrder(REQUEST_WRAPPER_FILTER_MAX_ORDER - 99);
    return registrationBean;
  }

}
