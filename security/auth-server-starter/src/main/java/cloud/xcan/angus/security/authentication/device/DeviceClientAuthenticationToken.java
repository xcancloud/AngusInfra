package cloud.xcan.angus.security.authentication.device;

import cloud.xcan.angus.security.client.CustomOAuth2RegisteredClient;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Transient;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;


@Transient
public class DeviceClientAuthenticationToken extends OAuth2ClientAuthenticationToken {

  public DeviceClientAuthenticationToken(String clientId,
      ClientAuthenticationMethod clientAuthenticationMethod,
      @Nullable Object credentials, @Nullable Map<String, Object> additionalParameters) {
    super(clientId, clientAuthenticationMethod, credentials, additionalParameters);
  }

  public DeviceClientAuthenticationToken(CustomOAuth2RegisteredClient registeredClient,
      ClientAuthenticationMethod clientAuthenticationMethod,
      @Nullable Object credentials) {
    super(registeredClient, clientAuthenticationMethod, credentials);
  }

}
