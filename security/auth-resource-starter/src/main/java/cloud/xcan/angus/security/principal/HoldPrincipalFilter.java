package cloud.xcan.angus.security.principal;


import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.angus.remote.ApiConstant.EXT_EKEY_NAME;
import static cloud.xcan.angus.remote.message.SysException.M.PRINCIPAL_MISSING;
import static cloud.xcan.angus.remote.message.SysException.M.PRINCIPAL_MISSING_KEY;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.DENIED_OP_TENANT_ACCESS_T;
import static cloud.xcan.angus.remote.message.http.Forbidden.M.FATAL_EXIT_KEY;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_COUNTRY;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DEFAULT_LANGUAGE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DEFAULT_TIMEZONE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_FULL_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_GRANT_TYPE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_MAIN_DEPT_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_PERMISSION;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_PRINCIPAL;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_SYS_ADMIN;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_TENANT_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_TENANT_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_TO_USER;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_USERNAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_REQUEST_AGENT;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_REQUEST_DEVICE_ID;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_REQUEST_REMOTE_ADDR;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.ACCESS_TOKEN;
import static cloud.xcan.angus.spec.experimental.BizConstant.XCAN_TENANT_PLATFORM_CODE;
import static cloud.xcan.angus.spec.principal.Principal.DEFAULT_USER_ID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.GrantType;
import cloud.xcan.angus.remote.ApiResult;
import cloud.xcan.angus.remote.message.SysException;
import cloud.xcan.angus.security.handler.CustomAuthenticationEntryPoint;
import cloud.xcan.angus.security.introspection.CustomOpaqueTokenIntrospector;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Hold identity information from introspect endpoint to the current request thread context.
 * <p>
 * Note: Non-secure authentication requests will initialize an empty Principal.
 *
 * @author XiaoLong Liu
 * @see CustomOpaqueTokenIntrospector
 */
@Slf4j
public class HoldPrincipalFilter extends OncePerRequestFilter {

  private static ObjectMapper objectMapper;

  private final static AntPathRequestMatcher[] AUTH_API_MATCHERS = new AntPathRequestMatcher[]{
      new AntPathRequestMatcher("/api/**"),
      new AntPathRequestMatcher("/innerapi/**"),
      new AntPathRequestMatcher("/openapi2p/**"),
      new AntPathRequestMatcher("/openapi/**")
  };

