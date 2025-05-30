package cloud.xcan.angus.core.spring.filter;

import static cloud.xcan.angus.core.spring.boot.ApplicationInfo.APP_READY;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.isInnerApi;
import static cloud.xcan.angus.core.utils.ServletUtils.getAndSetRequestId;
import static cloud.xcan.angus.core.utils.ServletUtils.getAuthServiceCode;
import static cloud.xcan.angus.core.utils.ServletUtils.getDeviceId;
import static cloud.xcan.angus.core.utils.ServletUtils.getUserAgent;
import static cloud.xcan.angus.core.utils.ServletUtils.writeApiResult;
import static cloud.xcan.angus.remote.message.http.ServiceUnavailable.M.SERVICE_UNAVAILABLE_KEY;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_LOCALE;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_TIME_ZONE;
import static cloud.xcan.angus.spec.SpecConstant.LOCALE_COOKIE_NAME;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.CORS_CREDENTIALS;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.CORS_EXPOSE_HEADERS;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.CORS_HEADERS;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.CORS_METHODS;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.CORS_ORIGIN;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.DEVICE_ID_IN_QUERY;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.REMOTE_ADDR_IN_QUERY;
import static cloud.xcan.angus.spec.experimental.BizConstant.Header.USER_AGENT;
import static cloud.xcan.angus.spec.locale.SdfLocaleHolder.getLocale;
import static cloud.xcan.angus.spec.locale.SdfLocaleHolder.getTimeZone;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.spec.experimental.BizConstant.AuthKey;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.http.HttpRequestHeader;
import cloud.xcan.angus.spec.http.HttpStatus;
import cloud.xcan.angus.spec.locale.SdfLocaleHolder;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import cloud.xcan.angus.spec.thread.ThreadContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

/**
 * Question:
 * <p>
 * LocaleResolver is exposed in request content by DispatcherServlet, whereas
 * AuthenticationSuccessHandler is fired before request enters DispatcherServlet (actually, request
 * that fired SavedRequestAwareAuthenticationSuccessHandler never enters DispatcherServlet, because
 * this handler performs a redirect).
 * <p>
 * Note: That it must be executed before HoldPrincipalFilter.
 *
 * @author XiaoLong Liu
 */
@Slf4j
public class GlobalHoldFilter implements Filter {

  private final ApplicationInfo applicationInfo;
  private final GlobalProperties globalProperties;

  public GlobalHoldFilter(ApplicationInfo applicationInfo, GlobalProperties globalProperties) {
    this.applicationInfo = applicationInfo;
    this.globalProperties = globalProperties;
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    if (!APP_READY) {
      writeApiResult(response, HttpStatus.SERVICE_UNAVAILABLE.value,
          "Application is initializing", SERVICE_UNAVAILABLE_KEY, null);
      return;
    }

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    String path = request.getRequestURI();

    setCors(request, path, response);

    // Processing option debugging requests for browser
    if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
      response.setStatus(HttpStatus.OK.value);
      return;
    }

    boolean rootRequest = isEmpty(request.getHeader(Header.REQUEST_ID));

    MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
    Principal principal = assemblePrincipalRemote(mutableRequest, request, rootRequest);

