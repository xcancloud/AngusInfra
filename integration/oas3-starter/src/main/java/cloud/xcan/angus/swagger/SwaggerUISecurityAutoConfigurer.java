package cloud.xcan.angus.swagger;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SwaggerUISecurityAutoConfigurer {

  @Bean
  public SecurityFilterChain swaggerSecurity(HttpSecurity http) throws Exception {
    http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**")
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/swagger-ui/**").authenticated() // Access UI require authentication
            .requestMatchers("/v3/api-docs/**").permitAll() // Allow public access api-docs
        )
        .httpBasic(withDefaults()) // Enable http basic authentication
        .csrf(AbstractHttpConfigurer::disable); // Disable CSRF protection (configure as needed);
    return http.build();
  }
}
