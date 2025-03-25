package cloud.xcan.angus.security.principal;


import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.angus.remote.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.angus.remote.message.CommSysException.M.PRINCIPAL_INFO_MISSING;
import static cloud.xcan.angus.remote.message.CommSysException.M.PRINCIPAL_INFO_MISSING_KEY;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.DENIED_OP_TENANT_ACCESS_T;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.FATAL_EXIT_KEY;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;
import static cloud.xcan.angus.spec.experimental.BizConstant.XCAN_TENANT_PLATFORM_CODE;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.remote.ApiResult;
import cloud.xcan.angus.security.OAuth2PrincipalHoldAutoConfigurer;
import cloud.xcan.angus.security.handler.CustomAuthenticationEntryPoint;
import cloud.xcan.angus.security.introspection.CustomOpaqueTokenIntrospector;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Hold identity information from introspect endpoint to the current request context.
 *
 * @author XiaoLong Liu
 * @see CustomOpaqueTokenIntrospector
 * @see OAuth2PrincipalHoldAutoConfigurer
 */
@Slf4j
public class HoldPrincipalFilter extends AbstractHoldPrincipal implements Filter {

  private static ObjectMapper objectMapper;

  public HoldPrincipalFilter(ObjectMapper objectMapper) {
    HoldPrincipalFilter.objectMapper = objectMapper;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain chain) throws IOException, ServletException {
    try {
      HttpServletRequest request = (HttpServletRequest) servletRequest;

      HttpServletResponse response = (HttpServletResponse) servletResponse;
      request.setCharacterEncoding(DEFAULT_ENCODING);

      Principal principal = PrincipalContext.get();
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication instanceof AnonymousAuthenticationToken) {
        writeApiResult(response, SC_BAD_REQUEST, PRINCIPAL_INFO_MISSING,
            PRINCIPAL_INFO_MISSING_KEY);
        return;
      }

      // Read identity information from endpoint(/oauth2/introspect) to the current request context
      boolean holdSuccess = holdAuthPrincipal(request, principal, authentication);
      if (!holdSuccess) {
        return;
      }

      // Only allow the operating platform to access other tenants.
      if (checkMultiTenantAccess(principal, response)) {
        return;
      }

      setResponseHeader(response, principal);
      setPrincipalTenantId(principal, request);

      chain.doFilter(request, response);
    } finally {
      PrincipalContext.remove();
    }
  }

  @Override
  public void destroy() {
  }

  private static boolean checkMultiTenantAccess(Principal principal, HttpServletResponse response)
      throws ServletException {
    if (isTenantClientOpMultiTenant(principal)) {
      log.error(String.format("User %s access tenant %s is denied", principal.getUserId(),
          principal.getOptTenantId()));
      writeApiResult(response, SC_FORBIDDEN, DENIED_OP_TENANT_ACCESS_T,
          FATAL_EXIT_KEY, new Object[]{principal.getOptTenantId()});
      return true;
    }
    return false;
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

  public static void writeApiResult(HttpServletResponse response, int status, String message,
      String eKey) throws ServletException {
    writeApiResult(response, status, message, eKey, null);
  }

  public static void writeApiResult(HttpServletResponse response, int status, String message,
      String eKey, Object[] messageArgs) throws ServletException {
    response.setHeader(Header.E_KEY, eKey);
    ApiResult<?> result = new ApiResult<>()
        .setCode(PROTOCOL_ERROR_CODE)
        .setMsg(MessageHolder.message(message, messageArgs))
        .setExt(Map.of(EXT_EKEY_NAME, eKey));
    writeJsonUtf8Result(response, status, result);
  }

  public static void writeJsonUtf8Result(HttpServletResponse response, int status, Object result)
      throws ServletException {
    CustomAuthenticationEntryPoint.writeJsonUtf8Result(objectMapper, response, status, result);
  }

}
