package cloud.xcan.angus.core.spring.rest;

import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotNull;
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
@RequestMapping("/pubapi/v1/proxy")
public class ProxyRequestRest {

  @Operation(description = "Call other servers and return the results directly to the client", operationId = "proxy:request:pub")
  @RequestMapping(value = "/**")
  public void proxy(HttpServletRequest request, HttpServletResponse response)
      throws IOException, URISyntaxException {
    // Assemble new uri
    URI newUri = getFullTargetUri(request);
    // Execute proxy queries
    String methodName = request.getMethod();
    HttpMethod httpMethod = HttpMethod.valueOf(methodName);
    assertNotNull(httpMethod, String.format("httpMethod %s is not supported", methodName));
    ClientHttpRequest delegate = new SimpleClientHttpRequestFactory()
        .createRequest(newUri, httpMethod);
    Enumeration<String> headerNames = request.getHeaderNames();
    // Set request headers
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> v = request.getHeaders(headerName);
      List<String> arr = new ArrayList<>();
      while (v.hasMoreElements()) {
        arr.add(v.nextElement());
      }
      delegate.getHeaders().addAll(headerName, arr);
    }
    StreamUtils.copy(request.getInputStream(), delegate.getBody());
    // Execute remote calls
    try (ClientHttpResponse clientHttpResponse = delegate.execute()) {
      response.setStatus(clientHttpResponse.getStatusCode().value());
      // Set response header
      clientHttpResponse.getHeaders().forEach((key, value) -> value.forEach(it -> {
        response.setHeader(key, it);
      }));
      StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
    }
  }

  private URI getFullTargetUri(HttpServletRequest request) throws URISyntaxException {
    URI uri = new URI(request.getRequestURI());
    String path = uri.getPath();
    String query = request.getQueryString();
    Map<String, Deque<String>> queryParameters = isEmpty(query)
        ? null : parseQueryString(query, UTF_8);
    String targetAddr = isNotEmpty(queryParameters) && queryParameters.containsKey("targetAddr")
        ? queryParameters.get("targetAddr").getFirst() : null;
    assertNotNull(targetAddr, "Parameter targetAddr is required");
    String fullTargetUrl = targetAddr + path.replace("/pubapi/v1/proxy", "");
    if (!query.isEmpty() && !"null".equals(query)) {
      fullTargetUrl = fullTargetUrl + "?" + query;
    }
    return new URI(fullTargetUrl);
  }

}
