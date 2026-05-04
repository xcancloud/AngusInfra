package cloud.xcan.angus.security.authentication.device;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;

public final class DeviceClientAuthenticationConverter implements AuthenticationConverter {

  private RequestMatcher deviceAuthorizationRequestMatcher;
  private RequestMatcher deviceAccessTokenRequestMatcher;

  public DeviceClientAuthenticationConverter() {
  }

  public DeviceClientAuthenticationConverter(String deviceAuthorizationEndpointUri) {
    RequestMatcher clientIdParameterMatcher = request ->
        request.getParameter(OAuth2ParameterNames.CLIENT_ID) != null;
    this.deviceAuthorizationRequestMatcher = new AndRequestMatcher(
        new AntPathRequestMatcher(deviceAuthorizationEndpointUri, HttpMethod.POST.name()),
        clientIdParameterMatcher);
    this.deviceAccessTokenRequestMatcher = request ->
        AuthorizationGrantType.DEVICE_CODE.getValue()
            .equals(request.getParameter(OAuth2ParameterNames.GRANT_TYPE)) &&
            request.getParameter(OAuth2ParameterNames.DEVICE_CODE) != null &&
            request.getParameter(OAuth2ParameterNames.CLIENT_ID) != null;
  }

  @Nullable
  @Override
  public Authentication convert(HttpServletRequest request) {
    if (!this.deviceAuthorizationRequestMatcher.matches(request) &&
        !this.deviceAccessTokenRequestMatcher.matches(request)) {
      return null;
    }

    // client_id (REQUIRED)
    String clientId = request.getParameter(OAuth2ParameterNames.CLIENT_ID);
    if (!StringUtils.hasText(clientId) ||
        request.getParameterValues(OAuth2ParameterNames.CLIENT_ID).length != 1) {
      throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
    }

    return new DeviceClientAuthenticationToken(clientId, ClientAuthenticationMethod.NONE, null,
        null);
  }

  public RequestMatcher getDeviceAuthorizationRequestMatcher() {
    return deviceAuthorizationRequestMatcher;
  }

  public void setDeviceAuthorizationRequestMatcher(
      RequestMatcher deviceAuthorizationRequestMatcher) {
    this.deviceAuthorizationRequestMatcher = deviceAuthorizationRequestMatcher;
  }

  public RequestMatcher getDeviceAccessTokenRequestMatcher() {
    return deviceAccessTokenRequestMatcher;
  }

  public void setDeviceAccessTokenRequestMatcher(
      RequestMatcher deviceAccessTokenRequestMatcher) {
    this.deviceAccessTokenRequestMatcher = deviceAccessTokenRequestMatcher;
  }
}
