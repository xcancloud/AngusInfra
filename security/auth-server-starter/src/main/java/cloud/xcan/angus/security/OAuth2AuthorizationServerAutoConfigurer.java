package cloud.xcan.angus.security;

import static cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationProviderUtils.createDelegatingPasswordEncoder;
import static cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationProviderUtils.getOAuth2TokenGenerator;
import static cloud.xcan.angus.spec.experimental.BizConstant.AUTH_WHITELIST;
import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

import cloud.xcan.angus.security.authentication.CustomJdbcOAuth2AuthorizationService;
import cloud.xcan.angus.security.authentication.CustomOAuth2TokenIntrospectionAuthenticationProvider;
import cloud.xcan.angus.security.authentication.dao.DaoAuthenticationProvider;
import cloud.xcan.angus.security.authentication.dao.checker.DefaultPostAuthenticationChecks;
import cloud.xcan.angus.security.authentication.dao.checker.DefaultPreAuthenticationChecks;
import cloud.xcan.angus.security.authentication.dao.LinkSecretService;
import cloud.xcan.angus.security.authentication.device.DeviceClientAuthenticationConverter;
import cloud.xcan.angus.security.authentication.device.DeviceClientAuthenticationProvider;
import cloud.xcan.angus.security.authentication.email.EmailCodeAuthenticationConverter;
import cloud.xcan.angus.security.authentication.email.EmailCodeAuthenticationProvider;
import cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationConverter;
import cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationProvider;
import cloud.xcan.angus.security.authentication.sms.SmsCodeAuthenticationConverter;
import cloud.xcan.angus.security.authentication.sms.SmsCodeAuthenticationProvider;
import cloud.xcan.angus.security.repository.JdbcRegisteredClientRepository;
import cloud.xcan.angus.security.repository.JdbcUserAuthoritiesLazyService;
import cloud.xcan.angus.security.repository.JdbcUserDetailsRepository;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientCredentialsAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2TokenIntrospectionAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({OAuth2ResourceServerProperties.class})
public class OAuth2AuthorizationServerAutoConfigurer {

  private static final String ISSUER = "https://www.xcan.cloud";
  private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";

