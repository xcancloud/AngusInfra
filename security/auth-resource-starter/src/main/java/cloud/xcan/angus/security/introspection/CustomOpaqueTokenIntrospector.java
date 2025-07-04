package cloud.xcan.angus.security.introspection;

import static cloud.xcan.angus.spec.experimental.BizConstant.AuthKey.AUTHORITY_SCOPE_PREFIX;

import cloud.xcan.angus.spec.experimental.BizConstant.Header;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimAccessor;
import org.springframework.security.oauth2.core.OAuth2TokenIntrospectionClaimNames;
import org.springframework.security.oauth2.server.resource.introspection.BadOpaqueTokenException;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionAuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * A Custom implementation of {@link OpaqueTokenIntrospector} that verifies and introspects a token
 * using the configured
 * <a href="https://tools.ietf.org/html/rfc7662" target="_blank">OAuth 2.0 Introspection
 * Endpoint</a>.
 *
 * @see SpringOpaqueTokenIntrospector
 */
public class CustomOpaqueTokenIntrospector implements OpaqueTokenIntrospector {


  public static final String INTROSPECTION_CLAIM_NAMES_SCOPE = "permissions";

  private static final ParameterizedTypeReference<Map<String, Object>> STRING_OBJECT_MAP = new ParameterizedTypeReference<>() {
  };

  private final Log logger = LogFactory.getLog(getClass());

  private final RestOperations restOperations;

  private Converter<String, RequestEntity<?>> requestEntityConverter;

  private Converter<OAuth2TokenIntrospectionClaimAccessor, ? extends OAuth2AuthenticatedPrincipal> authenticationConverter = this::defaultAuthenticationConverter;

