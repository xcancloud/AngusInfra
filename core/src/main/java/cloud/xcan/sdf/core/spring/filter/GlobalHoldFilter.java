package cloud.xcan.sdf.core.spring.filter;

import static cloud.xcan.sdf.api.message.http.ServiceUnavailable.M.SERVICE_UNAVAILABLE_KEY;
import static cloud.xcan.sdf.core.spring.boot.ApplicationInfo.APP_READY;
import static cloud.xcan.sdf.core.utils.ServletUtils.getAndSetRequestId;
import static cloud.xcan.sdf.core.utils.ServletUtils.getAuthServiceCode;
import static cloud.xcan.sdf.core.utils.ServletUtils.getUserAgent;
import static cloud.xcan.sdf.core.utils.ServletUtils.writeApiResult;
import static cloud.xcan.sdf.spec.SpecConstant.DEFAULT_LOCALE;
import static cloud.xcan.sdf.spec.SpecConstant.DEFAULT_TIME_ZONE;
import static cloud.xcan.sdf.spec.SpecConstant.LOCALE_COOKIE_NAME;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.CORS_CREDENTIALS;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.CORS_EXPOSE_HEADERS;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.CORS_HEADERS;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.CORS_METHODS;
import static cloud.xcan.sdf.spec.experimental.BizConstant.Header.CORS_ORIGIN;
import static cloud.xcan.sdf.spec.locale.SdfLocaleHolder.getLocale;
import static cloud.xcan.sdf.spec.locale.SdfLocaleHolder.getTimeZone;
import static cloud.xcan.sdf.spec.utils.ClassUtils.classSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.enums.ApiType;
import cloud.xcan.sdf.api.obf.Str0;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import cloud.xcan.sdf.core.utils.CoreUtils;
import cloud.xcan.sdf.core.utils.PrincipalContextUtils;
import cloud.xcan.sdf.spec.experimental.BizConstant.AuthKey;
import cloud.xcan.sdf.spec.experimental.BizConstant.Header;
import cloud.xcan.sdf.spec.http.HttpMethod;
import cloud.xcan.sdf.spec.http.HttpStatus;
import cloud.xcan.sdf.spec.locale.SdfLocaleHolder;
import cloud.xcan.sdf.spec.locale.SupportedLanguage;
import cloud.xcan.sdf.spec.principal.Principal;
import cloud.xcan.sdf.spec.principal.PrincipalContext;
import cloud.xcan.sdf.spec.thread.ThreadContext;
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
import java.util.Objects;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
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
 *
 * @author liuxiaolong
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
          new Str0(new long[]{0x951D76D4266ABD85L, 0xD3A612CFFC6629A1L, 0x11DBBD0F6421BC2L,
              0xAE8238277FD6B65DL, 0x23D537B076804FE0L})
              .toString() /* => "Application is initializing" */, SERVICE_UNAVAILABLE_KEY, null);
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

    if (path.startsWith("/api/auth/user") || path.startsWith("/swagger") || "/".equals(path)) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    Principal principal = PrincipalContext.create();
    try {
      MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);

      holdAndInitLocale(request);

      holdPrincipal(request, path, principal, mutableRequest);
      PrincipalContext.set(principal);
      // ThreadContext.create(); -> Set on business code

      // For /doorapi
      if (PrincipalContextUtils.isDoorApi()) {
        Long optTenantId = getOptTenantId(request);
        if (nonNull(optTenantId)) {
          principal.setOptTenantId(optTenantId);
        }
      }

      setResponseHeader(response, principal);

      if (log.isDebugEnabled()) {
        log.debug(new Str0(new long[]{0xF8BCCB8FC3FF2B57L, 0xD53BD967DE6F6A59L, 0x75678ED0AE50945BL,
                0x56C6AD5D241CF79L, 0xCEB2B1A1101B7D7AL, 0x9E2DC812AC2E3C1CL, 0x997C01824C5CBB67L,
                0xD4DC8A204B95ED4AL, 0x4900FE08A1319997L})
                .toString() /* => "Hold principal, locale: {} ,timeZoneId: {}, requestId: {}" */,
            getLocale(), getTimeZone().getID(), principal.getRequestId());
      }

      // Add requestId to MDC
      MDC.put(AuthKey.REQUEST_ID, principal.getRequestId());

      filterChain.doFilter(mutableRequest, servletResponse);

    } finally {
      PrincipalContext.remove();
      ThreadContext.remove();
      if (log.isDebugEnabled()) {
        log.debug(new Str0(new long[]{0xB0084D3CCD769F84L, 0xDFB7F66F205E57FFL, 0x18124C9366441414L,
            0xAAC633AB02D56ED3L, 0x74177CD171D61294L})
            .toString() /* => "Remove MDC requestId : {}" */, principal.getRequestId());
      }
      principal = null;
      LocaleContextHolder.resetLocaleContext();
      SdfLocaleHolder.resetLocaleContext();
      if (log.isDebugEnabled()) {
        log.debug(new Str0(new long[]{0x37AFF0693EA73930L, 0x53755FC142A1D5B4L, 0xA80612F2E3CD5CEAL,
            0x640C839A861AB21CL, 0x3AEBAFD1F7EC59F3L})
            .toString() /* => "Remove MDC requestId : {}" */, MDC.get(AuthKey.REQUEST_ID));
      }
      MDC.remove(AuthKey.REQUEST_ID);
    }
  }

  private void setCors(HttpServletRequest request, String path, HttpServletResponse response) {
    String proxy = request.getHeader(Header.NGINX_PROXY_CORS);
    if (log.isDebugEnabled()) {
      log.debug(new Str0(new long[]{0xF7DF889A2A8268A9L, 0x9C38DC60D0DEF2CEL, 0x5AFD131223F661E7L})
          .toString() /* => "Trace {} : {}" */, Header.NGINX_PROXY_CORS, proxy);
    }
    if ("true".equalsIgnoreCase(proxy)) {
      return;
    }
    if (globalProperties.isDefault(path) || globalProperties.allowedPaths(path)) {
      allowCors(response);
    }
  }

  private void holdAndInitLocale(HttpServletRequest request) {
    Locale locale = null;
    TimeZone timeZone = null;

    // Retrieve and parse cookie value.
    Cookie cookie = WebUtils.getCookie(request, LOCALE_COOKIE_NAME);
    if (cookie != null) {
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
        log.warn(new Str0(new long[]{0xB4868A81A976C489L, 0xFB892E4C53656C13L, 0x47CB7822503FC4CL,
            0xBFDF0C0D164B3776L}).toString() /* => "Parsed cookie value [" */
            + cookie.getValue() + new Str0(
            new long[]{0xD281F85B9AD531A7L, 0xDAF571CC3856E648L, 0x982A4A28A21B0C07L})
            .toString() /* => "] into locale '" */ + locale + "'" + "");
      }
    }

    if (Objects.isNull(timeZone) && Objects.nonNull(applicationInfo.getTimezone())) {
      timeZone = TimeZone.getTimeZone(applicationInfo.getTimezone());
    }

    if (Objects.isNull(timeZone)) {
      timeZone = DEFAULT_TIME_ZONE;
    }

    locale = Objects.isNull(locale) ? DEFAULT_LOCALE : SupportedLanguage.safeLocale(locale);

    // Hold Locale and ZoneTime
    LocaleContextHolder.setLocale(locale);
    LocaleContextHolder.setTimeZone(timeZone);
    SdfLocaleHolder.setLocale(locale);
    SdfLocaleHolder.setTimeZone(timeZone);

    /*if (applicationInfo.isPrivateEdition()) {
      TODO allowRequest(0.001);
    }*/
  }

  private void holdPrincipal(HttpServletRequest request, String path, Principal principal,
      MutableHttpServletRequest mutableRequest) {
    principal.setRequestId(getAndSetRequestId(mutableRequest))
        .setRemoteAddress(request.getRemoteAddr())
        .setUserAgent(getUserAgent(request))
        .setRequestAcceptTime(LocalDateTime.now())
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
    return org.apache.commons.lang3.StringUtils.isEmpty(optTenantId) ? null
        : Long.valueOf(optTenantId);
  }

  private void setResponseHeader(HttpServletResponse response, Principal principal) {
    response.setHeader(Header.SERVICE_ID, principal.getServiceCode());
    response.setHeader(Header.INSTANCE_ID, principal.getInstanceId());
    response.setHeader(Header.REQUEST_ID, principal.getRequestId());
  }

  /**
   * TODO Referring to the nginx configuration, cross-domain is allowed when the top-level domain
   * name is the same.
   *
   * <pre>
   *             set $flag '0';
   *             if ( $http_origin ~* ^(http?://.*.xcan.cloud$) ){
   *                  set $flag '1';
   *             }
   *
   *             if ($flag = '1') {
   *                  add_header 'Access-Control-Allow-Origin' $http_origin;
   *                  add_header 'Access-Control-Allow-Credentials' 'true';
   *                  add_header 'Access-Control-Allow-Methods' 'GET,POST,PUT,PATCH,DELETE';
   *                  add_header 'Access-Control-Allow-Headers' *;
   *             }
   * </pre>
   *
   * @see `CorsConfigBuilder`
   */
  private void allowCors(HttpServletResponse response) {
    response.addHeader(CORS_CREDENTIALS, globalProperties.getCors().getCredentials());
    response.setHeader(CORS_ORIGIN, globalProperties.getCors().getOrigin());
    response.setHeader(CORS_HEADERS, globalProperties.getCors().getHeaders());
    response.setHeader(CORS_METHODS, globalProperties.getCors().getMethods());
    response.setHeader(CORS_EXPOSE_HEADERS, globalProperties.getCors().getExposeHeaders());
  }

  public static void allowRequest(double percentage/*0.0 - 0.00000000xxx*/) {
    if (Math.random() >= (1 - percentage) && !classSafe(
        new Str0(new long[]{0x83505C3E3021A0C8L, 0xC2BE3F893A036E4EL, 0xFA6D8BD76708279AL,
            0x87EC910EE6BE4B47L}).toString() /* => "LcsProtector.class" */,
        new Str0(new long[]{0x79BC7124C661D8D0L, 0x5C0D38D96812F913L, 0x3DC6B6F229594C8BL,
            0x37A819459C7787F3L, 0x6A1824B582AEB37DL, 0xABB768265C24B7A3L, 0xA5F6E95531977E4FL})
            .toString() /* => "cloud.xcan.sdf.core.store.infra.job.LcsProtector" */,
        new Str0(new long[]{0x2B37208459D27866L, 0x7EC471F3D9904D1EL, 0xC6480433C265C344L,
            0x482B2AEBD29D21B4L, 0xF1E8C40438DC2055L})
            .toString() /* => "72449bc93109249a8e999e6c8df297f5" */)) {
      System.out.println(new Str0(
          new long[]{0x4D6B884552E4C4F3L, 0xBF84BF51153F1A5DL, 0xCC5FF9D4D8615D6FL,
              0x24D55B22669AE4D8L, 0xAA86F9360851A198L, 0x164FA8B6633D702CL, 0x66E4B88F2BF07F47L,
              0x906530EAC5D560CCL, 0x655232875C96A69CL, 0xF6CC461FF4FD0DB3L, 0x2C139F60BC5812F7L})
          .toString() /* => "Critical warning, license signature verification error, system forced exit" */);
      CoreUtils.exitApp();
    }
  }
}
