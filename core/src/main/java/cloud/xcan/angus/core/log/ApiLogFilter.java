package cloud.xcan.angus.core.log;

import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getApplicationInfo;
import static cloud.xcan.angus.core.utils.ServletUtils.getRequestId;
import static cloud.xcan.angus.remote.ApiConstant.EVENT_PATTERN;
import static cloud.xcan.angus.remote.ApiConstant.LOG_PATTERN;
import static cloud.xcan.angus.remote.ApiConstant.Service.EVENT_SERVICE;
import static cloud.xcan.angus.remote.ApiConstant.Service.LOGGER_SERVICE;
import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;
import static cloud.xcan.angus.spec.http.ContentTypes.isBinaryContent;
import static cloud.xcan.angus.spec.http.ContentTypes.isFormData;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getApiType;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getClientSource;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.api.enums.PrintLevel;
import cloud.xcan.angus.core.app.AppBeanReady;
import cloud.xcan.angus.core.disruptor.DisruptorQueueManager;
import cloud.xcan.angus.core.event.ApiLogEvent;
import cloud.xcan.angus.core.event.source.ApiLog;
import cloud.xcan.angus.core.log.ApiLogProperties.SystemRequest;
import cloud.xcan.angus.core.log.ApiLogProperties.UserRequest;
import cloud.xcan.angus.spec.experimental.BizConstant.ClientSource;
import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.spec.http.HttpStatus;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Depend on {@link `HoldPrincipalFilter`}, {@link PrincipalContext}
 *
 * @see feign.Logger
 * @see org.springframework.web.filter.AbstractRequestLoggingFilter
 */
@Slf4j
public class ApiLogFilter extends OncePerRequestFilter implements AppBeanReady {

  private final ApiLogProperties apiLogProperties;

  private final DisruptorQueueManager<ApiLogEvent> apiLogEventDisruptorQueue;

  private final Pattern logPattern = Pattern.compile(LOG_PATTERN);
  private final Pattern eventPattern = Pattern.compile(EVENT_PATTERN);

  /**
   * The change of value takes effect after restarting
   */
  private Pattern systemIgnorePattern;
  private Pattern systemPushLoggerIgnorePattern;

  /**
   * The change of value takes effect after restarting
   */
  private Pattern userIgnorePattern;
  private Pattern userPushLoggerIgnorePattern;

  public ApiLogFilter(ApiLogProperties apiLogProperties,
      DisruptorQueueManager<ApiLogEvent> apiLogEventDisruptorQueue) {
    this.apiLogProperties = apiLogProperties;
    this.apiLogEventDisruptorQueue = apiLogEventDisruptorQueue;
  }

  @Override
  public void ready() {
    if (nonNull(apiLogProperties)) {
      SystemRequest systemRequest = apiLogProperties.getSystemRequest();
      this.systemIgnorePattern = Pattern.compile(systemRequest.getIgnorePattern());
      if (nonNull(systemRequest.getPushLoggerServiceIgnorePattern())) {
        this.systemPushLoggerIgnorePattern = Pattern
            .compile(systemRequest.getPushLoggerServiceIgnorePattern());
      }
      UserRequest userAccessLog = apiLogProperties.getUserRequest();
      this.userIgnorePattern = Pattern.compile(userAccessLog.getIgnorePattern());
      if (nonNull(userAccessLog.getPushLoggerServiceIgnorePattern())) {
        this.userPushLoggerIgnorePattern = Pattern
            .compile(userAccessLog.getPushLoggerServiceIgnorePattern());
      }
    }
  }

  private static ContentCachingRequestWrapper wrapRequest(HttpServletRequest request) {
    if (request instanceof ContentCachingRequestWrapper) {
      return (ContentCachingRequestWrapper) request;
    } else {
      return new ContentCachingRequestWrapper(request);
    }
  }

