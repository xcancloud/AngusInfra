package cloud.xcan.angus.security;

import static cloud.xcan.sdf.spec.experimental.BizConstant.AUTH_RESOURCES_IN_FILTER;
import static org.springframework.boot.web.servlet.filter.OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER;

import cloud.xcan.angus.security.principal.HoldPrincipalFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author XiaoLong Liu
 */
@Configuration
public class OAuth2PrincipalHoldAutoConfigurer {

  OAuth2PrincipalHoldAutoConfigurer() {
  }

  @Bean
  public FilterRegistrationBean<HoldPrincipalFilter> registrationPrincipalFilterBean(
      ObjectMapper objectMapper) {
    FilterRegistrationBean<HoldPrincipalFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setName("holdPrincipalFilter");
    registrationBean.setFilter(new HoldPrincipalFilter(objectMapper));
    registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
    registrationBean.addUrlPatterns(AUTH_RESOURCES_IN_FILTER);
    registrationBean.setOrder(REQUEST_WRAPPER_FILTER_MAX_ORDER - 99);
    return registrationBean;
  }

}
