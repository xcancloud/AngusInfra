package cloud.xcan.angus.security.authentication.dao;


import cloud.xcan.angus.api.enums.SignInType;
import org.springframework.security.core.AuthenticationException;

public interface LinkSecretService {

  void matches(SignInType type, String userId, String linkSecret)
      throws AuthenticationException;

}
