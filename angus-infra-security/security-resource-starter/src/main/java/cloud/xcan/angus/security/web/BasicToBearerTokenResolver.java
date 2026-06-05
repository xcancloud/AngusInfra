package cloud.xcan.angus.security.web;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.util.StringUtils;

/**
 * {@link BearerTokenResolver} that, in addition to standard {@code Authorization: Bearer} and the
 * query-parameter token, extracts an access token from artifact-protocol credential carriers:
 *
 * <ul>
 *   <li>the password component of an {@code Authorization: Basic} header (username ignored) — the
 *       "token as password" convention used by docker / mvn / pip / apt / yum / helm;</li>
 *   <li>any configured raw-token header such as {@code X-NuGet-ApiKey}.</li>
 * </ul>
 *
 * <p>The extracted value is returned verbatim as the bearer token and is validated by the existing
 * opaque-token introspection pipeline; this resolver performs no validation itself. When the bridge
 * is disabled it delegates entirely to {@link DefaultBearerTokenResolver}, preserving the stock
 * Bearer-only behavior.</p>
 *
 * <p>Resolution order: standard Bearer (delegate) first, then configured token headers, then Basic
 * password. The delegate is consulted first so a genuine {@code Bearer} token always wins and the
 * delegate's "token specified in multiple places" protection still applies.</p>
 */
public class BasicToBearerTokenResolver implements BearerTokenResolver {

  private static final String BASIC_PREFIX = "Basic ";

  private final DefaultBearerTokenResolver delegate;
  private final boolean enabled;
  private final boolean acceptBasicPassword;
  private final List<String> tokenHeaders;

  public BasicToBearerTokenResolver(DefaultBearerTokenResolver delegate,
      BasicAuthBridgeProperties properties) {
    this.delegate = delegate;
    this.enabled = properties.isEnabled();
    this.acceptBasicPassword = properties.isAcceptBasicPassword();
    this.tokenHeaders = properties.getTokenHeaders();
  }

  @Override
  public String resolve(HttpServletRequest request) {
    // Standard Bearer / query-param token wins; also preserves the delegate's multi-token guard.
    String bearer = delegate.resolve(request);
    if (bearer != null) {
      return bearer;
    }
    if (!enabled) {
      return null;
    }
    String fromHeader = resolveFromTokenHeaders(request);
    if (fromHeader != null) {
      return fromHeader;
    }
    return resolveFromBasicPassword(request);
  }

  private String resolveFromTokenHeaders(HttpServletRequest request) {
    if (tokenHeaders == null) {
      return null;
    }
    for (String headerName : tokenHeaders) {
      String value = request.getHeader(headerName);
      if (StringUtils.hasText(value)) {
        return value.trim();
      }
    }
    return null;
  }

  private String resolveFromBasicPassword(HttpServletRequest request) {
    if (!acceptBasicPassword) {
      return null;
    }
    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorization == null || !StringUtils.startsWithIgnoreCase(authorization, BASIC_PREFIX)) {
      return null;
    }
    String encoded = authorization.substring(BASIC_PREFIX.length()).trim();
    if (encoded.isEmpty()) {
      return null;
    }
    String decoded;
    try {
      decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException ex) {
      return null;
    }
    int colon = decoded.indexOf(':');
    if (colon < 0) {
      return null;
    }
    String password = decoded.substring(colon + 1);
    return StringUtils.hasText(password) ? password : null;
  }
}
