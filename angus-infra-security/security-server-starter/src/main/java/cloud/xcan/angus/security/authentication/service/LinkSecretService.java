package cloud.xcan.angus.security.authentication.service;


import cloud.xcan.angus.api.enums.SignInType;
import org.springframework.security.core.AuthenticationException;

public interface LinkSecretService {

  void matches(SignInType type, String userId, String linkSecret)
      throws AuthenticationException;

}
