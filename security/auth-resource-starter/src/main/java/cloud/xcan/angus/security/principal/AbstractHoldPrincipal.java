package cloud.xcan.angus.security.principal;

import static cloud.xcan.angus.remote.ApiConstant.ECode.SYSTEM_ERROR_CODE;
import static cloud.xcan.angus.remote.message.CommSysException.M.PRINCIPAL_INFO_MISSING;
import static cloud.xcan.angus.remote.message.CommSysException.M.PRINCIPAL_INFO_MISSING_KEY;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_NAME;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_COUNTRY;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DEFAULT_LANGUAGE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DEFAULT_TIMEZONE;
import static cloud.xcan.angus.security.model.SecurityConstant.INTROSPECTION_CLAIM_NAMES_DEVICE_ID;
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
import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.BEARER_TOKEN_TYPE;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.ACCESS_TOKEN;
import static cloud.xcan.angus.spec.principal.Principal.DEFAULT_USER_ID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.enums.GrantType;
import cloud.xcan.angus.remote.message.CommSysException;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

@Slf4j
public abstract class AbstractHoldPrincipal {

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
          throw CommSysException.of("Unsupported grant type: " + grantType);
        }
      }
    }
    return holdSuccess;
  }

  public boolean holdClientPrincipal(HttpServletRequest request, Principal principal,
      Map<String, Object> attributes, GrantType grantType) {
    // @formatter:off
    try {
      Object clientId = attributes.get(OAuth2TokenIntrospectionClaimNames.CLIENT_ID).toString();
      Map<String, Object> clientPrincipal = (Map<String, Object>) attributes.get(
          INTROSPECTION_CLAIM_NAMES_PRINCIPAL);
      Object tenantId0 = clientPrincipal.get(INTROSPECTION_CLAIM_NAMES_TENANT_ID);
      // Client authentication tenant ID is not mandatory
      Long tenantId = nonNull(tenantId0) ? Long.parseLong(tenantId0.toString()) : -1;
      if (checkRequiredInfo(tenantId, clientId)) {
        Object tenantName = clientPrincipal.get(INTROSPECTION_CLAIM_NAMES_TENANT_NAME);
        Object clientSource = clientPrincipal.get(INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE);
        Object clientName = clientPrincipal.get(INTROSPECTION_CLAIM_NAMES_CLIENT_NAME);
        principal.setAuthorization(getAuthorization(request)).setAuthenticated(true).setGrantType(grantType)
            .setUri(request.getRequestURI()).setMethod(request.getMethod())
            .setDefaultLanguage(SupportedLanguage.defaultLanguage()) // TODO Tenant level settings should be used
            .setDefaultTimeZone(null) // TODO Tenant level settings should be used
            .setTenantId(tenantId).setTenantName(nonNull(tenantName)? tenantName.toString() : null)
            .setClientId(clientId.toString()).setClientSource(nonNull(clientSource) ? clientSource.toString() : null)
            .setUserId(-1L).setFullname(nonNull(clientName) ? clientName.toString() : null/*default*/) // SystemToken[xxx]
            .setUsername(clientId.toString()/*default*/).setSysAdmin(false).setToUser(false).setMainDeptId(-1L).setCountry(null);
        if (log.isDebugEnabled()) {
          log.debug("Hold client principal info : {}", principal);
        }
      }
      return true;
    } catch (Exception e) {
      log.error("Hold client principal error, cause: ", e);
    }
    // @formatter:on
    return false;
  }

  public boolean holdUserPrincipal(HttpServletRequest request,
      Principal principal, Map<String, Object> attributes, GrantType grantType) {
    // @formatter:off
    try {
      Object clientId = attributes.get(OAuth2TokenIntrospectionClaimNames.CLIENT_ID).toString();
      Map<String, Object> userPrincipal = (Map<String, Object>) attributes.get(INTROSPECTION_CLAIM_NAMES_PRINCIPAL);
      Object tenantId = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_TENANT_ID);
      if (checkRequiredInfo(tenantId, clientId)) {
        Object username = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_USERNAME);
        Object id = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_ID);
        Object fullName = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_FULL_NAME);
        Object sysAdmin = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_SYS_ADMIN);
        Object toUser = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_TO_USER);
        Object mainDeptId = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_MAIN_DEPT_ID);
        Object tenantName = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_TENANT_NAME);
        Object country = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_COUNTRY);
        Object clientSource = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_CLIENT_SOURCE);
        Object deviceId = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_DEVICE_ID);
        Object defaultLanguage = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_DEFAULT_LANGUAGE);
        Object defaultTimeZone = userPrincipal.get(INTROSPECTION_CLAIM_NAMES_DEFAULT_TIMEZONE);
        Object permissions = attributes.get(INTROSPECTION_CLAIM_NAMES_PERMISSION);
        principal.setAuthorization(getAuthorization(request)).setAuthenticated(true).setGrantType(grantType)
            .setUri(request.getRequestURI()).setMethod(request.getMethod())
            .setDefaultLanguage(nonNull(defaultLanguage) ? SupportedLanguage.valueOf(defaultLanguage.toString()) : SupportedLanguage.defaultLanguage())
            .setDefaultTimeZone(nonNull(defaultTimeZone) ? defaultTimeZone.toString() : null)
            .setClientId(clientId.toString()).setClientSource(nonNull(clientSource) ? clientSource.toString() : null)
            .setTenantId(Long.valueOf(tenantId.toString())).setTenantName(nonNull(tenantName)? tenantName.toString() : null)
            .setUserId(nonNull(id) ? Long.valueOf(id.toString()) : null)
            .setFullname(nonNull(fullName) ? fullName.toString() : null)
            .setUsername(nonNull(username) ? username.toString() : null)
            .setSysAdmin(nonNull(sysAdmin) && Boolean.parseBoolean(sysAdmin.toString()))
            .setToUser(nonNull(toUser) && Boolean.parseBoolean(toUser.toString()))
            .setMainDeptId(nonNull(mainDeptId) ? Long.valueOf(mainDeptId.toString()) : null)
            .setCountry(nonNull(country) ? country.toString() : null)
            .setDeviceId(nonNull(deviceId) ? deviceId.toString() : null)
            .setPermissions(isNull(permissions) ? Collections.emptyList()
                : ((ArrayList<Object>)permissions).stream().map(Object::toString).collect(Collectors.toList()));
        if (log.isDebugEnabled()) {
          log.debug("Hold principal info : {}", principal);
        }
        return true;
      }
    } catch (Exception e) {
      log.error("Hold user principal error, cause: ", e);
    }
    // @formatter:on
    return false;
  }

  public boolean checkRequiredInfo(Object tenantId, Object clientId) {
    if (log.isDebugEnabled()) {
      log.debug("Check the required principal info : tenantId = {}, clientId = {}", tenantId,
          clientId);
    }
    boolean success = null != tenantId && null != clientId && isNotEmpty(clientId.toString());
    if (!success) {
      throw CommSysException.of(SYSTEM_ERROR_CODE, PRINCIPAL_INFO_MISSING,
          PRINCIPAL_INFO_MISSING_KEY);
    }
    return success;
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
}
