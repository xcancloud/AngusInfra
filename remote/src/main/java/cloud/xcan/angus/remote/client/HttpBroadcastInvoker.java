package cloud.xcan.angus.remote.client;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getAuthorization;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.BizConstant.AuthKey;
import cloud.xcan.angus.spec.http.HttpSender;
import cloud.xcan.angus.spec.http.HttpSender.Request;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.http.HttpUrlConnectionSender;
import cloud.xcan.angus.spec.utils.JsonUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpBroadcastInvoker {

  private final HttpSender httpSender;
  private final ServiceDiscoveryHelper serviceDiscoveryHelper;

  public HttpBroadcastInvoker(ServiceDiscoveryHelper serviceDiscoveryHelper) {
    this.httpSender = new HttpUrlConnectionSender();
    this.serviceDiscoveryHelper = serviceDiscoveryHelper;
  }

  /**
   * Broadcasts a request to all instances of a service.
   *
   * @param serviceId Target service ID (e.g., "user-service")
   * @param apiPath   API endpoint path (e.g., "/api/v1/notify")
   * @param payload   Request payload
   */
  public Map<String, ApiLocaleResult<?>> broadcast(
      String serviceId, String apiPath, Object payload) throws Throwable {
    List<String> instanceUrls = serviceDiscoveryHelper.getAllInstanceUrls(serviceId);
    Map<String, ApiLocaleResult<?>> results = new HashMap<>();
    for (String baseUrl : instanceUrls) {
      String fullUrl = baseUrl + apiPath; // API path appended to instance URL
      Response response = Request.build(fullUrl, httpSender)
          .withHeader(AuthKey.AUTHORIZATION, getAuthorization())
          .withJsonContent(JsonUtils.toJson(payload))
          .send();
      log.info("[Success] Instance {} responded: {}", baseUrl, response.body());
      ApiLocaleResult<?> responseBody = JsonUtils.fromJson(response.body(), ApiLocaleResult.class);
      results.put(baseUrl, responseBody);
    }
    return results;
  }
}