  /**
   * Creates a {@code OpaqueTokenAuthenticationProvider} with the provided parameters
   *
   * @param introspectionUri The introspection endpoint uri
   * @param clientId         The client id authorized to introspect
   * @param clientSecret     The client's secret
   */
  public CustomOpaqueTokenIntrospector(String introspectionUri, String clientId,
      String clientSecret) {
    Assert.notNull(introspectionUri, "introspectionUri cannot be null");
    Assert.notNull(clientId, "clientId cannot be null");
    Assert.notNull(clientSecret, "clientSecret cannot be null");
    this.requestEntityConverter = this.defaultRequestEntityConverter(URI.create(introspectionUri));
    RestTemplate restTemplate = new RestTemplate();
    restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(clientId, clientSecret));
    this.restOperations = restTemplate;
  }

  /**
   * Creates a {@code OpaqueTokenAuthenticationProvider} with the provided parameters The given
   * {@link RestOperations} should perform its own client authentication against the introspection
   * endpoint.
   *
   * @param introspectionUri The introspection endpoint uri
   * @param restOperations   The client for performing the introspection request
   */
  public CustomOpaqueTokenIntrospector(String introspectionUri, RestOperations restOperations) {
    Assert.notNull(introspectionUri, "introspectionUri cannot be null");
    Assert.notNull(restOperations, "restOperations cannot be null");
    this.requestEntityConverter = this.defaultRequestEntityConverter(URI.create(introspectionUri));
    this.restOperations = restOperations;
  }

  private Converter<String, RequestEntity<?>> defaultRequestEntityConverter(URI introspectionUri) {
    return (token) -> {
      HttpHeaders headers = requestHeaders();
      MultiValueMap<String, String> body = requestBody(token);
      return new RequestEntity<>(body, headers, HttpMethod.POST, introspectionUri);
    };
  }

  private HttpHeaders requestHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add(Header.REQUEST_ID, PrincipalContext.getRequestId());
    return headers;
  }

  private MultiValueMap<String, String> requestBody(String token) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("token", token);
    return body;
  }

  @Override
  public OAuth2AuthenticatedPrincipal introspect(String token) {
    RequestEntity<?> requestEntity = this.requestEntityConverter.convert(token);
    if (requestEntity == null) {
      throw new OAuth2IntrospectionException("requestEntityConverter returned a null entity");
    }
    ResponseEntity<Map<String, Object>> responseEntity = makeRequest(requestEntity);
    Map<String, Object> claims = adaptToNimbusResponse(responseEntity);
    OAuth2TokenIntrospectionClaimAccessor accessor = convertClaimsSet(claims);
    return this.authenticationConverter.convert(accessor);
  }

  /**
   * Sets the {@link Converter} used for converting the OAuth 2.0 access token to a
   * {@link RequestEntity} representation of the OAuth 2.0 token introspection request.
   *
   * @param requestEntityConverter the {@link Converter} used for converting to a
   *                               {@link RequestEntity} representation of the token introspection
   *                               request
   */
  public void setRequestEntityConverter(
      Converter<String, RequestEntity<?>> requestEntityConverter) {
    Assert.notNull(requestEntityConverter, "requestEntityConverter cannot be null");
    this.requestEntityConverter = requestEntityConverter;
  }

  private ResponseEntity<Map<String, Object>> makeRequest(RequestEntity<?> requestEntity) {
    try {
      return this.restOperations.exchange(requestEntity, STRING_OBJECT_MAP);
    } catch (Exception ex) {
      throw new OAuth2IntrospectionException(ex.getMessage(), ex);
    }
  }

  private Map<String, Object> adaptToNimbusResponse(
      ResponseEntity<Map<String, Object>> responseEntity) {
    if (responseEntity.getStatusCode() != HttpStatus.OK) {
      throw new OAuth2IntrospectionException(
          "Introspection endpoint responded with " + responseEntity.getStatusCode());
    }
    Map<String, Object> claims = responseEntity.getBody();
    // relying solely on the authorization server to validate this token (not checking
    // 'exp', for example)
    if (claims == null) {
      return Collections.emptyMap();
    }

    boolean active = (boolean) claims.compute(OAuth2TokenIntrospectionClaimNames.ACTIVE, (k, v) -> {
      if (v instanceof String) {
        return Boolean.parseBoolean((String) v);
      }
      if (v instanceof Boolean) {
        return v;
      }
      return false;
    });
    if (!active) {
      this.logger.trace("Did not validate token since it is inactive");
      throw new BadOpaqueTokenException("Provided token isn't active");
    }
    return claims;
  }

  private ArrayListFromStringClaimAccessor convertClaimsSet(Map<String, Object> claims) {
    Map<String, Object> converted = new LinkedHashMap<>(claims);
    converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.AUD, (k, v) -> {
      if (v instanceof String) {
        return Collections.singletonList(v);
      }
      return v;
    });
    converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.CLIENT_ID,
        (k, v) -> v.toString());
    converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.EXP,
        (k, v) -> Instant.ofEpochSecond(((Number) v).longValue()));
    converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.IAT,
        (k, v) -> Instant.ofEpochSecond(((Number) v).longValue()));
    // RFC-7662 page 7 directs users to RFC-7519 for defining the values of these
    // issuer fields.
    // https://datatracker.ietf.org/doc/html/rfc7662#page-7
    //
    // RFC-7519 page 9 defines issuer fields as being 'case-sensitive' strings
    // containing
    // a 'StringOrURI', which is defined on page 5 as being any string, but strings
    // containing ':'
    // should be treated as valid URIs.
    // https://datatracker.ietf.org/doc/html/rfc7519#section-2
    //
    // It is not defined however as to whether-or-not normalized URIs should be
    // treated as the same literal
    // value. It only defines validation itself, so to avoid potential ambiguity or
    // unwanted side effects that
    // may be awkward to debug, we do not want to manipulate this value. Previous
    // versions of Spring Security
    // would *only* allow valid URLs, which is not what we wish to achieve here.
    converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.ISS, (k, v) -> v.toString());
    converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.NBF,
        (k, v) -> Instant.ofEpochSecond(((Number) v).longValue()));
    converted.computeIfPresent(OAuth2TokenIntrospectionClaimNames.SCOPE,
        (k, v) -> (v instanceof String s) ? new ArrayListFromString(s.split(" ")) : v);
    return () -> converted;
  }

  /**
   * <p>
   * Sets the
   * {@link Converter Converter&lt;OAuth2TokenIntrospectionClaimAccessor,
   * OAuth2AuthenticatedPrincipal&gt;} to use. Defaults to
   * {@link CustomOpaqueTokenIntrospector#defaultAuthenticationConverter}.
   * </p>
   * <p>
   * Use if you need a custom mapping of OAuth 2.0 token claims to the authenticated principal.
   * </p>
   *
   * @param authenticationConverter the converter
   * @since 6.3
   */
  public void setAuthenticationConverter(
      Converter<OAuth2TokenIntrospectionClaimAccessor, ? extends OAuth2AuthenticatedPrincipal> authenticationConverter) {
    Assert.notNull(authenticationConverter, "converter cannot be null");
    this.authenticationConverter = authenticationConverter;
  }

  /**
   * If {@link CustomOpaqueTokenIntrospector#authenticationConverter} is not explicitly set, this
   * default converter will be used. transforms an {@link OAuth2TokenIntrospectionClaimAccessor}
   * into an {@link OAuth2AuthenticatedPrincipal} by extracting claims, mapping scopes to
   * authorities, and creating a principal.
   *
   * @return {@link Converter Converter&lt;OAuth2TokenIntrospectionClaimAccessor,
   * OAuth2AuthenticatedPrincipal&gt;}
   * @since 6.3
   */
  private OAuth2IntrospectionAuthenticatedPrincipal defaultAuthenticationConverter(
      OAuth2TokenIntrospectionClaimAccessor accessor) {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    Collection<GrantedAuthority> scopeAuthorities = authorities(AUTHORITY_SCOPE_PREFIX,
        accessor.getScopes());
    authorities.addAll(scopeAuthorities);
    Collection<GrantedAuthority> userAuthorities = authorities("",
        accessor.getClaimAsStringList(INTROSPECTION_CLAIM_NAMES_SCOPE));
    authorities.addAll(userAuthorities);
    return new OAuth2IntrospectionAuthenticatedPrincipal(accessor.getClaims(), authorities);
  }

  private Collection<GrantedAuthority> authorities(String prefix, List<String> authorities) {
    if (!(/*authorities instanceof ArrayListFromString*/ authorities instanceof ArrayList)) {
      return Collections.emptyList();
    }
    Collection<GrantedAuthority> finalAuthorities = new ArrayList<>();
    for (String authority : authorities) {
      finalAuthorities.add(new SimpleGrantedAuthority(prefix + authority));
    }
    return finalAuthorities;
  }

  private static final class ArrayListFromString extends ArrayList<String> {

    ArrayListFromString(String... elements) {
      super(Arrays.asList(elements));
    }

  }

  private interface ArrayListFromStringClaimAccessor extends OAuth2TokenIntrospectionClaimAccessor {

    @Override
    default List<String> getScopes() {
      Object value = getClaims().get(OAuth2TokenIntrospectionClaimNames.SCOPE);
      if (value instanceof ArrayListFromString list) {
        return list;
      }
      return OAuth2TokenIntrospectionClaimAccessor.super.getScopes();
    }

  }

}
