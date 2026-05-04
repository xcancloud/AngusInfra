package cloud.xcan.angus.core.spring.rest;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.QueryParameterUtils.parseQueryString;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

import cloud.xcan.angus.spec.utils.StreamUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ProxyRequest", description = "Http  Request Proxy - Proxy HTTP request endpoints via AngusTester to resolve cross-origin restrictions (CORS) and enforce security policies.")
@Validated
@RestController
@RequestMapping(ProxyRequestRest.PROXY_ENDPOINT)
public class ProxyRequestRest {

  public static final String PROXY_ENDPOINT = "/pubapi/v1/proxy";
  public static final String TARGET_ADDR_PARAMETER = "targetAddr";

  private static final SimpleClientHttpRequestFactory REQUEST_FACTORY =
      new SimpleClientHttpRequestFactory();

  /**
   * Hop-by-hop headers must not be forwarded (RFC 7230).
   */
  private static final Set<String> HOP_BY_HOP_REQUEST_HEADERS = Set.of(
      "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
      "te", "trailer", "transfer-encoding", "upgrade", "host");

  private static final Set<String> HOP_BY_HOP_RESPONSE_HEADERS = Set.of(
      "connection", "keep-alive", "proxy-authenticate", "proxy-authorization",
      "te", "trailer", "transfer-encoding", "upgrade");

  @Operation(summary = "Call other servers and return the results directly to the client",
      hidden = true, operationId = "proxy:request:pub")
  @RequestMapping(value = "/**")
  public void proxy(HttpServletRequest request, HttpServletResponse response)
      throws IOException, URISyntaxException {
    URI newUri = getFullTargetUri(request);
    ClientHttpRequest delegateRequest =
        REQUEST_FACTORY.createRequest(newUri, parseHttpMethod(request.getMethod()));
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      if (shouldSkipRequestHeader(headerName)) {
        continue;
      }
      Enumeration<String> values = request.getHeaders(headerName);
      delegateRequest.getHeaders().addAll(headerName, Collections.list(values));
    }
    StreamUtils.copy(request.getInputStream(), delegateRequest.getBody());

    try (ClientHttpResponse clientHttpResponse = delegateRequest.execute()) {
      response.setStatus(clientHttpResponse.getStatusCode().value());
      clientHttpResponse.getHeaders().forEach((key, values) -> {
        if (shouldSkipResponseHeader(key)) {
          return;
        }
        values.forEach(value -> response.addHeader(key, value));
      });
      StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
    }
  }

  private static HttpMethod parseHttpMethod(String methodName) {
    try {
      return HttpMethod.valueOf(methodName.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      assertTrue(false, "Unsupported HTTP method: " + methodName);
      throw ex;
    }
  }

  private URI getFullTargetUri(HttpServletRequest request) throws URISyntaxException {
    String path = new URI(request.getRequestURI()).getPath();
    String query = request.getQueryString();
    Map<String, Deque<String>> queryParameters =
        isEmpty(query) ? null : parseQueryString(query, UTF_8);
    assertTrue(isNotEmpty(queryParameters) && queryParameters.containsKey(TARGET_ADDR_PARAMETER),
        "Parameter targetAddr is required");
    String targetAddr = queryParameters.get(TARGET_ADDR_PARAMETER).getFirst();
    String fullTargetUrl = targetAddr + path.replace(PROXY_ENDPOINT, "");
    String forwardQuery = buildForwardQueryString(queryParameters);
    if (isNotEmpty(forwardQuery)) {
      fullTargetUrl = fullTargetUrl + "?" + forwardQuery;
    }
    return new URI(fullTargetUrl);
  }

  /**
   * Rebuilds the query string for the upstream URL, omitting {@link #TARGET_ADDR_PARAMETER} so the
   * target host does not receive the proxy routing parameter.
   */
  private static String buildForwardQueryString(Map<String, Deque<String>> queryParameters) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (Map.Entry<String, Deque<String>> entry : queryParameters.entrySet()) {
      if (TARGET_ADDR_PARAMETER.equals(entry.getKey())) {
        continue;
      }
      String encKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
      Deque<String> values = entry.getValue();
      if (values.isEmpty()) {
        if (!first) {
          sb.append('&');
        }
        first = false;
        sb.append(encKey).append('=');
      } else {
        for (String val : values) {
          if (!first) {
            sb.append('&');
          }
          first = false;
          sb.append(encKey).append('=')
              .append(URLEncoder.encode(val != null ? val : "", StandardCharsets.UTF_8));
        }
      }
    }
    return sb.length() == 0 ? null : sb.toString();
  }

  private static boolean shouldSkipRequestHeader(@Nullable String name) {
    return name == null || HOP_BY_HOP_REQUEST_HEADERS.contains(name.toLowerCase(Locale.ROOT));
  }

  private static boolean shouldSkipResponseHeader(@Nullable String name) {
    return name == null || HOP_BY_HOP_RESPONSE_HEADERS.contains(name.toLowerCase(Locale.ROOT));
  }
}
