package cloud.xcan.angus.security.principal;

import static cloud.xcan.sdf.api.message.CommSysException.M.PRINCIPAL_INFO_MISSING;
import static cloud.xcan.sdf.api.message.CommSysException.M.PRINCIPAL_INFO_MISSING_KEY;
import static cloud.xcan.sdf.spec.SpecConstant.DEFAULT_ENCODING;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.AUTHORITY;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.CLIENT_SOURCE_HUMP;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.DEFAULT_LANGUAGE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.DEFAULT_LANGUAGE_HUMP;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.DEFAULT_TIME_ZONE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.OAUTH2_REQUEST;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.POLICY_PREFIX;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.POLICY_TOP_PREFIX;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.PRINCIPAL;
import static cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey.REQUEST_PARAMETERS;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.ACCESS_TOKEN;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.CLIENT_ID;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.CLIENT_SOURCE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.OWNER_TENANT_ID;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.COUNTRY;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.DEVICE_ID;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.ITC;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.MAIN_DEPT_ID;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.SYS_ADMIN_FLAG;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.TENANT_ID;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.TENANT_NAME;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.TO_USER_FLAG;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.USERNAME;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.USER_DATA_ID;
import static cloud.xcan.sdf.spec.experimental.BizConstant.UserKey.USER_NAME;
import static cloud.xcan.sdf.spec.principal.Principal.DEFAULT_USER_ID;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.authuser.AuthUser;
import cloud.xcan.sdf.api.enums.DataScope;
import cloud.xcan.sdf.api.enums.GrantType;
import cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey;
import cloud.xcan.sdf.spec.experimental.BizConstant.ClientSource;
import cloud.xcan.sdf.spec.experimental.BizConstant.Header;
import cloud.xcan.sdf.spec.locale.SupportedLanguage;
import cloud.xcan.sdf.spec.principal.Principal;
import cloud.xcan.sdf.spec.principal.PrincipalContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

@Slf4j
public abstract class AbstractHoldPrincipal {

  public boolean holdAuthPrincipal(HttpServletResponse response,
      HttpServletRequest request, Principal principal, Authentication auth2Auth,
      OAuth2AuthorizationRequest authRequest) {
    boolean holdSuccess = false;
    if (auth2Auth.isAuthenticated()) {
      GrantType grantType = HoldPrincipalFilter.getGrantType(auth2Auth, authRequest);
      if (GrantType.CLIENT_CREDENTIALS.equals(grantType)) {
        holdSuccess = holdClientPrincipal(principal, request, auth2Auth, authRequest, grantType);
      } else {
        Object userAuth = auth2Auth.getPrincipal();
        if (userAuth instanceof AuthUser) {
          holdSuccess = holdUserPrincipal(request, response, principal, auth2Auth, authRequest,
              (AuthUser) userAuth, grantType);
        } else {
          holdSuccess = holdUserMapPrincipal(principal, request, response, auth2Auth,
              authRequest, grantType);
        }
      }
    }
    return holdSuccess;
  }

  public boolean holdUserPrincipal(HttpServletRequest request, HttpServletResponse response,
      Principal principal, Authentication auth2Auth, OAuth2AuthorizationRequest authRequest,
      AuthUser user, GrantType grantType) {
    boolean success = false;
    try {
      String clientId = authRequest.getClientId();
      Long tenantId = user.getTenantId();
      success = checkRequiredInfo(response, tenantId, clientId);
      if (success) {
        principal.setAuthorization(getAuthorization(request))
            .setAuthPassed(auth2Auth.isAuthenticated())
            .setGrantType(grantType)
            .setDefaultLanguage(user.getDefaultLanguage())
            .setDefaultTimeZone(user.getDefaultTimeZone())
            .setClientId(clientId)
            .setClientSource(user.getClientSource())
            .setTenantId(tenantId)
            .setTenantName(user.getTenantName())
            .setUserId(user.getId())
            .setUserFullname(user.getFullname())
            .setUsername(user.getUsername())
            .setSysAdminFlag(user.getSysAdminFlag())
            .setToUserFlag(user.getToUserFlag())
            .setMainDeptId(user.getMainDeptId())
            .setItc(user.getItc())
            .setCountry(user.getCountry())
            .setDeviceId(user.getDeviceId())
            .setAuthorities(getAuthoritiesRoles(user.getAuthorities()));
        if (log.isDebugEnabled()) {
          log.debug("Hold principal info : {}", principal);
        }
        PrincipalContext.set(principal);
      }
    } catch (Exception e) {
      log.error("Hold user principal error, cause: ", e);
    }
    return success;
  }

