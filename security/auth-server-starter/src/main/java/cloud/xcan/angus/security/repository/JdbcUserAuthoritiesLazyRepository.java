package cloud.xcan.angus.security.repository;

import cloud.xcan.angus.security.model.CustomOAuth2User;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;

/**
 * Lazy initialization and loading of authorization policies, resource, and operational role
 * permissions.
 */
public interface JdbcUserAuthoritiesLazyRepository {

  Set<GrantedAuthority> lazyUserAuthorities(CustomOAuth2User user);

}
