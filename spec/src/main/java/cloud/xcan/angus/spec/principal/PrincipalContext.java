package cloud.xcan.angus.spec.principal;


import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.api.enums.DataScope;
import cloud.xcan.angus.api.enums.GrantType;
import cloud.xcan.angus.api.enums.Platform;
import cloud.xcan.angus.api.enums.ResourceAclType;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PrincipalContext {

  /**
   * The InheritableThreadLocal class is a subclass of ThreadLocal. Instead of each thread having
   * its own value inside a ThreadLocal, the InheritableThreadLocal grants access to values to a
   * thread and all child threads created by that thread.
   */
  public static ThreadLocal<Principal> tl = new InheritableThreadLocal<>();

  public static void set(Principal principal) {
    tl.set(principal);
  }

  public static Principal create() {
    Principal principal = new Principal();
    tl.set(principal);
    return principal;
  }

  public static Principal createIfAbsent(){
    Principal principal = tl.get();
    if (principal == null) {
      principal = new Principal();
      tl.set(principal);
    }
    return principal;
  }

  public static Principal get() {
    Principal principal = tl.get();
    if (principal == null) {
      principal = new Principal(); // Only temp principal
      // tl.set(principal);
    }
    return principal;
  }

  public static void remove() {
    tl.remove();
  }

  /**
   * Tenant level setting
   */
  public static SupportedLanguage getDefaultLanguage() {
    SupportedLanguage defaultLanguage = get().getDefaultLanguage();
    return Objects.isNull(defaultLanguage) ? SupportedLanguage.defaultLanguage() : defaultLanguage;
  }

  /**
   * Platform level setting
   */
  @DoInFuture("Support tenant level setting")
  public static String getDefaultTimeZone() {
    return get().getDefaultTimeZone();
  }

  public static String getClientId() {
    return get().getClientId();
  }

  public static String getClientSource() {
    return get().getClientSource();
  }

  public static String getServiceCode() {
    return get().getServiceCode();
  }

  public static String getServiceName() {
    return get().getServiceName();
  }

  public static String getResourceCode() {
    return get().getResourceCode();
  }

  public static String getInstanceId() {
    return get().getInstanceId();
  }

  public static String getMethod() {
    return get().getMethod();
  }

  public static String getUri() {
    return get().getUri();
  }

  public static String getRemoteAddress() {
    return get().getRemoteAddress();
  }

  public static String getUserAgent() {
    return get().getUserAgent();
  }

  public static boolean isAuthPassed() {
    return get().isAuthenticated();
  }

  public static GrantType getGrantType() {
    return get().getGrantType();
  }

  public static Platform getAccessType() {
    return get().getPlatformScope();
  }

  public static ApiType getApiType() {
    return get().getApiType();
  }

  public static String getAuthorization() {
    return get().getAuthorization();
  }

  public static String getToken() {
    String authorization = get().getAuthorization();
    return isNotEmpty(authorization) ? authorization.split(" ")[1] : null;
  }

  public static DataScope getDataScope() {
    return get().getDataScope();
  }

  public static ResourceAclType getAccessScope() {
    return get().getResourceAclType();
  }

  public static String getAuthServiceCode() {
    return get().getAuthServiceCode();
  }

  public static String getRequestId() {
    return get().getRequestId();
  }

  public static LocalDateTime getRequestAcceptTime() {
    return get().getRequestAcceptTime();
  }

  public static Long getTenantId() {
    return get().getTenantId();
  }

  public static String getTenantName() {
    return get().getTenantName();
  }

  public static Long getUserId() {
    return get().getUserId();
  }

  public static String getUserFullname() {
    return get().getFullname();
  }

  public static String getUsername() {
    return get().getUsername();
  }

  public static String getCountry() {
    return get().getCountry();
  }

  public static String getDeviceId() {
    return get().getDeviceId();
  }

  public static Long getDeptId() {
    return get().getMainDeptId();
  }

  public static List<String> getAuthorities() {
    return get().getPermissions();
  }

  public static Map<String, ?> getExtensions() {
    return get().getExtensions();
  }

  public static boolean containExtension(String key) {
    Principal principal = get();
    return principal.getExtensions().containsKey(key);
  }

  public static Object getExtension(String key) {
    return get().getExtensions().get(key);
  }

  public static Map<String, ?> setExtensions(Map<String, Object> extension) {
    Principal principal = get();
    principal.setExtensions(extension);
    return principal.getExtensions();
  }

  public static Map<String, ?> addExtension(String key, Object value) {
    Principal principal = get();
    principal.getExtensions().put(key, value);
    return principal.getExtensions();
  }

  public static Map<String, ?> addExtension(String key, Collection<Object> values) {
    Principal principal = get();
    principal.getExtensions().put(key, values);
    return principal.getExtensions();
  }
}