  public boolean holdUserMapPrincipal(Principal principal, HttpServletRequest req,
      HttpServletResponse response, Authentication auth2Auth,
      OAuth2AuthorizationRequest authRequest, GrantType grantType) {
    boolean success = false;
    try {
      Object details = auth2Auth.getDetails();
      if (!ObjectUtils.isEmpty(details) && details instanceof LinkedHashMap) {
        Map<String, Map<String, Object>> detailsMap = (Map<String, Map<String, Object>>) details;
        Map<String, Object> pInfo = detailsMap.get(PRINCIPAL);
        SupportedLanguage defaultLanguage = getSupportedLanguage(pInfo);
        String defaultTimeZone = nonNull(pInfo.get(DEFAULT_TIME_ZONE)) ?
            (String) pInfo.get(DEFAULT_TIME_ZONE) : null;
        String clientId = HoldPrincipalFilter.getClientId(authRequest, detailsMap);
        String clientSource = (String) pInfo.get(CLIENT_SOURCE_HUMP);
        Long tenantId = Long.valueOf((String) pInfo.get(TENANT_ID));
        String tenantName = (String) pInfo.get(TENANT_NAME);
        Long userId = Long.valueOf((String) pInfo.get(USER_DATA_ID));
        Long mainDeptId = nonNull(pInfo.get(MAIN_DEPT_ID)) ?
            Long.valueOf((String) pInfo.get(MAIN_DEPT_ID)) : null;
        String itc = nonNull(pInfo.get(ITC)) ? (String) pInfo.get(ITC) : null;
        String country = nonNull(pInfo.get(COUNTRY)) ? (String) pInfo.get(COUNTRY) : null;
        String deviceId = nonNull(pInfo.get(DEVICE_ID)) ? (String) pInfo.get(DEVICE_ID) : null;
        success = checkRequiredInfo(response, tenantId, clientId);
        if (success) {
          principal.setAuthorization(getAuthorization(req))
              .setAuthPassed(auth2Auth.isAuthenticated())
              .setGrantType(grantType)
              .setDefaultLanguage(defaultLanguage)
              .setDefaultTimeZone(defaultTimeZone)
              .setClientId(clientId)
              .setClientSource(clientSource)
              .setTenantId(tenantId).setTenantName(tenantName)
              .setUserId(userId)
              .setUserFullname((String) pInfo.get(USER_NAME))
              .setUsername((String) pInfo.get(USERNAME))
              .setSysAdminFlag((Boolean) pInfo.get(SYS_ADMIN_FLAG))
              .setToUserFlag((Boolean) pInfo.get(TO_USER_FLAG))
              .setMainDeptId(mainDeptId)
              .setItc(itc).setCountry(country)
              .setDeviceId(deviceId)
              .setAuthorities(getAuthoritiesRoles(
                  (ArrayList<LinkedHashMap<String, String>>) pInfo.get(AUTHORITY)));
          if (log.isDebugEnabled()) {
            log.debug("Hold principal info : {}", pInfo);
          }
          PrincipalContext.set(principal);
        }
      }
    } catch (Exception e) {
      log.error("Hold user principal error, cause: ", e);
    }
    return success;
  }

