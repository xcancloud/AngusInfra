package cloud.xcan.angus.remote.client;

import cloud.xcan.angus.remote.ApiLocaleResult;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
   * @param queryParams Request query parameters
   */
  @GetMapping
  ApiLocaleResult<?> get(@SpringQueryMap Object queryParams);

  /**
   * Sends a POST request to a dynamically specified URL.
   *
   * @param body Request payload
   */
  @PostMapping
  ApiLocaleResult<?> post( @RequestBody Object body);

  /**
   * Sends a PUT request to a dynamically specified URL.
   *
   * @param body Request payload
   */
  @PutMapping
  ApiLocaleResult<?> put( @RequestBody Object body);

  /**
   * Sends a PATCH request to a dynamically specified URL.
   *
   * @param body Request payload
   */
  @PatchMapping
  ApiLocaleResult<?> patch(@RequestBody Object body);

  /**
   * Sends a DELETE request to a dynamically specified URL.
   *
   * @param queryParams Request query parameters
   */
  @DeleteMapping
  void delete( @SpringQueryMap Object queryParams);

}
