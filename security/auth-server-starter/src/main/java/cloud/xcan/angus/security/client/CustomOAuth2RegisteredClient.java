package cloud.xcan.angus.security.client;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.util.SpringAuthorizationServerVersion;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * A representation of a client registration with an OAuth 2.0 Authorization Server.
 *
 * @see <a target="_blank" href="https://tools.ietf.org/html/rfc6749#section-2">Section 2
 * Client Registration</a>
 * @see org.springframework.security.oauth2.server.authorization.client.RegisteredClient
 */
public class CustomOAuth2RegisteredClient extends RegisteredClient {

  private static final long serialVersionUID = SpringAuthorizationServerVersion.SERIAL_VERSION_UID;

  /**
   * OAuth2 client fields
   */
  private String id;

  private String clientId;

  private Instant clientIdIssuedAt;

  private String clientSecret;

  private Instant clientSecretExpiresAt;

  private String clientName;

  private Set<ClientAuthenticationMethod> clientAuthenticationMethods;

  private Set<AuthorizationGrantType> authorizationGrantTypes;

  private Set<String> redirectUris;

  private Set<String> postLogoutRedirectUris;

  private Set<String> scopes;

  private ClientSettings clientSettings;

  private TokenSettings tokenSettings;

  /**
   * AngusGM Client Info.
   */
  private String description;

  private boolean enabled;

  private String platform;

  private String source;

  private String bizTag;

  private String tenantId;

  private String createdBy;

  private Instant createdDate;

  private String lastModifiedBy;

  private Instant lastModifiedDate;

  protected CustomOAuth2RegisteredClient() {
  }

  /**
   * Returns the identifier for the registration.
   *
   * @return the identifier for the registration
   */
  public String getId() {
    return this.id;
  }

  /**
   * Returns the client identifier.
   *
   * @return the client identifier
   */
  public String getClientId() {
    return this.clientId;
  }

  /**
   * Returns the time at which the client identifier was issued.
   *
   * @return the time at which the client identifier was issued
   */
  @Nullable
  public Instant getClientIdIssuedAt() {
    return this.clientIdIssuedAt;
  }

  /**
   * Returns the client secret or {@code null} if not available.
   *
   * @return the client secret or {@code null} if not available
   */
  @Nullable
  public String getClientSecret() {
    return this.clientSecret;
  }

  /**
   * Returns the time at which the client secret expires or {@code null} if it does not expire.
   *
   * @return the time at which the client secret expires or {@code null} if it does not expire
   */
  @Nullable
  public Instant getClientSecretExpiresAt() {
    return this.clientSecretExpiresAt;
  }

  /**
   * Returns the client name.
   *
   * @return the client name
   */
  public String getClientName() {
    return this.clientName;
  }

  /**
   * Returns the {@link ClientAuthenticationMethod authentication method(s)} that the client may
   * use.
   *
   * @return the {@code Set} of {@link ClientAuthenticationMethod authentication method(s)}
   */
  public Set<ClientAuthenticationMethod> getClientAuthenticationMethods() {
    return this.clientAuthenticationMethods;
  }

  /**
   * Returns the {@link AuthorizationGrantType authorization grant type(s)} that the client may
   * use.
   *
   * @return the {@code Set} of {@link AuthorizationGrantType authorization grant type(s)}
   */
  public Set<AuthorizationGrantType> getAuthorizationGrantTypes() {
    return this.authorizationGrantTypes;
  }

  /**
   * Returns the redirect URI(s) that the client may use in redirect-based flows.
   *
   * @return the {@code Set} of redirect URI(s)
   */
  public Set<String> getRedirectUris() {
    return this.redirectUris;
  }

  /**
   * Returns the post logout redirect URI(s) that the client may use for logout. The
   * {@code post_logout_redirect_uri} parameter is used by the client when requesting that the
   * End-User's User Agent be redirected to after a logout has been performed.
   *
   * @return the {@code Set} of post logout redirect URI(s)
   * @since 1.1
   */
  public Set<String> getPostLogoutRedirectUris() {
    return this.postLogoutRedirectUris;
  }

  /**
   * Returns the scope(s) that the client may use.
   *
   * @return the {@code Set} of scope(s)
   */
  public Set<String> getScopes() {
    return this.scopes;
  }

  /**
   * Returns the {@link ClientSettings client configuration settings}.
   *
   * @return the {@link ClientSettings}
   */
  public ClientSettings getClientSettings() {
    return this.clientSettings;
  }

  /**
   * Returns the {@link TokenSettings token configuration settings}.
   *
   * @return the {@link TokenSettings}
   */
  public TokenSettings getTokenSettings() {
    return this.tokenSettings;
  }