  public boolean holdClientPrincipal(Principal principal, HttpServletRequest request,
      Authentication auth2Auth, OAuth2AuthorizationRequest authRequest,
      GrantType grantType) {
    boolean success = false;
    try {
      Map<String, String> requestMap = getClientPrincipalMap(auth2Auth, authRequest);
      SupportedLanguage defaultLanguage = nonNull(requestMap.get(DEFAULT_LANGUAGE)) ?
          SupportedLanguage.valueOf(requestMap.get(DEFAULT_LANGUAGE)) : null;
      String defaultTimeZone = requestMap.get(DEFAULT_TIME_ZONE);
      Long tenantId = Long.parseLong(nullSafe(requestMap.get(AuthKey.TENANT_ID), "-1"));
      String tenantName = nullSafe(requestMap.get(AuthKey.TENANT_NAME), "");
      String clientId = nullSafe(requestMap.get(AuthKey.CLIENT_ID), "");
      String clientName = nullSafe(requestMap.get(AuthKey.CLIENT_NAME), "");
      String clientSource = nullSafe(requestMap.get(AuthKey.CLIENT_SOURCE), "");
      principal.setAuthorization(getAuthorization(request))
          .setAuthPassed(auth2Auth.isAuthenticated())
          .setGrantType(grantType)
          .setDefaultLanguage(defaultLanguage)
          .setDefaultTimeZone(defaultTimeZone)
          .setTenantId(tenantId)
          .setTenantName(tenantName)
          .setClientId(clientId)
          .setClientSource(clientSource)
          .setUserId(-1L)
          .setUserFullname(clientName/*default*/) // SystemToken[xxx]
          .setUsername(clientId/*default*/)
          .setSysAdminFlag(ClientSource.XCAN_SYS_TOKEN.equals(
              clientSource)/*After being set as a system administrator, you can only limit the permission of the api*/)
          .setToUserFlag(tenantId.equals(OWNER_TENANT_ID))
          .setMainDeptId(-1L)
          .setItc(null).setCountry(null);
      if (log.isDebugEnabled()) {
        log.debug("Hold client principal info : {}", principal);
      }
      PrincipalContext.set(principal);
      success = true;
    } catch (Exception e) {
      log.error("Hold client principal error, cause: ", e);
    }
    return success;
  }

  public boolean holdGatewayHeaderPrincipal(Principal principal, HttpServletRequest request,
      HttpServletResponse response) {
    boolean success = false;
    try {
      SupportedLanguage defaultLanguage = nonNull(request.getHeader(Header.LANGUAGE)) ?
          SupportedLanguage.valueOf(request.getHeader(Header.LANGUAGE)) : null;
      String defaultTimeZone = request.getHeader(Header.TIME_ZONE);
      String clientId = request.getHeader(CLIENT_ID);
      Long tenantId = getTenantId(request.getHeader(Header.TENANT_ID));
      String tenantName = request.getHeader(Header.TENANT_NAME);
      String userName = request.getHeader(Header.USER_FULLNAME);
      if (StringUtils.isNotEmpty(userName)) {
        userName = URLDecoder.decode(userName, DEFAULT_ENCODING);
      }
      if (StringUtils.isNotEmpty(tenantName)) {
        tenantName = URLDecoder.decode(tenantName, DEFAULT_ENCODING);
      }
      String username = request.getHeader(Header.USERNAME);
      success = checkRequiredInfo(response, tenantId, clientId);
      if (success) {
        principal.setAuthorization(getAuthorization(request))
            .setAuthPassed(true)
            .setGrantType(GrantType.of(request.getHeader(Header.GRANT_TYPE)))
            .setDefaultLanguage(defaultLanguage)
            .setDefaultTimeZone(defaultTimeZone)
            .setClientId(clientId)
            .setClientSource(request.getHeader(CLIENT_SOURCE))
            .setTenantId(tenantId)
            .setTenantName(tenantName)
            .setUserId(getUserId(request.getHeader(Header.USER_ID)))
            .setUserFullname(userName)
            .setUsername(username)
            .setSysAdminFlag(Boolean.parseBoolean(request.getHeader(Header.SYS_ADMIN_FLAG)))
            .setToUserFlag(Boolean.parseBoolean(request.getHeader(Header.TO_USER_FLAG)))
            .setMainDeptId(getDeptId(request.getHeader(Header.DEPT_ID)))
            .setItc(Header.ITC)
            .setCountry(Header.COUNTRY)
            .setDeviceId(Header.DEVICE_ID)
            .setAuthorities(getHeaderRoles(request.getHeader(Header.ROLES)))
            .setDataScope(getDataScope(request.getHeader(Header.DATA_SCOPE)));
        if (log.isDebugEnabled()) {
          log.debug("Hold principal info : {}", principal);
        }
        PrincipalContext.set(principal);
      }
    } catch (Exception e) {
      log.error("Hold principal error, cause: ", e);
    }
    return success;
  }

