package cloud.xcan.angus.rest;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserResourceRest {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @GetMapping("/users")
  public List<Map<String, Object>> getUsers() {
    return jdbcTemplate.queryForList("SELECT * FROM oauth2_user");
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping("/authorities")
  public List<Map<String, Object>> getAuthorities() {
    return jdbcTemplate.queryForList("SELECT * FROM oauth2_authorities");
  }

  @PreAuthorize("hasAuthority('client:list')")
  @GetMapping("/clients")
  public List<Map<String, Object>> getClients() {
    return jdbcTemplate.queryForList("SELECT * FROM oauth2_registered_client");
  }

}
