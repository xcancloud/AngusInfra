package cloud.xcan.angus;

import cloud.xcan.angus.security.principal.HoldPrincipalFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiResourceRest {

  /**
   * Used for testing and verifying hold principal in `/api/**` endpoint.
   *
   * @see HoldPrincipalFilter
   */
  @GetMapping("/api/test")
  public String getUsers() {
    return "test";
  }

}