  public boolean checkRequiredInfo(HttpServletResponse response, Long tenantId, String clientId)
      throws ServletException {
    if (log.isDebugEnabled()) {
      log.debug("Check the required principal info : tenantId = {}, clientId = {}", tenantId,
          clientId);
    }
    boolean success = null != tenantId && StringUtils.isNotEmpty(clientId);
    if (!success) {
      HoldPrincipalFilter.writeApiResult(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          PRINCIPAL_INFO_MISSING, PRINCIPAL_INFO_MISSING_KEY, null);
    }
    return success;
  }

  private Map<String, String> getClientPrincipalMap(Authentication auth2Auth,
      OAuth2AuthorizationRequest authRequest) {
    Map<String, String> requestMap = new LinkedHashMap<>();
    Object objectMap = auth2Auth.getDetails();
    if (objectMap != null && objectMap instanceof LinkedHashMap) {
      Map<String, Object> requestObjectMap =
          (Map<String, Object>) ((LinkedHashMap) objectMap).get(OAUTH2_REQUEST);
      if (requestObjectMap != null) {
        requestMap = (Map<String, String>) requestObjectMap.get(REQUEST_PARAMETERS);
      }
    } else {
      Map<String, Object> additionalParameters = authRequest.getAdditionalParameters();
      requestMap = (Map<String, String>) additionalParameters.get(REQUEST_PARAMETERS);
    }
    return requestMap;
  }

  @Nullable
  private SupportedLanguage getSupportedLanguage(Map<String, Object> pInfo) {
    return nonNull(pInfo.get(DEFAULT_LANGUAGE_HUMP)) ? SupportedLanguage.valueOf(
        ((Map) pInfo.get("defaultLanguage")).values().iterator().next().toString()) : null;
  }

  public List<String> getHeaderRoles(String headerRole) {
    return StringUtils.isBlank(headerRole) ? Collections.emptyList()
        : Arrays.asList(headerRole.split(","));
  }

  public Long getTenantId(String tenantId) {
    return StringUtils.isEmpty(tenantId) ? null : Long.valueOf(tenantId);
  }

  public Long getOptTenantId(HttpServletRequest req) {
    String optTenantId = req.getHeader(Header.OPT_TENANT_ID);
    return StringUtils.isEmpty(optTenantId) ? null : Long.valueOf(optTenantId);
  }

  public String getAccessDeviceId(HttpServletRequest req) {
    return req.getHeader(Header.AUTH_DEVICE_ID);
  }

  public Long getUserId(String userId) {
    return StringUtils.isEmpty(userId) ? null : Long.valueOf(userId);
  }

  public Long getDeptId(String deptId) {
    return StringUtils.isEmpty(deptId) ? null : Long.valueOf(deptId);
  }

  public DataScope getDataScope(String dataScope) {
    return StringUtils.isEmpty(dataScope) ? DataScope.CREATOR : DataScope.valueOf(dataScope);
  }

  public static String getAuthorization(HttpServletRequest request) {
    String authorization = request.getHeader(Header.AUTHORIZATION);
    if (StringUtils.isEmpty(authorization)) {
      authorization = BEARER_TOKEN_TYPE + " " + request.getParameter(ACCESS_TOKEN);
    }
    return authorization;
  }

  public List<String> getAuthoritiesRoles(ArrayList<LinkedHashMap<String, String>> authorities) {
    if (CollectionUtils.isEmpty(authorities)) {
      return Collections.emptyList();
    }
    List<String> authRoleCodes = new ArrayList<>();
    for (LinkedHashMap<String, String> authority : authorities) {
      for (Map.Entry<String, String> entry : authority.entrySet()) {
        if (entry.getValue().startsWith(POLICY_PREFIX)
            || entry.getValue().startsWith(POLICY_TOP_PREFIX)) {
          authRoleCodes.add(entry.getValue());
        }
      }
    }
    return authRoleCodes;
  }

  public List<String> getAuthoritiesRoles(Collection<GrantedAuthority> authorities) {
    return CollectionUtils.isEmpty(authorities) ? Collections.emptyList()
        : authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
  }

  public void setResponseHeader(HttpServletResponse response, Principal principal) {
    response.setHeader(Header.AUTH_CLIENT_ID, principal.getClientId());
    response.setHeader(Header.AUTH_TENANT_ID, String.valueOf(principal.getTenantId()));
    if (!DEFAULT_USER_ID.equals(principal.getUserId())) {
      response.setHeader(Header.AUTH_USER_ID, String.valueOf(principal.getUserId()));
    }
  }

}