  @Bean
  public SecurityFilterChain authorizationServerSecurityFilterChain(
      HttpSecurity http, RegisteredClientRepository registeredClientRepository,
      AuthorizationServerSettings authorizationServerSettings,
      AuthenticationManager authenticationManager, CorsConfigurationSource oauth2CorsConfiguration,
      OAuth2AuthorizationService oauth2AuthorizationService) throws Exception {
    // @formatter:off

    OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = getOAuth2TokenGenerator(http);
    OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = authorizationServer();

    // Authorization Server
    http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow public access
            .requestMatchers(AUTH_WHITELIST).permitAll() // Allow public access
          .anyRequest().authenticated()) // Other requests require authentication
        .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
        .cors(cors -> cors.configurationSource(oauth2CorsConfiguration))
        .with(authorizationServerConfigurer
            .tokenEndpoint(
                oAuth2TokenEndpointConfigurer -> oAuth2TokenEndpointConfigurer
                    .accessTokenRequestConverter(
                      new DelegatingAuthenticationConverter(Arrays.asList(
                        new OAuth2PasswordAuthenticationConverter(),
                        new OAuth2AuthorizationCodeAuthenticationConverter(),
                        new OAuth2RefreshTokenAuthenticationConverter(),
                        new DeviceClientAuthenticationConverter(authorizationServerSettings.getDeviceAuthorizationEndpoint()),
                        new SmsCodeAuthenticationConverter(),
                        new EmailCodeAuthenticationConverter())
                      ))
                    .authenticationProvider(
                        new OAuth2PasswordAuthenticationProvider(oauth2AuthorizationService, tokenGenerator, authenticationManager))
                    .authenticationProvider(
                        new OAuth2ClientCredentialsAuthenticationProvider(oauth2AuthorizationService, tokenGenerator))
                    .authenticationProvider(
                        new DeviceClientAuthenticationProvider(registeredClientRepository))
                    .authenticationProvider(
                        new SmsCodeAuthenticationProvider(oauth2AuthorizationService, tokenGenerator, authenticationManager))
                    .authenticationProvider(
                        new EmailCodeAuthenticationProvider(oauth2AuthorizationService, tokenGenerator, authenticationManager))),
            (authorizationServer) -> authorizationServer
                .authorizationServerSettings(authorizationServerSettings)
                // oauth2-authorization-server/src/test/java/org/springframework/security/oauth2/server/authorization/config/annotation/web/configurers/OAuth2TokenIntrospectionTests.java
                .tokenIntrospectionEndpoint(tokenIntrospectionEndpoint -> tokenIntrospectionEndpoint // Enable Introspection
                    .introspectionRequestConverter(new OAuth2TokenIntrospectionAuthenticationConverter())
                    .authenticationProvider(new CustomOAuth2TokenIntrospectionAuthenticationProvider(registeredClientRepository, oauth2AuthorizationService))
                    //.introspectionResponseHandler(new OAuth2AccessTokenResponseAuthenticationSuccessHandler())
                    //.errorResponseHandler(new OAuth2ErrorAuthenticationFailureHandler())
                )
                .deviceAuthorizationEndpoint(deviceAuthorizationEndpoint ->
                    deviceAuthorizationEndpoint.verificationUri("/activate"))
                .deviceVerificationEndpoint(deviceVerificationEndpoint ->
                    deviceVerificationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI))
                .authorizationEndpoint(authorizationEndpoint ->
                    authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI))
                .oidc(Customizer.withDefaults())  // Enable OpenID Connect 1.0
        ).csrf(AbstractHttpConfigurer::disable); // Disable CSRF protection (configure as needed)

    // @formatter:on

    return http.build();
  }

  @Bean
  public AuthorizationServerSettings authorizationServerSettings() {
    return AuthorizationServerSettings.builder().issuer(ISSUER).build();
  }

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public AuthenticationManager authenticationManager(HttpSecurity http,
      UserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
      @Autowired(required = false) LinkSecretService linkSecretService,
      @Autowired(required = false) DefaultPreAuthenticationChecks defaultPreAuthenticationChecks,
      @Autowired(required = false) DefaultPostAuthenticationChecks defaultPostAuthenticationChecks)
      throws Exception {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(passwordEncoder,
        linkSecretService);
    provider.setPreAuthenticationChecks(defaultPreAuthenticationChecks);
    provider.setUserDetailsService(userDetailsService);
    return http.getSharedObject(AuthenticationManagerBuilder.class)
        .authenticationProvider(provider)
        .userDetailsService(userDetailsService)
        .passwordEncoder(passwordEncoder)
        .and()
        .build();
  }

  /**
   * @see PasswordEncoderFactories#createDelegatingPasswordEncoder()
   */
  @Bean
  @ConditionalOnMissingBean
  public PasswordEncoder passwordEncoder() {
    return createDelegatingPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource oauth2CorsConfiguration() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/oauth2/**", config);
    return source;
  }

  @Bean
  public RegisteredClientRepository registeredClientRepository(
      @Qualifier("dataSource") DataSource dataSource) {
    return new JdbcRegisteredClientRepository(new JdbcTemplate(dataSource));
  }

  @Bean
  public UserDetailsService userDetailsManager(
      @Qualifier("dataSource") DataSource dataSource,
      @Autowired(required = false) JdbcUserAuthoritiesLazyService authoritiesLazyService) {
    return new JdbcUserDetailsRepository(dataSource, authoritiesLazyService);
  }

  @Bean
  public JdbcOAuth2AuthorizationService jdbcOAuth2AuthorizationService(
      @Qualifier("dataSource") DataSource dataSource,
      RegisteredClientRepository registeredClientRepository) {
    return new CustomJdbcOAuth2AuthorizationService(new JdbcTemplate(dataSource),
        registeredClientRepository);
  }

  @Bean
  public JdbcOAuth2AuthorizationConsentService jdbcOAuth2AuthorizationConsentService(
      @Qualifier("dataSource") DataSource dataSource,
      RegisteredClientRepository registeredClientRepository) {
    return new JdbcOAuth2AuthorizationConsentService(new JdbcTemplate(dataSource),
        registeredClientRepository);
  }

}
