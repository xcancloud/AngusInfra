package cloud.xcan.angus.security.authentication.service;

import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;

public interface JdbcOAuth2AuthorizationService extends OAuth2AuthorizationService {

  void removeByClientId(String clientId);
}