  private static ContentCachingResponseWrapper wrapResponse(HttpServletResponse response) {
    if (response instanceof ContentCachingResponseWrapper) {
      return (ContentCachingResponseWrapper) response;
    } else {
      return new ContentCachingResponseWrapper(response);
    }
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (isAsyncDispatch(request) || !apiLogProperties.getEnabled()) {
      filterChain.doFilter(request, response);
      return;
    }

    String path = request.getRequestURI();
    if (path.startsWith("/api/auth/user") || path.startsWith("/pubapi/v1/auth/user")
        || path.startsWith("/innerapi/v1/log/") || path.startsWith("/actuator")
        || path.startsWith("/swagger") || "/".equals(path)) {
      filterChain.doFilter(request, response);
      return;
    }

    boolean isSystemLog = ClientSource.XCAN_SYS_TOKEN.equals(getClientSource());
    PrintLevel level;
    if (isSystemLog) {
      if (ignoreSystemLog(path)) {
        filterChain.doFilter(request, response);
        return;
      }
      level = apiLogProperties.getSystemRequest().getPrintLevel();
    } else {
      if (ignoreUserLog(path)) {
        filterChain.doFilter(request, response);
        return;
      }
      level = apiLogProperties.getUserRequest().getPrintLevel();
    }

    String requestId = getRequestId(request);
    long startMillis = currentTimeMillis();
    ApiLog apiLog = null;
    ContentCachingRequestWrapper wrapperRequest = wrapRequest(request);
    ContentCachingResponseWrapper wrapperResponse = wrapResponse(response);
    try {
      filterChain.doFilter(wrapperRequest, wrapperResponse);
      try {
        // Fix:: wrapper.getContentAsByteArray().length=0 -> Must read the http request through the wrapper after the request processing, the wrapper will cache the http request data after the request
        apiLog = logRequest(isSystemLog, level, requestId, wrapperRequest);
        logResponse(isSystemLog, level, requestId, wrapperResponse,
            currentTimeMillis() - startMillis, apiLog);
      } catch (Exception e) {
        e.printStackTrace();
      }
    } catch (IOException ioe) {
      throw logIoException(isSystemLog, level, requestId, ioe, currentTimeMillis()
          - startMillis, apiLog);
    } finally {
      if (nonNull(apiLog) && isPushToLoggerService(isSystemLog, path)) {
        apiLogEventDisruptorQueue.add(new ApiLogEvent(apiLog));
      }
    }
  }

