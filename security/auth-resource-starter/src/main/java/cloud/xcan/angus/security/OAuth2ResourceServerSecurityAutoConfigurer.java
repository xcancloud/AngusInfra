package cloud.xcan.angus.security;

import static cloud.xcan.angus.spec.experimental.BizConstant.AUTH_WHITELIST;

import cloud.xcan.angus.security.handler.CustomAccessDeniedHandler;
import cloud.xcan.angus.security.handler.CustomAuthenticationEntryPoint;
import cloud.xcan.angus.security.introspection.CustomOpaqueTokenIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({OAuth2ResourceServerProperties.class})
public class OAuth2ResourceServerSecurityAutoConfigurer {

  @Bean
  public SecurityFilterChain resourceServerFilterChain(HttpSecurity http,
      BearerTokenResolver bearerTokenResolver, AccessDeniedHandler accessDeniedHandler,
      AuthenticationEntryPoint authenticationEntryPoint,
      OpaqueTokenIntrospector opaqueTokenIntrospector) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize
        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow public access
        .requestMatchers(AUTH_WHITELIST).permitAll() // Allow public access
        .anyRequest().authenticated() // Other requests require authentication
    ).oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
        .bearerTokenResolver(bearerTokenResolver)
        .accessDeniedHandler(accessDeniedHandler)
        .authenticationEntryPoint(authenticationEntryPoint)
        .opaqueToken(opaque -> opaque.introspector(opaqueTokenIntrospector))
    ).csrf(AbstractHttpConfigurer::disable); // Disable CSRF protection (configure as needed)
    return http.build();
  }

  @Bean
  public BearerTokenResolver bearerTokenResolver() {
    DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
    resolver.setAllowUriQueryParameter(true);
    return resolver;
  }

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
    return new CustomAuthenticationEntryPoint(objectMapper);
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
    return new CustomAccessDeniedHandler(objectMapper);
  }

  @Bean
  public OpaqueTokenIntrospector opaqueTokenIntrospector(
      OAuth2ResourceServerProperties auth2ResourceServerProperties) {
    return new CustomOpaqueTokenIntrospector(
        auth2ResourceServerProperties.getOpaquetoken().getIntrospectionUri(),
        auth2ResourceServerProperties.getOpaquetoken().getClientId(),
        auth2ResourceServerProperties.getOpaquetoken().getClientSecret()
    );
  }

}
