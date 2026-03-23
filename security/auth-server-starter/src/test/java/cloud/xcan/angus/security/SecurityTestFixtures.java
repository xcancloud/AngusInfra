package cloud.xcan.angus.security;

import cloud.xcan.angus.security.client.CustomOAuth2RegisteredClient;
import cloud.xcan.angus.security.model.CustomOAuth2User;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

/**
 * Test fixture factory for creating OAuth2 domain objects for unit tests.
 * <p>
 * Provides fluent builders and factory methods for constructing: - OAuth2 registered clients -
 * OAuth2 users with various configurations - Token settings and client settings
 * <p>
 * Usage:
 * <pre>
 *   CustomOAuth2RegisteredClient client =
 *     SecurityTestFixtures.clientBuilder()
 *       .clientId("test-client")
 *       .withPasswordGrant()
 *       .build();
 * </pre>
 *
 * @author Test Framework
 * @version 1.0
 */
public class SecurityTestFixtures {

  // ┌─────────────────────────────────────────────────────────────────────────┐
  // │ OAuth2 Registered Client Fixtures
  // └─────────────────────────────────────────────────────────────────────────┘

  /**
   * Creates a basic registered client for testing
   */
  public static CustomOAuth2ClientBuilder clientBuilder() {
    return new CustomOAuth2ClientBuilder();
  }

  /**
   * Creates a standard test client with all common grant types enabled
   */
  public static CustomOAuth2RegisteredClient createDefaultTestClient() {
    return clientBuilder()
        .clientId("test-client-default")
        .clientSecret("test-secret-12345")
        .clientName("Default Test Client")
        .withPasswordGrant()
        .withRefreshTokenGrant()
        .withClientCredentialsGrant()
        .build();
  }

  /**
   * Creates a password-only test client
   */
  public static CustomOAuth2RegisteredClient createPasswordOnlyTestClient() {
    return clientBuilder()
        .clientId("password-client")
        .clientSecret("password-secret")
        .clientName("Password Test Client")
        .withPasswordGrant()
        .withRefreshTokenGrant()
        .build();
  }

  /**
   * Creates an SMS code grant test client
   */
  public static CustomOAuth2RegisteredClient createSmsCodeTestClient() {
    return clientBuilder()
        .clientId("sms-client")
        .clientSecret("sms-secret")
        .clientName("SMS Test Client")
        .withCustomGrantType("sms_code")
        .withRefreshTokenGrant()
        .build();
  }

  /**
   * Creates an email code grant test client
   */
  public static CustomOAuth2RegisteredClient createEmailCodeTestClient() {
    return clientBuilder()
        .clientId("email-client")
        .clientSecret("email-secret")
        .clientName("Email Test Client")
        .withCustomGrantType("email_code")
        .withRefreshTokenGrant()
        .build();
  }

  /**
   * Creates a device code grant test client
   */
  public static CustomOAuth2RegisteredClient createDeviceCodeTestClient() {
    return clientBuilder()
        .clientId("device-client")
        .clientSecret("device-secret")
        .clientName("Device Test Client")
        .withCustomGrantType("device_code")
        .withRefreshTokenGrant()
        .build();
  }

  /**
   * Creates a public (no client secret) test client
   */
  public static CustomOAuth2RegisteredClient createPublicTestClient() {
    return clientBuilder()
        .clientId("public-client")
        .clientName("Public Test Client")
        .isPublic(true)
        .withPasswordGrant()
        .build();
  }

  // ┌─────────────────────────────────────────────────────────────────────────┐
  // │ OAuth2 User Fixtures
  // └─────────────────────────────────────────────────────────────────────────┘

  /**
   * Creates a basic user for testing
   */
  public static CustomOAuth2UserBuilder userBuilder() {
    return new CustomOAuth2UserBuilder();
  }

  /**
   * Creates a standard test user
   */
  public static CustomOAuth2User createDefaultTestUser() {
    return userBuilder()
        .username("testuser")
        .password("Test@1234")
        .id(1L)
        .tenantId("test-tenant-123")
        .enabled(true)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .authority("ROLE_USER")
        .build();
  }

