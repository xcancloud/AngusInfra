package cloud.xcan.angus.security;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Minimal Spring Boot entry for slice tests: enables auto-configuration (embedded servlet
 * container, MVC, security auto-config) while importing the authorization server setup.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@Import(OAuth2AuthorizationServerAutoConfigurer.class)
public class OAuth2AuthorizationServerTestApplication {

}