  public String getDescription() {
    return description;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getPlatform() {
    return platform;
  }

  public String getSource() {
    return source;
  }

  public String getBizTag() {
    return bizTag;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public String getLastModifiedBy() {
    return lastModifiedBy;
  }

  public Instant getLastModifiedDate() {
    return lastModifiedDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CustomOAuth2RegisteredClient that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return enabled == that.enabled && Objects.equals(id, that.id)
        && Objects.equals(clientId, that.clientId)
        && Objects.equals(clientIdIssuedAt, that.clientIdIssuedAt)
        && Objects.equals(clientSecret, that.clientSecret)
        && Objects.equals(clientSecretExpiresAt, that.clientSecretExpiresAt)
        && Objects.equals(clientName, that.clientName)
        && Objects.equals(clientAuthenticationMethods, that.clientAuthenticationMethods)
        && Objects.equals(authorizationGrantTypes, that.authorizationGrantTypes)
        && Objects.equals(redirectUris, that.redirectUris)
        && Objects.equals(postLogoutRedirectUris, that.postLogoutRedirectUris)
        && Objects.equals(scopes, that.scopes)
        && Objects.equals(clientSettings, that.clientSettings)
        && Objects.equals(tokenSettings, that.tokenSettings)
        && Objects.equals(description, that.description)
        && Objects.equals(platform, that.platform) && Objects.equals(source, that.source)
        && Objects.equals(bizTag, that.bizTag) && Objects.equals(tenantId, that.tenantId)
        && Objects.equals(createdBy, that.createdBy)
        && Objects.equals(createdDate, that.createdDate)
        && Objects.equals(lastModifiedBy, that.lastModifiedBy)
        && Objects.equals(lastModifiedDate, that.lastModifiedDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, clientId, clientIdIssuedAt, clientSecret,
        clientSecretExpiresAt, clientName, clientAuthenticationMethods,
        authorizationGrantTypes, redirectUris, postLogoutRedirectUris, scopes,
        clientSettings, tokenSettings, description, enabled, platform, source,
        bizTag, tenantId, createdBy, createdDate, lastModifiedBy, lastModifiedDate);
  }

  /**
   * Returns a new {@link CustomOAuth2RegisteredClient.Builder}, initialized with the provided
   * registration identifier.
   *
   * @param id the identifier for the registration
   * @return the {@link CustomOAuth2RegisteredClient.Builder}
   */
  public static CustomOAuth2RegisteredClient.Builder withId0(String id) {
    Assert.hasText(id, "id cannot be empty");
    return new CustomOAuth2RegisteredClient.Builder(id);
  }

  /**
   * Returns a new {@link CustomOAuth2RegisteredClient.Builder}, initialized with the values from
   * the provided {@link CustomOAuth2RegisteredClient}.
   *
   * @param registeredClient the {@link CustomOAuth2RegisteredClient} used for initializing the
   *                         {@link CustomOAuth2RegisteredClient.Builder}
   * @return the {@link CustomOAuth2RegisteredClient.Builder}
   */
  public static CustomOAuth2RegisteredClient.Builder from(
      CustomOAuth2RegisteredClient registeredClient) {
    Assert.notNull(registeredClient, "registeredClient cannot be null");
    return new CustomOAuth2RegisteredClient.Builder(registeredClient);
  }

  /**
   * A builder for {@link CustomOAuth2RegisteredClient}.
   */
  public static class Builder implements Serializable {

    private static final long serialVersionUID = SpringAuthorizationServerVersion.SERIAL_VERSION_UID;

    /**
     * OAuth2 client fields
     */
    private String id;

    private String clientId;

    private Instant clientIdIssuedAt;

    private String clientSecret;

    private Instant clientSecretExpiresAt;

    private String clientName;

    private final Set<ClientAuthenticationMethod> clientAuthenticationMethods = new HashSet<>();

    private final Set<AuthorizationGrantType> authorizationGrantTypes = new HashSet<>();

    private final Set<String> redirectUris = new HashSet<>();

    private final Set<String> postLogoutRedirectUris = new HashSet<>();

    private final Set<String> scopes = new HashSet<>();

    private ClientSettings clientSettings;

    private TokenSettings tokenSettings;

    /**
     * AngusGM Client Info.
     */
    private String description;

    private boolean enabled;

    private String platform;

    private String source;

    private String bizTag;

    private String tenantId;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    protected Builder(String id) {
      this.id = id;
    }

    protected Builder(CustomOAuth2RegisteredClient registeredClient) {
      this.id = registeredClient.getId();
      this.clientId = registeredClient.getClientId();
      this.clientIdIssuedAt = registeredClient.getClientIdIssuedAt();
      this.clientSecret = registeredClient.getClientSecret();
      this.clientSecretExpiresAt = registeredClient.getClientSecretExpiresAt();
      this.clientName = registeredClient.getClientName();
      if (!CollectionUtils.isEmpty(registeredClient.getClientAuthenticationMethods())) {
        this.clientAuthenticationMethods.addAll(registeredClient.getClientAuthenticationMethods());
      }
      if (!CollectionUtils.isEmpty(registeredClient.getAuthorizationGrantTypes())) {
        this.authorizationGrantTypes.addAll(registeredClient.getAuthorizationGrantTypes());
      }
      if (!CollectionUtils.isEmpty(registeredClient.getRedirectUris())) {
        this.redirectUris.addAll(registeredClient.getRedirectUris());
      }
      if (!CollectionUtils.isEmpty(registeredClient.getPostLogoutRedirectUris())) {
        this.postLogoutRedirectUris.addAll(registeredClient.getPostLogoutRedirectUris());
      }
      if (!CollectionUtils.isEmpty(registeredClient.getScopes())) {
        this.scopes.addAll(registeredClient.getScopes());
      }
      this.clientSettings = ClientSettings.withSettings(
          registeredClient.getClientSettings().getSettings()).build();
      this.tokenSettings = TokenSettings.withSettings(
          registeredClient.getTokenSettings().getSettings()).build();
      this.description = registeredClient.getDescription();
      this.enabled = registeredClient.isEnabled();
      this.platform = registeredClient.getPlatform();
      this.source = registeredClient.getSource();
      this.bizTag = registeredClient.getBizTag();
      this.tenantId = registeredClient.getTenantId();
      this.createdBy = registeredClient.getCreatedBy();
      this.createdDate = registeredClient.getCreatedDate();
      this.lastModifiedBy = registeredClient.getLastModifiedBy();
      this.lastModifiedDate = registeredClient.getLastModifiedDate();
    }

    /**
     * Sets the identifier for the registration.
     *
     * @param id the identifier for the registration
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder id(String id) {
      this.id = id;
      return this;
    }

    /**
     * Sets the client identifier.
     *
     * @param clientId the client identifier
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    /**
     * Sets the time at which the client identifier was issued.
     *
     * @param clientIdIssuedAt the time at which the client identifier was issued
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder clientIdIssuedAt(Instant clientIdIssuedAt) {
      this.clientIdIssuedAt = clientIdIssuedAt;
      return this;
    }

    /**
     * Sets the client secret.
     *
     * @param clientSecret the client secret
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder clientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
      return this;
    }

    /**
     * Sets the time at which the client secret expires or {@code null} if it does not expire.
     *
     * @param clientSecretExpiresAt the time at which the client secret expires or {@code null} if
     *                              it does not expire
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder clientSecretExpiresAt(
        Instant clientSecretExpiresAt) {
      this.clientSecretExpiresAt = clientSecretExpiresAt;
      return this;
    }

    /**
     * Sets the client name.
     *
     * @param clientName the client name
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder clientName(String clientName) {
      this.clientName = clientName;
      return this;
    }

    /**
     * Adds an {@link ClientAuthenticationMethod authentication method} the client may use when
     * authenticating with the authorization server.
     *
     * @param clientAuthenticationMethod the authentication method
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder clientAuthenticationMethod(
        ClientAuthenticationMethod clientAuthenticationMethod) {
      this.clientAuthenticationMethods.add(clientAuthenticationMethod);
      return this;
    }

    /**
     * A {@code Consumer} of the {@link ClientAuthenticationMethod authentication method(s)}
     * allowing the ability to add, replace, or remove.
     *
     * @param clientAuthenticationMethodsConsumer a {@code Consumer} of the authentication
     *                                            method(s)
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder clientAuthenticationMethods(
        Consumer<Set<ClientAuthenticationMethod>> clientAuthenticationMethodsConsumer) {
      clientAuthenticationMethodsConsumer.accept(this.clientAuthenticationMethods);
      return this;
    }

    /**
     * Adds an {@link AuthorizationGrantType authorization grant type} the client may use.
     *
     * @param authorizationGrantType the authorization grant type
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder authorizationGrantType(
        AuthorizationGrantType authorizationGrantType) {
      this.authorizationGrantTypes.add(authorizationGrantType);
      return this;
    }

    /**
     * A {@code Consumer} of the {@link AuthorizationGrantType authorization grant type(s)} allowing
     * the ability to add, replace, or remove.
     *
     * @param authorizationGrantTypesConsumer a {@code Consumer} of the authorization grant type(s)
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder authorizationGrantTypes(
        Consumer<Set<AuthorizationGrantType>> authorizationGrantTypesConsumer) {
      authorizationGrantTypesConsumer.accept(this.authorizationGrantTypes);
      return this;
    }

    /**
     * Adds a redirect URI the client may use in a redirect-based flow.
     *
     * @param redirectUri the redirect URI
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder redirectUri(String redirectUri) {
      this.redirectUris.add(redirectUri);
      return this;
    }

    /**
     * A {@code Consumer} of the redirect URI(s) allowing the ability to add, replace, or remove.
     *
     * @param redirectUrisConsumer a {@link Consumer} of the redirect URI(s)
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder redirectUris(
        Consumer<Set<String>> redirectUrisConsumer) {
      redirectUrisConsumer.accept(this.redirectUris);
      return this;
    }

    /**
     * Adds a post logout redirect URI the client may use for logout. The
     * {@code post_logout_redirect_uri} parameter is used by the client when requesting that the
     * End-User's User Agent be redirected to after a logout has been performed.
     *
     * @param postLogoutRedirectUri the post logout redirect URI
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     * @since 1.1
     */
    public CustomOAuth2RegisteredClient.Builder postLogoutRedirectUri(
        String postLogoutRedirectUri) {
      this.postLogoutRedirectUris.add(postLogoutRedirectUri);
      return this;
    }

    /**
     * A {@code Consumer} of the post logout redirect URI(s) allowing the ability to add, replace,
     * or remove.
     *
     * @param postLogoutRedirectUrisConsumer a {@link Consumer} of the post logout redirect URI(s)
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     * @since 1.1
     */
    public CustomOAuth2RegisteredClient.Builder postLogoutRedirectUris(
        Consumer<Set<String>> postLogoutRedirectUrisConsumer) {
      postLogoutRedirectUrisConsumer.accept(this.postLogoutRedirectUris);
      return this;
    }

    /**
     * Adds a scope the client may use.
     *
     * @param scope the scope
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder scope(String scope) {
      this.scopes.add(scope);
      return this;
    }

    /**
     * A {@code Consumer} of the scope(s) allowing the ability to add, replace, or remove.
     *
     * @param scopesConsumer a {@link Consumer} of the scope(s)
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder scopes(Consumer<Set<String>> scopesConsumer) {
      scopesConsumer.accept(this.scopes);
      return this;
    }

    /**
     * Sets the {@link ClientSettings client configuration settings}.
     *
     * @param clientSettings the client configuration settings
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder clientSettings(ClientSettings clientSettings) {
      this.clientSettings = clientSettings;
      return this;
    }

    /**
     * Sets the {@link TokenSettings token configuration settings}.
     *
     * @param tokenSettings the token configuration settings
     * @return the {@link CustomOAuth2RegisteredClient.Builder}
     */
    public CustomOAuth2RegisteredClient.Builder tokenSettings(TokenSettings tokenSettings) {
      this.tokenSettings = tokenSettings;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder description(String description) {
      this.description = description;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder platform(String platform) {
      this.platform = platform;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder source(String source) {
      this.source = source;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder bizTag(String bizTag) {
      this.bizTag = bizTag;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder tenantId(String tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder createdBy(String createdBy) {
      this.createdBy = createdBy;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder createdDate(Instant createdDate) {
      this.createdDate = createdDate;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder lastModifiedBy(String lastModifiedBy) {
      this.lastModifiedBy = lastModifiedBy;
      return this;
    }

    public CustomOAuth2RegisteredClient.Builder lastModifiedDate(Instant lastModifiedDate) {
      this.lastModifiedDate = lastModifiedDate;
      return this;
    }

    /**
     * Builds a new {@link CustomOAuth2RegisteredClient}.
     *
     * @return a {@link CustomOAuth2RegisteredClient}
     */
    public CustomOAuth2RegisteredClient build() {
      Assert.hasText(this.clientId, "clientId cannot be empty");
      Assert.notEmpty(this.authorizationGrantTypes, "authorizationGrantTypes cannot be empty");
      if (this.authorizationGrantTypes.contains(AuthorizationGrantType.AUTHORIZATION_CODE)) {
        Assert.notEmpty(this.redirectUris, "redirectUris cannot be empty");
      }
      if (!StringUtils.hasText(this.clientName)) {
        this.clientName = this.id;
      }
      if (CollectionUtils.isEmpty(this.clientAuthenticationMethods)) {
        this.clientAuthenticationMethods.add(ClientAuthenticationMethod.CLIENT_SECRET_BASIC);
      }
      if (this.clientSettings == null) {
        ClientSettings.Builder builder = ClientSettings.builder();
        if (isPublicClientType()) {
          // @formatter:off
          builder
              .requireProofKey(true)
              .requireAuthorizationConsent(true);
          // @formatter:on
        }
        this.clientSettings = builder.build();
      }
      if (this.tokenSettings == null) {
        this.tokenSettings = TokenSettings.builder().build();
      }
      validateScopes();
      validateRedirectUris();
      validatePostLogoutRedirectUris();
      return create();
    }

    private boolean isPublicClientType() {
      return this.authorizationGrantTypes.contains(AuthorizationGrantType.AUTHORIZATION_CODE)
          && this.clientAuthenticationMethods.size() == 1
          && this.clientAuthenticationMethods.contains(ClientAuthenticationMethod.NONE);
    }

    private CustomOAuth2RegisteredClient create() {
      CustomOAuth2RegisteredClient registeredClient = new CustomOAuth2RegisteredClient();

      registeredClient.id = this.id;
      registeredClient.clientId = this.clientId;
      registeredClient.clientIdIssuedAt = this.clientIdIssuedAt;
      registeredClient.clientSecret = this.clientSecret;
      registeredClient.clientSecretExpiresAt = this.clientSecretExpiresAt;
      registeredClient.clientName = this.clientName;
      registeredClient.clientAuthenticationMethods = Collections
          .unmodifiableSet(new HashSet<>(this.clientAuthenticationMethods));
      registeredClient.authorizationGrantTypes = Collections
          .unmodifiableSet(new HashSet<>(this.authorizationGrantTypes));
      registeredClient.redirectUris = Collections.unmodifiableSet(new HashSet<>(this.redirectUris));
      registeredClient.postLogoutRedirectUris = Collections
          .unmodifiableSet(new HashSet<>(this.postLogoutRedirectUris));
      registeredClient.scopes = Collections.unmodifiableSet(new HashSet<>(this.scopes));
      registeredClient.clientSettings = this.clientSettings;
      registeredClient.tokenSettings = this.tokenSettings;
      registeredClient.description = registeredClient.getDescription();
      registeredClient.enabled = registeredClient.isEnabled();
      registeredClient.platform = registeredClient.getPlatform();
      registeredClient.source = registeredClient.getSource();
      registeredClient.bizTag = registeredClient.getBizTag();
      registeredClient.tenantId = registeredClient.getTenantId();
      registeredClient.createdBy = registeredClient.getCreatedBy();
      registeredClient.createdDate = registeredClient.getCreatedDate();
      registeredClient.lastModifiedBy = registeredClient.getLastModifiedBy();
      registeredClient.lastModifiedDate = registeredClient.getLastModifiedDate();
      return registeredClient;
    }

    private void validateScopes() {
      if (CollectionUtils.isEmpty(this.scopes)) {
        return;
      }

      for (String scope : this.scopes) {
        Assert.isTrue(validateScope(scope), "scope \"" + scope + "\" contains invalid characters");
      }
    }

    private static boolean validateScope(String scope) {
      return scope == null || scope.chars()
          .allMatch((c) -> withinTheRangeOf(c, 0x21, 0x21) || withinTheRangeOf(c, 0x23, 0x5B)
              || withinTheRangeOf(c, 0x5D, 0x7E));
    }

    private static boolean withinTheRangeOf(int c, int min, int max) {
      return c >= min && c <= max;
    }

    private void validateRedirectUris() {
      if (CollectionUtils.isEmpty(this.redirectUris)) {
        return;
      }

      for (String redirectUri : this.redirectUris) {
        Assert.isTrue(validateRedirectUri(redirectUri),
            "redirect_uri \"" + redirectUri
                + "\" is not a valid redirect URI or contains fragment");
      }
    }

    private void validatePostLogoutRedirectUris() {
      if (CollectionUtils.isEmpty(this.postLogoutRedirectUris)) {
        return;
      }

      for (String postLogoutRedirectUri : this.postLogoutRedirectUris) {
        Assert.isTrue(validateRedirectUri(postLogoutRedirectUri), "post_logout_redirect_uri \""
            + postLogoutRedirectUri
            + "\" is not a valid post logout redirect URI or contains fragment");
      }
    }

    private static boolean validateRedirectUri(String redirectUri) {
      try {
        URI validRedirectUri = new URI(redirectUri);
        return validRedirectUri.getFragment() == null;
      } catch (URISyntaxException ex) {
        return false;
      }
    }
  }
}
