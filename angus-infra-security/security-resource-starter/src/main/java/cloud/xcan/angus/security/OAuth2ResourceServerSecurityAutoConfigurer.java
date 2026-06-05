package cloud.xcan.angus.security;

import static cloud.xcan.angus.spec.experimental.BizConstant.AUTH_RESOURCES;

import cloud.xcan.angus.security.handler.CustomAccessDeniedHandler;
import cloud.xcan.angus.security.handler.CustomAuthenticationEntryPoint;
import cloud.xcan.angus.security.introspection.CustomOpaqueTokenIntrospector;
import cloud.xcan.angus.security.principal.HoldPrincipalFilter;
import cloud.xcan.angus.security.web.BasicAuthBridgeProperties;
import cloud.xcan.angus.security.web.BasicToBearerTokenResolver;
import cloud.xcan.angus.security.web.ResourceServerSecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({OAuth2ResourceServerProperties.class, BasicAuthBridgeProperties.class,
    ResourceServerSecurityProperties.class})
public class OAuth2ResourceServerSecurityAutoConfigurer {

  @Bean("resourceServerSecurityFilterChain")
  public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http,
      BearerTokenResolver bearerTokenResolver, AccessDeniedHandler accessDeniedHandler,
      AuthenticationEntryPoint authenticationEntryPoint,
      OpaqueTokenIntrospector opaqueTokenIntrospector, ObjectMapper objectMapper) throws Exception {
    http.authorizeHttpRequests(authorize -> authorize
            //.requestMatchers("/**").permitAll() // Allow public access
            .requestMatchers(AUTH_RESOURCES).authenticated()
            .anyRequest().permitAll())// Other requests require authentication
        .addFilterAfter(new HoldPrincipalFilter(objectMapper), AuthorizationFilter.class)
        .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
            .bearerTokenResolver(bearerTokenResolver)
            .accessDeniedHandler(accessDeniedHandler)
            .authenticationEntryPoint(authenticationEntryPoint)
            .opaqueToken(opaque -> opaque.introspector(opaqueTokenIntrospector))
        ).csrf(AbstractHttpConfigurer::disable); // Disable CSRF protection (configure as needed)
    // Avoid setting `X-Frame-Options: deny` in the HTTP response header,
    // which causes the browser to reject the page from being loaded within <frame></frame>
    http.headers(headers -> headers
        // .frameOptions(FrameOptionsConfig::sameOrigin)
        .frameOptions(FrameOptionsConfig::disable)
    );
    return http.build();
  }

  /**
   * Resolves the access token from the request. When the artifact-protocol Basic bridge is enabled
   * (see {@link BasicAuthBridgeProperties}), the token may additionally arrive as an HTTP Basic
   * password or a configured token header (e.g. {@code X-NuGet-ApiKey}); otherwise this is the
   * stock {@link DefaultBearerTokenResolver} behavior.
   *
   * <p>The {@code ?access_token=} query-parameter carrier is gated by
   * {@link ResourceServerSecurityProperties#isAllowUriQueryToken()} (default {@code true} for
   * backward compatibility); security-sensitive services disable it to keep tokens out of URLs and
   * access logs per RFC 6750 §2.3.</p>
   */
  @Bean
  public BearerTokenResolver bearerTokenResolver(BasicAuthBridgeProperties basicAuthBridgeProperties,
      ResourceServerSecurityProperties resourceServerSecurityProperties) {
    DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
    resolver.setAllowUriQueryParameter(resourceServerSecurityProperties.isAllowUriQueryToken());
    if (basicAuthBridgeProperties.isEnabled()) {
      return new BasicToBearerTokenResolver(resolver, basicAuthBridgeProperties);
    }
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
