package cloud.xcan.angus.core.spring.rest;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.spring.rest.ProxyRequestRest.PROXY_ENDPOINT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.QueryParameterUtils.parseQueryString;
import static org.apache.commons.codec.CharEncoding.UTF_8;

import cloud.xcan.angus.spec.utils.StreamUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "ProxyRequest", description = "Http  Request Proxy - Proxy HTTP request endpoints via AngusTester to resolve cross-origin restrictions (CORS) and enforce security policies.")
@Validated
@RestController
@RequestMapping(PROXY_ENDPOINT)
public class ProxyRequestRest {

  public static final String PROXY_ENDPOINT = "/pubapi/v1/proxy";
  public static final String TARGET_ADDR_PARAMETER = "targetAddr";

  @Operation(summary = "Call other servers and return the results directly to the client",
      hidden = true, operationId = "proxy:request:pub")
  @RequestMapping(value = "/**")
  public void proxy(HttpServletRequest request, HttpServletResponse response)
      throws IOException, URISyntaxException {
    // 1. Assemble new uri
    URI newUri = getFullTargetUri(request);
    // 2. Create proxy request
    String methodName = request.getMethod();
    ClientHttpRequest delegateRequest = new SimpleClientHttpRequestFactory()
        .createRequest(newUri, HttpMethod.valueOf(methodName));
    Enumeration<String> headerNames = request.getHeaderNames();
    // 3. Set request headers
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> v = request.getHeaders(headerName);
      List<String> arr = new ArrayList<>();
      while (v.hasMoreElements()) {
        arr.add(v.nextElement());
      }
      delegateRequest.getHeaders().addAll(headerName, arr);
    }
    // 3. Set request body
    StreamUtils.copy(request.getInputStream(), delegateRequest.getBody());
    // 4. Execute remote calls
    try (ClientHttpResponse clientHttpResponse = delegateRequest.execute()) {
      response.setStatus(clientHttpResponse.getStatusCode().value());
      // 5. Set response header
      clientHttpResponse.getHeaders().forEach((key, value) -> value.forEach(it -> {
        response.setHeader(key, it);
      }));
      // 5. Set response body
      StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
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
    if (!query.isEmpty() && !"null".equals(query)) {
      fullTargetUrl = fullTargetUrl + "?" + query;
    }
    return new URI(fullTargetUrl);
  }

}
