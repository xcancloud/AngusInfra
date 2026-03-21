package cloud.xcan.angus.security;

import cloud.xcan.angus.security.cache.TokenCacheManager;
import cloud.xcan.angus.security.config.InnerApiAuthProperties;
import cloud.xcan.angus.security.remote.ClientSignInnerApiRemote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot Auto-Configuration for Inner API Authentication
 * 
 * This configuration:
 * 1. Enables and loads InnerApiAuthProperties from application.yml
 * 2. Creates TokenCacheManager bean for OAuth2 token caching
 * 3. Creates FeignInnerApiAuthInterceptor bean for request interception
 * 
 * The configuration is automatically applied when:
 * - xcan.auth.innerapi.enabled=true (or not specified, default is true)
 * - All required components are on the classpath
 * 
 * @author Framework Team
 * @version 2.0
 * @since 2025-03-21
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(InnerApiAuthProperties.class)
@ConditionalOnProperty(
    prefix = "xcan.auth.innerapi",
    name = "enabled",
    matchIfMissing = true
)
public class FeignInnerApiAutoConfigurer {

  /**
   * Configure TokenCacheManager bean
   * 
   * This bean manages OAuth2 token caching with automatic refresh.
   * It uses InnerApiAuthProperties for cache duration and retry configuration.
   * 
   * @param properties configuration properties from application.yml
   * @param clientSignInnerApiRemote Feign client for token endpoint
   * @return configured TokenCacheManager instance
   */
  @Bean
  public TokenCacheManager tokenCacheManager(
      InnerApiAuthProperties properties,
      ClientSignInnerApiRemote clientSignInnerApiRemote) {
    log.info("Creating TokenCacheManager bean");
    return new TokenCacheManager(properties, clientSignInnerApiRemote);
  }

  /**
   * Configure FeignInnerApiAuthInterceptor bean
   * 
   * This bean intercepts Feign requests and injects OAuth2 Bearer tokens
   * for service-to-service authentication.
   * 
   * @param tokenCacheManager manages token caching and refresh
   * @param properties configuration properties for path matching
   * @return configured FeignInnerApiAuthInterceptor instance
   */
  @Bean
  public FeignInnerApiAuthInterceptor feignInnerApiAuthInterceptor(
      TokenCacheManager tokenCacheManager,
      InnerApiAuthProperties properties) {
    log.info("Creating FeignInnerApiAuthInterceptor bean");
    return new FeignInnerApiAuthInterceptor(tokenCacheManager, properties);
  }

}
