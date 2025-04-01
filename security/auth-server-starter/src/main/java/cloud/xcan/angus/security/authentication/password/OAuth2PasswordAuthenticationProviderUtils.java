package cloud.xcan.angus.security.authentication.password;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.StringUtils;

/**
 * Utility methods for the OAuth 2.0 {@link AuthenticationProvider}'s.
 */
@Slf4j
public final class OAuth2PasswordAuthenticationProviderUtils {

  public static final String DEFAULT_ENCODING_ID = "bcrypt";

  private OAuth2PasswordAuthenticationProviderUtils() {
  }

  public static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(
      Authentication authentication) {
    OAuth2ClientAuthenticationToken clientPrincipal = null;
    if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(
        authentication.getPrincipal().getClass())) {
      clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
    }
    if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
      return clientPrincipal;
    }
    throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
  }

  public static <T extends OAuth2Token> OAuth2AccessToken accessToken(T token,
      OAuth2TokenContext accessTokenContext) {
    return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
        token.getTokenValue(), token.getIssuedAt(), token.getExpiresAt(),
        accessTokenContext.getAuthorizedScopes());
  }

  public static <T> T getOptionalBean(HttpSecurity http, Class<T> type) {
    Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(http.getSharedObject(
        ApplicationContext.class), type);
    if (beansMap.size() > 1) {
      throw new NoUniqueBeanDefinitionException(type, beansMap.size(),
          "Expected single matching bean of type '" + type.getName() + "' but found " +
              beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(
              beansMap.keySet()));
    }
    return (!beansMap.isEmpty() ? beansMap.values().iterator().next() : null);
  }

  static <T> T getOptionalBean(HttpSecurity http, ResolvableType type) {
    ApplicationContext context = http.getSharedObject(ApplicationContext.class);
    String[] names = context.getBeanNamesForType(type);
    if (names.length > 1) {
      throw new NoUniqueBeanDefinitionException(type, names);
    }
    return names.length == 1 ? (T) context.getBean(names[0]) : null;
  }

  public static OAuth2TokenCustomizer<OAuth2TokenClaimsContext> getAccessTokenCustomizer(
      HttpSecurity http) {
    ResolvableType type = ResolvableType.forClassWithGenerics(OAuth2TokenCustomizer.class,
        OAuth2TokenClaimsContext.class);
    return getOptionalBean(http, type);
  }

  /**
   * Creates a {@link DelegatingPasswordEncoder} with default mappings. Additional mappings may be
   * added and the encoding will be updated to conform with best practices. However, due to the
   * nature of {@link DelegatingPasswordEncoder} the updates should not impact users. The mappings
   * current are:
   *
   * <ul>
   * <li>bcrypt - {@link BCryptPasswordEncoder} (Also used for encoding)</li>
   * <li>ldap -
   * {@link org.springframework.security.crypto.password.LdapShaPasswordEncoder}</li>
   * <li>MD4 -
   * {@link org.springframework.security.crypto.password.Md4PasswordEncoder}</li>
   * <li>MD5 - {@code new MessageDigestPasswordEncoder("MD5")}</li>
   * <li>noop -
   * {@link org.springframework.security.crypto.password.NoOpPasswordEncoder}</li>
   * <li>pbkdf2 - {@link Pbkdf2PasswordEncoder}</li>
   * <li>scrypt - {@link SCryptPasswordEncoder}</li>
   * <li>SHA-1 - {@code new MessageDigestPasswordEncoder("SHA-1")}</li>
   * <li>SHA-256 - {@code new MessageDigestPasswordEncoder("SHA-256")}</li>
   * <li>sha256 -
   * {@link org.springframework.security.crypto.password.StandardPasswordEncoder}</li>
   * <li>argon2 - {@link Argon2PasswordEncoder}</li>
   * </ul>
   *
   * @return the {@link PasswordEncoder} to use
   */
  @SuppressWarnings("deprecation")
  public static PasswordEncoder createDelegatingPasswordEncoder() {
    Map<String, PasswordEncoder> encoders = new HashMap<>();
    encoders.put(DEFAULT_ENCODING_ID, new BCryptPasswordEncoder());
    // See /Volumes/workspace/workspace_angus/seek/3rd/spring-security-samples/servlet/spring-boot/java/ldap
    //encoders.put("ldap", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
    //encoders.put("LDAP-PROXY", LdapPasswordConnection.getInstance()); -> Used by old AngusGM
    encoders.put("SHA", new org.springframework.security.crypto.password.LdapShaPasswordEncoder());
    encoders.put("MD4", new org.springframework.security.crypto.password.Md4PasswordEncoder());
    encoders.put("MD5",
        new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("MD5"));
    encoders.put("noop",
        org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());
    //encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());
    //encoders.put("scrypt", new SCryptPasswordEncoder());
    encoders.put("SHA-1",
        new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-1"));
    encoders.put("SHA-256",
        new org.springframework.security.crypto.password.MessageDigestPasswordEncoder("SHA-256"));
    encoders
        .put("sha256", new org.springframework.security.crypto.password.StandardPasswordEncoder());
    //encoders.put("argon2", new Argon2PasswordEncoder());

    return new DelegatingPasswordEncoder(DEFAULT_ENCODING_ID, encoders);
  }

  public static OAuth2TokenGenerator<? extends OAuth2Token> getOAuth2TokenGenerator(
      HttpSecurity http) {
    OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = http.getSharedObject(
        OAuth2TokenGenerator.class);
    if (tokenGenerator == null) {
      tokenGenerator = getOptionalBean(http, OAuth2TokenGenerator.class);
      if (tokenGenerator == null) {
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer
            = getAccessTokenCustomizer(http);
        if (accessTokenCustomizer != null) {
          accessTokenGenerator.setAccessTokenCustomizer(accessTokenCustomizer);
        }
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        tokenGenerator = new DelegatingOAuth2TokenGenerator(
            accessTokenGenerator, refreshTokenGenerator);
      }
      http.setSharedObject(OAuth2TokenGenerator.class, tokenGenerator);
    }
    return tokenGenerator;
  }

  public static String createHash(String value) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] digest = md.digest(value.getBytes(StandardCharsets.US_ASCII));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
  }


}
