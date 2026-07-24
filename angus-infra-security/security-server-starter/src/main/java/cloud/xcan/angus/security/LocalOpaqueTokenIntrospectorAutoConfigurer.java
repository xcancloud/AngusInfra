package cloud.xcan.angus.security;

import cloud.xcan.angus.security.authentication.CustomOAuth2TokenIntrospectionAuthenticationProvider;
import cloud.xcan.angus.security.introspection.LocalOpaqueTokenIntrospector;
import cloud.xcan.angus.security.web.ResourceServerSecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

/**
 * When the authorization server and resource server run in the same process (GM), register a local
 * {@link OpaqueTokenIntrospector} so API authentication does not HTTP-call
 * {@code /oauth2/introspect} on itself.
 */
@Configuration
@ConditionalOnClass(OpaqueTokenIntrospector.class)
@AutoConfigureBefore(OAuth2ResourceServerSecurityAutoConfigurer.class)
@EnableConfigurationProperties(ResourceServerSecurityProperties.class)
@ConditionalOnProperty(prefix = "angus.security.local-introspect", name = "enabled",
    havingValue = "true", matchIfMissing = true)
public class LocalOpaqueTokenIntrospectorAutoConfigurer {

  @Bean
  @ConditionalOnBean(CustomOAuth2TokenIntrospectionAuthenticationProvider.class)
  @ConditionalOnMissingBean(OpaqueTokenIntrospector.class)
  public OpaqueTokenIntrospector localOpaqueTokenIntrospector(
      CustomOAuth2TokenIntrospectionAuthenticationProvider tokenIntrospectionAuthenticationProvider,
      ResourceServerSecurityProperties resourceServerSecurityProperties) {
    LocalOpaqueTokenIntrospector introspector = new LocalOpaqueTokenIntrospector(
        tokenIntrospectionAuthenticationProvider);
    introspector.setResultCache(
        resourceServerSecurityProperties.isIntrospectCacheEnabled(),
        resourceServerSecurityProperties.getIntrospectCacheTtlSeconds(),
        resourceServerSecurityProperties.getIntrospectCacheMaximumSize());
    return introspector;
  }
}