  /**
   * Creates an admin user
   */
  public static CustomOAuth2User createAdminTestUser() {
    return userBuilder()
        .username("admin")
        .password("Admin@1234")
        .id(2L)
        .tenantId("test-tenant-123")
        .enabled(true)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .authority("ROLE_ADMIN")
        .authority("ROLE_USER")
        .build();
  }

  /**
   * Creates a disabled user (locked account)
   */
  public static CustomOAuth2User createDisabledTestUser() {
    return userBuilder()
        .username("disabled-user")
        .password("Test@1234")
        .id(3L)
        .tenantId("test-tenant-123")
        .enabled(false)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .build();
  }

  /**
   * Creates a locked user (accountNonLocked = false)
   */
  public static CustomOAuth2User createLockedTestUser() {
    return userBuilder()
        .username("locked-user")
        .password("Test@1234")
        .id(4L)
        .tenantId("test-tenant-123")
        .enabled(true)
        .accountNonExpired(true)
        .accountNonLocked(false)
        .credentialsNonExpired(true)
        .build();
  }

  /**
   * Creates a user with expired credentials
   */
  public static CustomOAuth2User createExpiredCredentialsTestUser() {
    return userBuilder()
        .username("expired-creds-user")
        .password("Test@1234")
        .id(5L)
        .tenantId("test-tenant-123")
        .enabled(true)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(false)
        .build();
  }

  /**
   * Creates a user with expired account
   */
  public static CustomOAuth2User createExpiredAccountTestUser() {
    return userBuilder()
        .username("expired-account-user")
        .password("Test@1234")
        .id(6L)
        .tenantId("test-tenant-123")
        .enabled(true)
        .accountNonExpired(false)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .build();
  }

  /**
   * Creates a user with email
   */
  public static CustomOAuth2User createUserWithEmail(String email) {
    return userBuilder()
        .username("user-with-email")
        .email(email)
        .password("Test@1234")
        .id(7L)
        .tenantId("test-tenant-123")
        .enabled(true)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .authority("ROLE_USER")
        .build();
  }

  /**
   * Creates a user with phone
   */
  public static CustomOAuth2User createUserWithPhone(String phone) {
    return userBuilder()
        .username("user-with-phone")
        .mobile(phone)
        .password("Test@1234")
        .id(8L)
        .tenantId("test-tenant-123")
        .enabled(true)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .authority("ROLE_USER")
        .build();
  }

  // ┌─────────────────────────────────────────────────────────────────────────┐
  // │ Builder Classes
  // └─────────────────────────────────────────────────────────────────────────┘

  /**
   * Fluent builder for CustomOAuth2RegisteredClient
   */
  public static class CustomOAuth2ClientBuilder {

    private String clientId;
    private String clientSecret;
    private String clientName;
    private final Set<AuthorizationGrantType> grantTypes = new HashSet<>();
    private final Set<String> scopes = new HashSet<>();
    private String redirectUri;
    private boolean isPublic = false;

    public CustomOAuth2ClientBuilder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    public CustomOAuth2ClientBuilder clientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
      this.isPublic = false;
      return this;
    }

    public CustomOAuth2ClientBuilder clientName(String clientName) {
      this.clientName = clientName;
      return this;
    }

