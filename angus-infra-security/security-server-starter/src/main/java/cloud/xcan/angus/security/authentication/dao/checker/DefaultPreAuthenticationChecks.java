package cloud.xcan.angus.security.authentication.dao.checker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

@Slf4j
public class DefaultPreAuthenticationChecks implements UserDetailsChecker {

  protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

  @Override
  public void check(UserDetails user) {
    if (!user.isAccountNonLocked()) {
      log.debug("Failed to authenticate since user account is locked");
      throw new LockedException(
          messages.getMessage("AbstractUserDetailsAuthenticationProvider.locked",
              "User account is locked"));
    }
    if (!user.isEnabled()) {
      log.debug("Failed to authenticate since user account is disabled");
      throw new DisabledException(
          messages.getMessage("AbstractUserDetailsAuthenticationProvider.disabled",
              "User is disabled"));
    }
    if (!user.isAccountNonExpired()) {
      log.debug("Failed to authenticate since user account has expired");
      throw new AccountExpiredException(
          messages.getMessage("AbstractUserDetailsAuthenticationProvider.expired",
              "User account has expired"));
    }
  }
}

