package cloud.xcan.angus.remote.client;

import cloud.xcan.angus.remote.ApiLocaleResult;
import feign.Headers;
import feign.Param;
import feign.QueryMap;
import feign.RequestLine;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * FeignClient with dynamic URL capability. Key: The `url` acts as a placeholder, replaced at
 * runtime via @Param("url").
 */
//@FeignClient(name = "dynamic-feign-client", url = "${feign.dynamic.url:}")
public interface DynamicFeignClient {

  /**
   * Sends a GET request to a dynamically specified URL.
   *
   * @param url         Target endpoint URL (injected via @Param)
   * @param queryParams Request query parameters
   */
  @RequestLine("GET {url}")
  ApiLocaleResult<?> get(@Param("url") String url, @QueryMap Object queryParams);

  /**
   * Sends a POST request to a dynamically specified URL.
   *
   * @param url  Target endpoint URL (injected via @Param)
   * @param body Request payload
   */
  @RequestLine("POST {url}")
  @Headers("Content-Type: application/json")
  ApiLocaleResult<?> post(@Param("url") String url, @RequestBody Object body);

  /**
   * Sends a PUT request to a dynamically specified URL.
   *
   * @param url  Target endpoint URL (injected via @Param)
   * @param body Request payload
   */
  @RequestLine("PUT {url}")
  @Headers("Content-Type: application/json")
  ApiLocaleResult<?> put(@Param("url") String url, @RequestBody Object body);

  /**
   * Sends a PATCH request to a dynamically specified URL.
   *
   * @param url  Target endpoint URL (injected via @Param)
   * @param body Request payload
   */
  @RequestLine("PATCH {url}")
  @Headers("Content-Type: application/json")
  ApiLocaleResult<?> patch(@Param("url") String url, @RequestBody Object body);

  /**
   * Sends a DELETE request to a dynamically specified URL.
   *
   * @param url         Target endpoint URL (injected via @Param)
   * @param queryParams Request query parameters
   */
  @RequestLine("DELETE {url}")
  void delete(@Param("url") String url, @QueryMap Object queryParams);

}
