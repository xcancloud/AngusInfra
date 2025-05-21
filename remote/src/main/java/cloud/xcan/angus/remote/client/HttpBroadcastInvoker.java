package cloud.xcan.angus.remote.client;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getAuthorization;

import cloud.xcan.angus.remote.ApiLocaleResult;
import cloud.xcan.angus.spec.experimental.BizConstant.AuthKey;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.http.HttpSender;
import cloud.xcan.angus.spec.http.HttpSender.Request;
import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.http.HttpUrlConnectionSender;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
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
  public <V> Map<String, ApiLocaleResult<V>> broadcast(
      String serviceId, String apiPath, Object payload) throws Throwable {
    List<String> instanceUrls = serviceDiscoveryHelper.getAllInstanceUrls(serviceId);
    Map<String, ApiLocaleResult<V>> results = new HashMap<>();
    for (String instanceUrl : instanceUrls) {
      String fullUrl = instanceUrl + apiPath; // API path appended to instance URL
      Response response = Request.build(fullUrl, httpSender)
          .withMethod(HttpMethod.POST)
          .withHeader(AuthKey.AUTHORIZATION, getAuthorization())
          .withJsonContent(JsonUtils.toJson(payload))
          .send();
      log.info("[Success] Instance {} responded: {}", instanceUrl, response.body());
      ApiLocaleResult<V> responseBody = JsonUtils.fromJson(response.body(),
          new TypeReference<ApiLocaleResult<V>>() {
          });
      results.put(instanceUrl, responseBody);
    }
    return results;
  }
}
