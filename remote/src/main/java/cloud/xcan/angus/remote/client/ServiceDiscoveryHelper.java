package cloud.xcan.angus.remote.client;

import java.util.List;
import org.springframework.cloud.client.discovery.DiscoveryClient;

public class ServiceDiscoveryHelper {

  private final DiscoveryClient discoveryClient;

  public ServiceDiscoveryHelper(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  /**
   * Retrieves all instance URLs for a given service.
   *
   * @param serviceId Target service ID (e.g., "user-service")
   * @return List of instance URLs
   */
  public List<String> getAllInstanceUrls(String serviceId) {
    return discoveryClient.getInstances(serviceId)
        .stream()
        .map(instance -> instance.getUri().toString())
        .toList();
  }
}