  protected ApiLog logRequest(boolean systemLog, PrintLevel level, String requestId,
      ContentCachingRequestWrapper wrapper) {
    StringBuilder apiLogger = new StringBuilder();
    apiLogger.append("----- Http request log ------\n---> [").append(requestId).append("] ")
        .append(wrapper.getMethod()).append(" ")
        .append(wrapper.getRequestURL().toString()).append(" HTTP/1.1");
    boolean headersLevel = level.ordinal() >= PrintLevel.HEADERS.ordinal();
    boolean fullLevel = level.ordinal() >= PrintLevel.FULL.ordinal();
    HttpHeaders headers = new HttpHeaders();
    for (Enumeration<?> names = wrapper.getHeaderNames(); names.hasMoreElements(); ) {
      String headerName = (String) names.nextElement();
      for (Enumeration<?> headerValues = wrapper.getHeaders(headerName);
          headerValues.hasMoreElements(); ) {
        String headerValue = (String) headerValues.nextElement();
        if (Header.AUTHORIZATION.equals(headerName)) {
          // Desensitization authentication token
          // @see cloud.xcan.angus.api.commonlink.AASConstant.DEFAULT_TOKEN_SALT_LENGTH = 32;
          String[] takenFrames = headerValue.split("\\.");
          headerValue = takenFrames.length == 3 ? takenFrames[0] + ".********."
              + takenFrames[2] : headerValue;
        }
        headers.add(headerName, headerValue);
        if (headersLevel) {
          apiLogger.append("\n").append(headerName).append(": ").append(headerValue);
        }
      }
    }

    String queryString = wrapper.getQueryString();
    if (queryString != null && headersLevel) {
      apiLogger.append("\n").append(queryString);
    }
    String payload = "";
    byte[] buf = wrapper.getContentAsByteArray();
    int bodyLength = buf.length;
    if (fullLevel && bodyLength > 0) {
      if (wrapper.getContentType() != null && isBinaryContent(wrapper.getContentType())) {
        payload = "[Binary File]";
      } else if (wrapper.getContentType() != null && isFormData(wrapper.getContentType())) {
        payload = "[Multipart Form Data]";
      } else {
        int length = Math.min(bodyLength, apiLogProperties.getSystemRequest()
            .getMaxPayloadLength());
        try {
          payload = new String(buf, 0, length, DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException ex) {
          payload = "[unknown]";
        }
      }
      apiLogger.append("\n------ payload ------\n").append(payload);
    }
    if (fullLevel) {
      apiLogger.append("\n").append("---> END HTTP (").append(bodyLength).append("-byte body)");
    }
    log(systemLog, apiLogger.toString());
    return buildApiLogRequest(requestId, wrapper, bodyLength, headers, payload);
  }

  protected void logResponse(boolean systemLog, PrintLevel level, String requestId,
      ContentCachingResponseWrapper wrapper, long elapsedMillis, ApiLog apiLog) throws IOException {
    int status = wrapper.getStatus();
    boolean headersLevel = level.ordinal() >= PrintLevel.HEADERS.ordinal();
    boolean fullLevel = level.ordinal() >= PrintLevel.FULL.ordinal();
    StringBuilder apiLogger = new StringBuilder();
    apiLogger.append("----- Http response log ------\n<--- [").append(requestId)
        .append("] HTTP/1.1 ")
        .append(status).append(" (").append(elapsedMillis).append(" ms)");
    HttpHeaders headers = new HttpHeaders();
    for (String headerName : wrapper.getHeaderNames()) {
      Collection<String> headerValues = wrapper.getHeaders(headerName);
      for (String headerValue : headerValues) {
        headers.add(headerName, headerValue);
        if (headersLevel) {
          apiLogger.append("\n").append(headerName).append(": ").append(headerValue);
        }
      }
    }
    String payload = "";
    int bodyLength = 0;
    if (status != HttpStatus.NO_CONTENT.value && status != HttpStatus.RESET_CONTENT.value) {
      byte[] buf = wrapper.getContentAsByteArray();
      bodyLength = buf.length;
      if (fullLevel && bodyLength > 0) {
        if (wrapper.getContentType() != null && isBinaryContent(wrapper.getContentType())) {
          payload = "[Binary File]";
          // Fix:: } else if (wrapper.getContentType() != null && isFormData(wrapper.getContentType())) {
          //  payload = "[Multipart Form Data]";
        } else {
          int length = Math.min(bodyLength, apiLogProperties.getSystemRequest()
              .getMaxPayloadLength());
          try {
            payload = new String(buf, 0, length, DEFAULT_ENCODING);
          } catch (UnsupportedEncodingException ex) {
            payload = "[unknown]";
          }
        }
        apiLogger.append("\n------ payload ------\n").append(payload);
      }
    }
    if (fullLevel) {
      apiLogger.append("\n").append("<--- END HTTP (").append(bodyLength).append("-byte body)");
    }
    // IMPORTANT: copy content of response back into original response
    wrapper.copyBodyToResponse();
    log(systemLog, apiLogger.toString());
    buildApiLogResponse(elapsedMillis, apiLog, status, headers, payload, bodyLength);
  }

  protected IOException logIoException(boolean systemLog, PrintLevel logLevel, String requestId,
      IOException ioe, long elapsedMillis, ApiLog apiLog) {
    StringBuilder apiLogger = new StringBuilder();
    apiLogger.append("----- Http response log ------\n<--- [").append(requestId).append("] ERROR ")
        .append(ioe.getClass().getSimpleName()).append(ioe.getMessage())
        .append(" (").append(elapsedMillis).append(" ms)");
    StringWriter sw = new StringWriter();
    if (logLevel.ordinal() >= PrintLevel.FULL.ordinal()) {
      ioe.printStackTrace(new PrintWriter(sw));
      apiLogger.append("\n").append(sw.toString()).append("<--- END ERROR");
    }
    log(systemLog, apiLogger.toString());
    buildApiLogResponse(elapsedMillis, apiLog, 0, null, sw.toString(), 0);
    return ioe;
  }

  private ApiLog buildApiLogRequest(String requestId, HttpServletRequest request, int bodyLength,
      HttpHeaders headers, String payload) {
    Principal principal = PrincipalContext.get();
    return ApiLog.newBuilder().requestId(requestId)
        .remote(request.getRemoteAddr())
        .clientId(principal.getClientId()).clientSource(principal.getClientSource())
        .tenantId(principal.getTenantId()).tenantName(principal.getTenantName())
        .userId(principal.getUserId()).fullName(principal.getFullName())
        .serviceCode(principal.getServiceCode()).serviceName(principal.getServiceName())
        .instanceId(principal.getInstanceId())
        .apiType(principal.getApiType()).method(principal.getMethod()).uri(request.getRequestURI())
        .requestDate(principal.getRequestAcceptTime())
        .queryParam(request.getQueryString()).requestHeaders(getHeaders(headers))
        .requestBody(payload).requestSize(bodyLength)
        .build();
  }

  private void buildApiLogResponse(long elapsedMillis, ApiLog apiLog, int status,
      HttpHeaders headers, String payload, int bodyLength) {
    apiLog.setStatus(status).setResponseDate(LocalDateTime.now())
        .setResponseHeaders(getHeaders(headers))
        .setResponseBody(payload).setResponseSize(bodyLength)
        .setElapsedMillis(elapsedMillis);
  }

  protected void log(boolean systemLog, String logger) {
    if (log.isInfoEnabled() && isLoggerToFile(systemLog)) {
      log.info(logger);
    }
  }

  private boolean isLoggerServiceLogApi(String path) {
    return getApplicationInfo().getArtifactId()
        .equalsIgnoreCase(stringSafe(apiLogProperties.getLoggerService(), LOGGER_SERVICE))
        && logPattern.matcher(path).matches();
  }

  private boolean isEventServiceEventApi(String path) {
    return getApplicationInfo().getArtifactId()
        .equalsIgnoreCase(stringSafe(apiLogProperties.getEventService(), EVENT_SERVICE))
        && eventPattern.matcher(path).matches();
  }

  private boolean ignoreUserLog(String path) {
    return !apiLogProperties.getUserRequest().getEnabled()
        || (nonNull(userIgnorePattern) && userIgnorePattern.matcher(path).matches());
  }

  private boolean ignoreSystemLog(String path) {
    return !apiLogProperties.getSystemRequest().getEnabled()
        || (nonNull(userIgnorePattern) && systemIgnorePattern.matcher(path).matches());
  }

  private boolean isPushToLoggerService(boolean systemLog, String path) {
    // Important:: Ignore inner door or pub apis
    ApiType apiType = getApiType();
    if (Objects.isNull(apiType) || !apiType.isAuthApi()) {
      return false;
    }

    // if (isLoggerServiceLogApi(path) || isEventServiceEventApi(path)) {
    //  return false;
    // }

    if (systemLog) {
      return apiLogProperties.getSystemRequest().getPushLoggerService()
          && (Objects.isNull(systemPushLoggerIgnorePattern)
          || systemPushLoggerIgnorePattern.matcher(path).matches());
    }

    return apiLogProperties.getUserRequest().getPushLoggerService()
        && (Objects.isNull(userPushLoggerIgnorePattern)
        || userPushLoggerIgnorePattern.matcher(path).matches());
  }

  /**
   * Log file will be written when it is enabled.
   */
  private boolean isLoggerToFile(boolean systemLog) {
    return systemLog ? apiLogProperties.getSystemRequest().getEnabled()
        : apiLogProperties.getUserRequest().getEnabled();
  }

  private LinkedMultiValueMap<String, String> getHeaders(HttpHeaders headers) {
    if (CollectionUtils.isEmpty(headers)) {
      return null;
    }
    LinkedMultiValueMap<String, String> mvp = new LinkedMultiValueMap<>();
    for (String s : headers.keySet()) {
      mvp.put(s, headers.get(s));
    }
    return mvp;
  }

}