  public HoldPrincipalFilter(ObjectMapper objectMapper) {
    HoldPrincipalFilter.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    try {
      // Important: Set thread context for all requests
      Principal principal = PrincipalContext.createIfAbsent();

      boolean isMatched = false;
      for (AntPathRequestMatcher matcher : AUTH_API_MATCHERS) {
        if (matcher.matches(request)) {
          isMatched = true;
          break;
        }
      }
      if (!isMatched) {
        chain.doFilter(request, response);
        return;
      }

      request.setCharacterEncoding(DEFAULT_ENCODING);

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication instanceof AnonymousAuthenticationToken) {
        writeApiResult(response, SC_BAD_REQUEST, PRINCIPAL_MISSING, PRINCIPAL_MISSING_KEY);
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
    super.destroy();
  }

  public boolean holdAuthPrincipal(
      HttpServletRequest request, Principal principal, Authentication authentication) {
    boolean holdSuccess = false;
    if (authentication.isAuthenticated()) {
      if (authentication instanceof BearerTokenAuthentication bearerTokenAuthentication) {
        Map<String, Object> tokenAttributes = bearerTokenAuthentication.getTokenAttributes();
        GrantType grantType = GrantType.of(
            tokenAttributes.get(INTROSPECTION_CLAIM_NAMES_GRANT_TYPE).toString());
        if (GrantType.CLIENT_CREDENTIALS.equals(grantType)) {
          holdSuccess = holdClientPrincipal(request, principal, tokenAttributes, grantType);
        } else if (GrantType.PASSWORD.equals(grantType)) {
          holdSuccess = holdUserPrincipal(request, principal, tokenAttributes, grantType);
        } else {
          throw SysException.of("Unsupported grant type: " + grantType);
        }
      }
    }
    return holdSuccess;
  }

  @SuppressWarnings("unchecked")
  public boolean holdClientPrincipal(HttpServletRequest request, Principal principal,
      Map<String, Object> attributes, GrantType grantType) {
    // @formatter:off
    try {
      Object clientId = attributes.get(OAuth2TokenIntrospectionClaimNames.CLIENT_ID).toString();
      Map<String, Object> client = (Map<String, Object>) attributes.get(INTROSPECTION_CLAIM_NAMES_PRINCIPAL);
      Object tenantId0 = client.get(INTROSPECTION_CLAIM_NAMES_TENANT_ID);
      // Client authentication tenant ID is not mandatory
      Long tenantId = nonNull(tenantId0) ? Long.parseLong(tenantId0.toString()) : -1;
      Object tenantName = client.get(INTROSPECTION_CLAIM_NAMES_TENANT_NAME);
      Object clientSource = client.get(INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE);
      Object clientName = client.get(INTROSPECTION_CLAIM_NAMES_CLIENT_NAME);
      Object userAgent = attributes.get(INTROSPECTION_CLAIM_NAMES_REQUEST_AGENT);
      Object deviceId = attributes.get(INTROSPECTION_CLAIM_NAMES_REQUEST_DEVICE_ID);
      Object remoteAddr = attributes.get(INTROSPECTION_CLAIM_NAMES_REQUEST_REMOTE_ADDR);
      principal.setAuthorization(getAuthorization(request))
          .setAuthenticated(true).setGrantType(grantType)
          .setUri(request.getRequestURI()).setMethod(request.getMethod())
          .setDefaultLanguage(SupportedLanguage.defaultLanguage()) // TODO Tenant level settings should be used
          .setDefaultTimeZone(null) // TODO Tenant level settings should be used
          .setTenantId(tenantId).setTenantName(nonNull(tenantName)? tenantName.toString() : null)
          .setClientId(clientId.toString())
          .setClientSource(nonNull(clientSource) ? clientSource.toString() : null)
          .setUserId(-1L).setFullName(nonNull(clientName) ? clientName.toString() : null/*default*/) // SystemToken[xxx]
          .setUsername(clientId.toString()/*default*/).setSysAdmin(false)
          .setToUser(false).setMainDeptId(-1L).setCountry(null)
          .setDeviceId(nonNull(deviceId) ? deviceId.toString() : null)
          .setUserAgent(nonNull(userAgent) ? userAgent.toString() : null)
          .setRemoteAddress(nonNull(remoteAddr) ? remoteAddr.toString() : null);
      if (log.isDebugEnabled()) {
        log.debug("Hold client principal info : {}", principal);
      }
      return true;
    } catch (Exception e) {
      log.error("Hold client principal error, cause: ", e);
    }
    // @formatter:on
    return false;
  }

  @SuppressWarnings("unchecked")
  public boolean holdUserPrincipal(HttpServletRequest request,
      Principal principal, Map<String, Object> attributes, GrantType grantType) {
    // @formatter:off
    try {
      Object clientId = attributes.get(OAuth2TokenIntrospectionClaimNames.CLIENT_ID).toString();
      Map<String, Object> user = (Map<String, Object>) attributes.get(INTROSPECTION_CLAIM_NAMES_PRINCIPAL);
      Object tenantId = user.get(INTROSPECTION_CLAIM_NAMES_TENANT_ID);
      Object username = user.get(INTROSPECTION_CLAIM_NAMES_USERNAME);
      Object id = user.get(INTROSPECTION_CLAIM_NAMES_ID);
      Object fullName = user.get(INTROSPECTION_CLAIM_NAMES_FULL_NAME);
      Object sysAdmin = user.get(INTROSPECTION_CLAIM_NAMES_SYS_ADMIN);
      Object toUser = user.get(INTROSPECTION_CLAIM_NAMES_TO_USER);
      Object mainDeptId = user.get(INTROSPECTION_CLAIM_NAMES_MAIN_DEPT_ID);
      Object tenantName = user.get(INTROSPECTION_CLAIM_NAMES_TENANT_NAME);
      Object country = user.get(INTROSPECTION_CLAIM_NAMES_COUNTRY);
      Object clientSource = user.get(INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE);
      Object defaultLanguage = user.get(INTROSPECTION_CLAIM_NAMES_DEFAULT_LANGUAGE);
      Object defaultTimeZone = user.get(INTROSPECTION_CLAIM_NAMES_DEFAULT_TIMEZONE);
      Object permissions = attributes.get(INTROSPECTION_CLAIM_NAMES_PERMISSION);
      Object userAgent = attributes.get(INTROSPECTION_CLAIM_NAMES_REQUEST_AGENT);
      Object deviceId = attributes.get(INTROSPECTION_CLAIM_NAMES_REQUEST_DEVICE_ID);
      Object remoteAddr = attributes.get(INTROSPECTION_CLAIM_NAMES_REQUEST_REMOTE_ADDR);
      principal.setAuthorization(getAuthorization(request)).setAuthenticated(true).setGrantType(grantType)
          .setUri(request.getRequestURI()).setMethod(request.getMethod())
          .setDefaultLanguage(nonNull(defaultLanguage) ? SupportedLanguage.valueOf(defaultLanguage.toString()) : SupportedLanguage.defaultLanguage())
          .setDefaultTimeZone(nonNull(defaultTimeZone) ? defaultTimeZone.toString() : null)
          .setClientId(clientId.toString()).setClientSource(nonNull(clientSource) ? clientSource.toString() : null)
          .setTenantId(Long.valueOf(tenantId.toString())).setTenantName(nonNull(tenantName)? tenantName.toString() : null)
          .setUserId(nonNull(id) ? Long.valueOf(id.toString()) : null)
          .setFullName(nonNull(fullName) ? fullName.toString() : null)
          .setUsername(nonNull(username) ? username.toString() : null)
          .setSysAdmin(nonNull(sysAdmin) && Boolean.parseBoolean(sysAdmin.toString()))
          .setToUser(nonNull(toUser) && Boolean.parseBoolean(toUser.toString()))
          .setMainDeptId(nonNull(mainDeptId) ? Long.valueOf(mainDeptId.toString()) : null)
          .setCountry(nonNull(country) ? country.toString() : null)
          .setDeviceId(nonNull(deviceId) ? deviceId.toString() : null)
          .setUserAgent(nonNull(userAgent) ? userAgent.toString() : null)
          .setRemoteAddress(nonNull(remoteAddr) ? remoteAddr.toString() : null)
          .setPermissions(isNull(permissions) ? Collections.emptyList()
              : ((ArrayList<Object>)permissions).stream().map(Object::toString).collect(Collectors.toList()));
      if (log.isDebugEnabled()) {
        log.debug("Hold principal info : {}", principal);
      }
      return true;
    } catch (Exception e) {
      log.error("Hold user principal error, cause: ", e);
    }
    // @formatter:on
    return false;
  }

  public String getAccessDeviceId(HttpServletRequest req) {
    return req.getHeader(Header.AUTH_DEVICE_ID);
  }

  public static String getAuthorization(HttpServletRequest request) {
    String authorization = request.getHeader(Header.AUTHORIZATION);
    if (isEmpty(authorization)) {
      authorization = BEARER_TOKEN_TYPE + " " + request.getParameter(ACCESS_TOKEN);
    }
    return authorization;
  }

  public void setResponseHeader(HttpServletResponse response, Principal principal) {
    response.setHeader(Header.AUTH_CLIENT_ID, principal.getClientId());
    response.setHeader(Header.AUTH_TENANT_ID, String.valueOf(principal.getTenantId()));
    if (!DEFAULT_USER_ID.equals(principal.getUserId())) {
      response.setHeader(Header.AUTH_USER_ID, String.valueOf(principal.getUserId()));
    }
  }

  public void setPrincipalTenantId(Principal principal, HttpServletRequest request) {
    Long optTenantId = nonNull(principal.getOptTenantId())
        ? principal.getOptTenantId() : getOptTenantId(request);
    if (nonNull(optTenantId)) {
      principal.setOptTenantId(optTenantId);
    }
  }

  private Long getOptTenantId(HttpServletRequest req) {
    String optTenantId = req.getHeader(Header.OPT_TENANT_ID);
    return isEmpty(optTenantId) ? null : Long.valueOf(optTenantId);
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
