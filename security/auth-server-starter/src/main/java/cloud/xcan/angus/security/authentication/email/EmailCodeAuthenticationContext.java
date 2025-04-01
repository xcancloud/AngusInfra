package cloud.xcan.angus.security.authentication.email;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthenticationContext;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.util.Assert;

/**
 * An {@link OAuth2AuthenticationContext} that holds an {@link EmailCodeAuthenticationToken}
 * and additional information and is used when validating the OAuth 2.0 Password Grant Request.
 */
public final class EmailCodeAuthenticationContext implements OAuth2AuthenticationContext {

  private final Map<Object, Object> context;

  private EmailCodeAuthenticationContext(Map<Object, Object> context) {
    this.context = Collections.unmodifiableMap(new HashMap<>(context));
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public <V> V get(Object key) {
    return hasKey(key) ? (V) this.context.get(key) : null;
  }

  @Override
  public boolean hasKey(Object key) {
    Assert.notNull(key, "key cannot be null");
    return this.context.containsKey(key);
  }

  /**
   * Returns the {@link RegisteredClient registered client}.
   *
   * @return the {@link RegisteredClient}
   */
  public RegisteredClient getRegisteredClient() {
    return get(RegisteredClient.class);
  }

  /**
   * Constructs a new {@link Builder} with the provided {@link EmailCodeAuthenticationToken}.
   *
   * @param authentication the {@link EmailCodeAuthenticationToken}
   * @return the {@link Builder}
   */
  public static Builder with(EmailCodeAuthenticationToken authentication) {
    return new Builder(authentication);
  }

  /**
   * A builder for {@link EmailCodeAuthenticationContext}.
   */
  public static final class Builder extends
      AbstractBuilder<EmailCodeAuthenticationContext, Builder> {

    private Builder(EmailCodeAuthenticationToken authentication) {
      super(authentication);
    }

    /**
     * Sets the {@link RegisteredClient registered client}.
     *
     * @param registeredClient the {@link RegisteredClient}
     * @return the {@link Builder} for further configuration
     */
    public Builder registeredClient(RegisteredClient registeredClient) {
      return put(RegisteredClient.class, registeredClient);
    }

    /**
     * Builds a new {@link EmailCodeAuthenticationContext}.
     *
     * @return the {@link EmailCodeAuthenticationContext}
     */
    public EmailCodeAuthenticationContext build() {
      Assert.notNull(get(RegisteredClient.class), "registeredClient cannot be null");
      return new EmailCodeAuthenticationContext(getContext());
    }

  }

}
