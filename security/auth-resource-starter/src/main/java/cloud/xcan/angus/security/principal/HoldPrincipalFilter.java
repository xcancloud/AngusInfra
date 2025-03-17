package cloud.xcan.angus.security.principal;


import static cloud.xcan.sdf.api.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.sdf.api.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.sdf.api.message.CommSysException.M.PRINCIPAL_INFO_MISSING;
import static cloud.xcan.sdf.api.message.CommSysException.M.PRINCIPAL_INFO_MISSING_KEY;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.DENIED_OP_TENANT_ACCESS_T;
import static cloud.xcan.sdf.api.message.http.Forbidden.M.FATAL_EXIT_KEY;
import static cloud.xcan.sdf.spec.SpecConstant.DEFAULT_ENCODING;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.CLIENT_ID_HUMP;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.GRANT_TYPE_HUMP;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.OAUTH2_REQUEST;
import static cloud.xcan.sdf.spec.experimental.BizConstant.XCAN_TENANT_PLATFORM_CODE;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.ApiResult;
import cloud.xcan.sdf.api.enums.GrantType;
import cloud.xcan.sdf.spec.experimental.BizConstant.Header;
import cloud.xcan.sdf.spec.http.ContentType;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import cloud.xcan.sdf.spec.principal.Principal;
import cloud.xcan.sdf.spec.principal.PrincipalContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.ObjectUtils;

/**
 * Get request interface user information filter (for /api interface)
 *
 * @author XiaoLong Liu
 */
@Slf4j
public class HoldPrincipalFilter extends AbstractHoldPrincipal implements Filter {

  private  static ApplicationContext applicationContext;

  public HoldPrincipalFilter(ApplicationContext applicationContext) {
    HoldPrincipalFilter.applicationContext = applicationContext;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain chain) throws IOException, ServletException {
    try {
      HttpServletRequest request = (HttpServletRequest) servletRequest;
      String path = request.getRequestURI();

      if (path.startsWith("/api/auth/user") || path.startsWith("/actuator")
          || path.startsWith("/swagger") || "/".equals(path)) {
        chain.doFilter(servletRequest, servletResponse);
        return;
      }

      HttpServletResponse response = (HttpServletResponse) servletResponse;
      request.setCharacterEncoding(DEFAULT_ENCODING);

      Principal principal = PrincipalContext.get();
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication instanceof AnonymousAuthenticationToken) {
        writeApiResult(response, SC_BAD_REQUEST,
            PRINCIPAL_INFO_MISSING, PRINCIPAL_INFO_MISSING_KEY, null/*TODO*/);
        return;
      }

      boolean holdSuccess = holdAuthPrincipal(response, request, principal, authentication, null/*TODO*/);

      if (!holdSuccess) {
        return;
      }

      setResponseHeader(response, principal);

      Long optTenantId = nonNull(principal.getOptTenantId())
          ? principal.getOptTenantId() : getOptTenantId(request);
      if (nonNull(optTenantId)) {
        principal.setOptTenantId(optTenantId);
      }
      if (isTenantClientOpMultiTenant(principal)) {
        log.error(String.format("User %s access tenant %s is denied", principal.getUserId(),
            optTenantId));
        writeApiResult(response, SC_FORBIDDEN, DENIED_OP_TENANT_ACCESS_T,
            FATAL_EXIT_KEY, new Object[]{String.valueOf(optTenantId)});
        return;
      }

      chain.doFilter(request, response);
    } finally {
      PrincipalContext.remove();
    }
  }

  @Override
  public void destroy() {
  }

  public static boolean isTenantClientOpMultiTenant(Principal principal) {
    return isTenantClient(principal) && nonNull(principal.getOptTenantId())
        /* Fix: 1=1 */ && !principal.getOptTenantId().equals(principal.getTenantId());
  }

  /**
   * Check if the tenant client is visiting
   */
  public static boolean isTenantClient(Principal principal) {
    return XCAN_TENANT_PLATFORM_CODE.equals(principal.getClientId());
  }

  public static String getClientId(OAuth2AuthorizationRequest authRequest,
      Map<String, Map<String, Object>> detailsMap) {
    String clientId = authRequest.getClientId();
    if (ObjectUtils.isEmpty(clientId)) {
      Object co = detailsMap.get(OAUTH2_REQUEST).get(CLIENT_ID_HUMP);
      if (!ObjectUtils.isEmpty(co)) {
        clientId = (String) co;
      }
    }
    return clientId;
  }

  public static GrantType getGrantType(Authentication auth2Auth,
      OAuth2AuthorizationRequest authRequest) {
    String grantType = authRequest.getGrantType().getValue();
    if (StringUtils.isNotEmpty(grantType)) {
      return GrantType.of(grantType);
    }
    Object details = auth2Auth.getDetails();
    if (details != null) {
      if (!isEmpty(details) && details instanceof LinkedHashMap) {
        Map<String, Map<String, Object>> detailsMap = (Map<String, Map<String, Object>>) details;
        Object co = detailsMap.get(OAUTH2_REQUEST).get(GRANT_TYPE_HUMP);
        if (!isEmpty(co)) {
          grantType = (String) co;
          return GrantType.of(grantType);
        }
      }
    }
    return null;
  }

  public static void writeApiResult(HttpServletResponse response, int status, String message,
      String eKey, Object[] messageArgs) throws ServletException {
    response.setHeader(Header.E_KEY, eKey);
    ApiResult<?> result = new ApiResult<>().setCode(PROTOCOL_ERROR_CODE)
        .setMsg(MessageHolder.message(message, messageArgs))
        .setExt(Map.of(EXT_EKEY_NAME, eKey));
    writeJsonUtf8Result(response, status, result);
  }

  public static void writeJsonUtf8Result(HttpServletResponse response, int status, Object result)
      throws ServletException {
    response.setCharacterEncoding(DEFAULT_ENCODING);
    response.setContentType(ContentType.TYPE_JSON_UTF8);
    response.setStatus(status);
    try {
      if (nonNull(result)) {
        response.getWriter().write(applicationContext.getBean(ObjectMapper.class)
            .writeValueAsString(result));
        response.getWriter().flush();
      }
    } catch (Exception e) {
      throw new ServletException(e.getMessage());
    }
  }

}
