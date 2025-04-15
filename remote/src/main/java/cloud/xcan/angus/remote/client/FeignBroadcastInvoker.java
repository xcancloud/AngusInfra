package cloud.xcan.angus.remote.client;

import cloud.xcan.angus.remote.ApiLocaleResult;
import java.net.URI;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignBroadcastInvoker {

  private final DynamicFeignClient dynamicFeignClient;
  private final ServiceDiscoveryHelper serviceDiscoveryHelper;

  public FeignBroadcastInvoker(DynamicFeignClient dynamicFeignClient,
      ServiceDiscoveryHelper serviceDiscoveryHelper) {
    this.dynamicFeignClient = dynamicFeignClient;
    this.serviceDiscoveryHelper = serviceDiscoveryHelper;
  }

  /**
   * Broadcasts a request to all instances of a service.
   *
   * @param serviceId Target service ID (e.g., "user-service")
   * @param apiPath   API endpoint path (e.g., "/api/v1/notify")
   * @param request   Request payload
   */
  public void broadcast(String serviceId, String apiPath, Object request) {
    List<String> instanceUrls = serviceDiscoveryHelper.getAllInstanceUrls(serviceId);
    for (String baseUrl : instanceUrls) {
      String fullUrl = baseUrl + apiPath; // API path appended to instance URL
      try {
        ApiLocaleResult<?> response = dynamicFeignClient.post(URI.create(fullUrl), request);
        log.info("[Success] Instance {} responded: {}", baseUrl, response);
      } catch (Exception e) {
        log.error("[Failure] Error calling instance {}: {}", baseUrl, e.getMessage());
      }
    }
  }
}
