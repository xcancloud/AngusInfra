package cloud.xcan.angus.core.fegin.interceptor;

import static cloud.xcan.angus.spec.SpecConstant.LOCALE_COOKIE_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.AUTHORIZATION;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.COOKIE;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.INVOKE_INSTANCE_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.INVOKE_SERVICE_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.OPT_TENANT_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.REQUEST_ID;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * Pre-defined custom RequestInterceptor for Feign Requests Authorization and Token Relay.
 * <p>
 * <br/>
 * <p>
 * SpringCloud OAuth2FeignRequestInterceptor When the token in the get request is empty, it will try
 * to get the token using the access token provider.
 *
 * @see org.springframework.cloud.openfeign.security.OAuth2FeignRequestInterceptor
 */
@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {

  private final ApplicationInfo applicationInfo;

  public FeignRequestInterceptor(ApplicationInfo applicationInfo) {
    this.applicationInfo = applicationInfo;
  }

  @Override
  public void apply(RequestTemplate template) {
    headerRelay(template);
  }

  private void headerRelay(RequestTemplate template) {
    // 1. set invoke source
    if (applicationInfo != null) {
      template.header(INVOKE_SERVICE_ID, applicationInfo.getArtifactId());
      template.header(INVOKE_INSTANCE_ID, applicationInfo.getInstanceId());
    }

    // 2. relay principal
    Principal principal = PrincipalContext.threadLocal.get();
    if (principal != null) {
      ApiType apiType = ApiType.findByUri(template.path());
      if (apiType.isUserTypeApi()) {
        template.header(AUTHORIZATION, principal.getAuthorization());
      }
      if (Objects.nonNull(principal.getOptTenantId())) {
        template.header(OPT_TENANT_ID, String.valueOf(principal.getOptTenantId()));
      }
      if (Objects.nonNull(principal.getRequestId())) {
        template.header(REQUEST_ID, principal.getRequestId());
      }
    }

    // 3. relay locale
    template.header(COOKIE, LOCALE_COOKIE_NAME + "=" + LocaleContextHolder.getLocale());
  }
}