    public CustomOAuth2ClientBuilder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      if (!isPublic) {
        this.clientSecret = "default-secret";
      }
      return this;
    }

    public CustomOAuth2ClientBuilder withPasswordGrant() {
      this.grantTypes.add(AuthorizationGrantType.PASSWORD);
      return this;
    }

    public CustomOAuth2ClientBuilder withAuthorizationCodeGrant() {
      this.grantTypes.add(AuthorizationGrantType.AUTHORIZATION_CODE);
      return this;
    }

    public CustomOAuth2ClientBuilder withClientCredentialsGrant() {
      this.grantTypes.add(AuthorizationGrantType.CLIENT_CREDENTIALS);
      return this;
    }

    public CustomOAuth2ClientBuilder withRefreshTokenGrant() {
      this.grantTypes.add(AuthorizationGrantType.REFRESH_TOKEN);
      return this;
    }

    public CustomOAuth2ClientBuilder withCustomGrantType(String grantType) {
      this.grantTypes.add(new AuthorizationGrantType(grantType));
      return this;
    }

    public CustomOAuth2ClientBuilder scope(String scope) {
      this.scopes.add(scope);
      return this;
    }

    public CustomOAuth2ClientBuilder redirectUri(String uri) {
      this.redirectUri = uri;
      return this;
    }

    public CustomOAuth2RegisteredClient build() {
      if (clientId == null || clientId.isBlank()) {
        throw new IllegalStateException("clientId is required");
      }
      if (grantTypes.contains(AuthorizationGrantType.AUTHORIZATION_CODE) && redirectUri == null) {
        throw new IllegalStateException(
            "redirectUri is required when using authorization_code grant");
      }

      CustomOAuth2RegisteredClient.Builder b =
          CustomOAuth2RegisteredClient.with(clientId)
              .clientId(clientId)
              .clientIdIssuedAt(Instant.now())
              .clientName(clientName != null ? clientName : clientId);

      if (!isPublic) {
        String secret = clientSecret != null ? clientSecret : "default-secret";
        b.clientSecret(secret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
      } else {
        b.clientAuthenticationMethod(ClientAuthenticationMethod.NONE);
      }

      if (grantTypes.isEmpty()) {
        b.authorizationGrantType(AuthorizationGrantType.PASSWORD);
      } else {
        grantTypes.forEach(b::authorizationGrantType);
      }

      if (scopes.isEmpty()) {
        b.scope("openid").scope("profile");
      } else {
        scopes.forEach(b::scope);
      }

      if (redirectUri != null) {
        b.redirectUri(redirectUri);
      }

      TokenSettings tokenSettings = TokenSettings.builder()
          .accessTokenTimeToLive(java.time.Duration.ofMinutes(15))
          .refreshTokenTimeToLive(java.time.Duration.ofDays(30))
          .reuseRefreshTokens(true)
          .build();
      b.tokenSettings(tokenSettings);

      ClientSettings clientSettings = ClientSettings.builder()
          .requireAuthorizationConsent(false)
          .build();
      b.clientSettings(clientSettings);

      return b.build();
    }
  }

  /**
   * Fluent builder for CustomOAuth2User
   */
  public static class CustomOAuth2UserBuilder {

    private String username;
    private String password;
    private Long id;
    private String tenantId;
    private String email;
    private String mobile;
    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private final Set<SimpleGrantedAuthority> authorities = new HashSet<>();

    public CustomOAuth2UserBuilder username(String username) {
      this.username = username;
      return this;
    }

    public CustomOAuth2UserBuilder password(String password) {
      this.password = password;
      return this;
    }

    public CustomOAuth2UserBuilder id(Long id) {
      this.id = id;
      return this;
    }

    public CustomOAuth2UserBuilder tenantId(String tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public CustomOAuth2UserBuilder email(String email) {
      this.email = email;
      return this;
    }

    public CustomOAuth2UserBuilder mobile(String mobile) {
      this.mobile = mobile;
      return this;
    }

    public CustomOAuth2UserBuilder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public CustomOAuth2UserBuilder accountNonExpired(boolean accountNonExpired) {
      this.accountNonExpired = accountNonExpired;
      return this;
    }

    public CustomOAuth2UserBuilder accountNonLocked(boolean accountNonLocked) {
      this.accountNonLocked = accountNonLocked;
      return this;
    }

    public CustomOAuth2UserBuilder credentialsNonExpired(boolean credentialsNonExpired) {
      this.credentialsNonExpired = credentialsNonExpired;
      return this;
    }

    public CustomOAuth2UserBuilder authority(String authority) {
      this.authorities.add(new SimpleGrantedAuthority(authority));
      return this;
    }

    public CustomOAuth2User build() {
      CustomOAuth2User user = new CustomOAuth2User();
      user.setUsername(username);
      user.setPassword(password != null ? password : "default-password");
      user.setId(id != null ? String.valueOf(id) : "1");
      user.setTenantId(tenantId != null ? tenantId : "default-tenant");
      user.setEmail(email);
      user.setMobile(mobile);
      user.setEnabled(enabled);
      user.setAccountNonExpired(accountNonExpired);
      user.setAccountNonLocked(accountNonLocked);
      user.setCredentialsNonExpired(credentialsNonExpired);

      HashSet<GrantedAuthority> granted = new HashSet<>(authorities);
      if (granted.isEmpty()) {
        granted.add(new SimpleGrantedAuthority("ROLE_USER"));
      }
      user.setAuthorities(granted);

      return user;
    }
  }
}