    try {
      if (path.startsWith("/oauth2") || path.startsWith("/swagger")
          || path.startsWith("/eureka") || "/".equals(path)) {
        filterChain.doFilter(mutableRequest, servletResponse);
        return;
      }

      initLocaleHolder(request);

      assemblePrincipalRequest(request, path, principal);

      relayOptTenantId(request, principal);

      setResponseHeader(response, principal);

      MDC.put(AuthKey.REQUEST_ID, principal.getRequestId());

      PrincipalContext.set(principal);

      if (log.isDebugEnabled()) {
        log.debug("Hold principal, locale: {} ,timeZoneId: {}, requestId: {}",
            getLocale(), getTimeZone().getID(), principal.getRequestId());
      }

      filterChain.doFilter(mutableRequest, servletResponse);

    } finally {
      PrincipalContext.remove();
      ThreadContext.remove();
      if (log.isDebugEnabled()) {
        log.debug("Remove MDC requestId : {}", principal.getRequestId());
      }
      principal = null;
      LocaleContextHolder.resetLocaleContext();
      SdfLocaleHolder.resetLocaleContext();
      if (log.isDebugEnabled()) {
        log.debug("Remove MDC requestId : {}", MDC.get(AuthKey.REQUEST_ID));
      }
      MDC.remove(AuthKey.REQUEST_ID);
      if (rootRequest) {
        PrincipalContext.remoteRequestAttribute();
      }
    }
  }



  private void setCors(HttpServletRequest request, String path, HttpServletResponse response) {
    String proxy = request.getHeader(Header.NGINX_PROXY_CORS);
    if (log.isDebugEnabled()) {
      log.debug("Trace {} : {}", Header.NGINX_PROXY_CORS, proxy);
    }
    if ("true".equalsIgnoreCase(proxy)) {
      return;
    }
    if (globalProperties.isDefault(path) || globalProperties.allowedPaths(path)) {
      allowCors(request, response);
    }
  }

  private void relayOptTenantId(HttpServletRequest request, Principal principal) {
    if (isInnerApi()) {
      Long optTenantId = getOptTenantId(request);
      if (nonNull(optTenantId)) {
        principal.setOptTenantId(optTenantId);
      }
    }
  }

  private void initLocaleHolder(HttpServletRequest request) {
    Locale locale = null;
    TimeZone timeZone = null;

    // Retrieve and parse cookie value.
    Cookie cookie = WebUtils.getCookie(request, LOCALE_COOKIE_NAME);
    if (nonNull(cookie)) {
      String value = cookie.getValue();
      String localePart = value;
      String timeZonePart = null;
      int separatorIndex = localePart.indexOf('/');
      if (separatorIndex == -1) {
        // Leniently accept older cookies separated by a space...
        separatorIndex = localePart.indexOf(' ');
      }
      if (separatorIndex >= 0) {
        localePart = value.substring(0, separatorIndex);
        timeZonePart = value.substring(separatorIndex + 1);
      }
      try {
        locale = (!"-".equals(localePart) ? StringUtils.parseLocale(localePart) : null);
        if (timeZonePart != null) {
          timeZone = StringUtils.parseTimeZoneString(timeZonePart);
        }
      } catch (IllegalArgumentException ex) {
        log.warn("Parsed cookie value [" + cookie.getValue() + "] into locale '" + locale + "'");
      }
    }

    if (isNull(timeZone) && nonNull(applicationInfo.getTimezone())) {
      timeZone = TimeZone.getTimeZone(applicationInfo.getTimezone());
    }

    if (isNull(timeZone)) {
      timeZone = DEFAULT_TIME_ZONE;
    }

    locale = isNull(locale) ? DEFAULT_LOCALE : SupportedLanguage.safeLocale(locale);

    // Hold Locale and ZoneTime
    LocaleContextHolder.setLocale(locale);
    LocaleContextHolder.setTimeZone(timeZone);
    SdfLocaleHolder.setLocale(locale);
    SdfLocaleHolder.setTimeZone(timeZone);
  }

  private static @NotNull Principal assemblePrincipalRemote(
      MutableHttpServletRequest mutableRequest, HttpServletRequest request, boolean rootRequest) {
    // Request id is required for oauth2 generate user token
    String requestId = getAndSetRequestId(mutableRequest);
    String remoteAddr = request.getRemoteAddr();
    String userAgent = getUserAgent(request);
    String deviceId = getDeviceId(request);
    Principal principal = PrincipalContext.createIfAbsent();
    principal.setRequestId(requestId).setRemoteAddress(remoteAddr)
        .setUserAgent(userAgent).setDeviceId(deviceId);
    if (rootRequest){
      PrincipalContext.setRequestAttribute(REMOTE_ADDR_IN_QUERY, remoteAddr);
      PrincipalContext.setRequestAttribute(USER_AGENT, userAgent);
      PrincipalContext.setRequestAttribute(DEVICE_ID_IN_QUERY, deviceId);
    }
    return principal;
  }

  private void assemblePrincipalRequest(HttpServletRequest request, String path, Principal principal) {
    principal.setRequestAcceptTime(LocalDateTime.now())
        .setServiceCode(applicationInfo.getArtifactId())
        .setServiceName(applicationInfo.getName())
        .setAuthServiceCode(getAuthServiceCode(request))
        .setInstanceId(applicationInfo.getInstanceId())
        .setMethod(request.getMethod())
        .setUri(path)
        .setApiType(ApiType.findByUri(path))
        .setDefaultLanguage(SupportedLanguage.safeLanguage(SdfLocaleHolder.getLocale()))
        .setDefaultTimeZone(SdfLocaleHolder.getTimeZone().getID());
  }

  public Long getOptTenantId(HttpServletRequest req) {
    String optTenantId = req.getHeader(Header.OPT_TENANT_ID);
    return isEmpty(optTenantId) ? null : Long.valueOf(optTenantId);
  }

  private void setResponseHeader(HttpServletResponse response, Principal principal) {
    response.setHeader(Header.SERVICE_ID, principal.getServiceCode());
    response.setHeader(Header.INSTANCE_ID, principal.getInstanceId());
    response.setHeader(Header.REQUEST_ID, principal.getRequestId());
  }

  private void allowCors(HttpServletRequest request, HttpServletResponse response) {
    if (request.getHeader(HttpRequestHeader.Origin.getValue()) != null) {
      response.setHeader(CORS_ORIGIN, request.getHeader(HttpRequestHeader.Origin.getValue()));
    } else {
      response.setHeader(CORS_ORIGIN, globalProperties.getCors().getOrigin());
    }
    response.addHeader(CORS_CREDENTIALS, globalProperties.getCors().getCredentials());
    response.setHeader(CORS_HEADERS, globalProperties.getCors().getHeaders());
    response.setHeader(CORS_METHODS, globalProperties.getCors().getMethods());
    response.setHeader(CORS_EXPOSE_HEADERS, globalProperties.getCors().getExposeHeaders());
  }
}
