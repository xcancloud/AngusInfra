package cloud.xcan.angus.security.authentication.dao;

import static java.util.Objects.isNull;

import cloud.xcan.angus.api.enums.SignInType;
import cloud.xcan.angus.security.authentication.password.OAuth2PasswordAuthenticationToken;
import cloud.xcan.angus.security.authentication.sms.SmsCodeAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.password.CompromisedPasswordChecker;
import org.springframework.security.authentication.password.CompromisedPasswordException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * An {@link AuthenticationProvider} implementation that retrieves user details from a
 * {@link UserDetailsService}.
 */
@Slf4j
public class DaoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

  /**
   * The plaintext password(or linkSecret) used to perform
   * {@link PasswordEncoder#matches(CharSequence, String)} on when the user is not found to avoid
   * SEC-2056.
   */
  private static final String USER_NOT_FOUND_PASSWORD = "userNotFoundPassword";

  private PasswordEncoder passwordEncoder;

  /**
   * The password used to perform {@link PasswordEncoder#matches(CharSequence, String)} on when the
   * user is not found to avoid SEC-2056. This is necessary, because some {@link PasswordEncoder}
   * implementations will short circuit if the password is not in a valid format.
   */
  private volatile String userNotFoundEncodedPassword;

  private UserDetailsService userDetailsService;

  private UserDetailsPasswordService userDetailsPasswordService;

  private LinkSecretCheckService linkSecretCheckService;

  private CompromisedPasswordChecker compromisedPasswordChecker;

  public DaoAuthenticationProvider() {
    this(PasswordEncoderFactories.createDelegatingPasswordEncoder(), null);
  }

  /**
   * Creates a new instance using the provided {@link PasswordEncoder}
   *
   * @param passwordEncoder        the {@link PasswordEncoder} to use. Cannot be null.
   * @param linkSecretCheckService the {@link LinkSecretCheckService} to use. Can be null.
   */
  public DaoAuthenticationProvider(PasswordEncoder passwordEncoder,
      LinkSecretCheckService linkSecretCheckService) {
    setPasswordEncoder(passwordEncoder);
    setLinkSecretCheckService(linkSecretCheckService);
  }

  @Override
  protected void additionalAuthenticationChecks(UserDetails userDetails,
      Authentication authentication) throws AuthenticationException {
    if (authentication.getCredentials() == null) {
      log.debug("Failed to authenticate since no credentials provided");
      throw new BadCredentialsException(this.messages
          .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
              "Bad credentials"));
    }

    if (authentication instanceof OAuth2PasswordAuthenticationToken) {
      String presentedPassword = authentication.getCredentials().toString();
      if (!this.passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
        log.debug("Failed to authenticate since password does not match stored value");
        throw new BadCredentialsException(this.messages
            .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials",
                "Bad credentials"));
      }
    } else if (authentication instanceof SmsCodeAuthenticationToken token) {
      if (isNull(linkSecretCheckService)) {
        log.debug("Failed to authenticate since no LinkSecretCheckService instance provided");
        throw new AuthenticationServiceException("No LinkSecretCheckService instance provided");
      }
      String presentedLinkSecret = authentication.getCredentials().toString();
      linkSecretCheckService.matches(SignInType.SMS_CODE, token.getId(), presentedLinkSecret);
    }
  }

  @Override
  protected void doAfterPropertiesSet() {
    Assert.notNull(this.userDetailsService, "A UserDetailsService must be set");
  }

  @Override
  protected final UserDetails retrieveUser(String compositeAccount, Authentication authentication)
      throws AuthenticationException {
    prepareTimingAttackProtection();
    try {
      UserDetails loadedUser = this.getUserDetailsService().loadUserByUsername(compositeAccount);
      if (loadedUser == null) {
        throw new InternalAuthenticationServiceException(
            "UserDetailsService returned null, which is an interface contract violation");
      }
      return loadedUser;
    } catch (UsernameNotFoundException ex) {
      mitigateAgainstTimingAttack(authentication);
      throw ex;
    } catch (InternalAuthenticationServiceException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
    }
  }

  @Override
  protected Authentication createSuccessAuthentication(Object principal,
      Authentication authentication, UserDetails user) {
    if (authentication instanceof OAuth2PasswordAuthenticationToken) {
      String presentedPassword = authentication.getCredentials().toString();
      boolean isPasswordCompromised = this.compromisedPasswordChecker != null
          && this.compromisedPasswordChecker.check(presentedPassword).isCompromised();
      if (isPasswordCompromised) {
        throw new CompromisedPasswordException(
            "The provided password is compromised, please change your password");
      }
      boolean upgradeEncoding = this.userDetailsPasswordService != null
          && this.passwordEncoder.upgradeEncoding(user.getPassword());
      if (upgradeEncoding) {
        String newPassword = this.passwordEncoder.encode(presentedPassword);
        user = this.userDetailsPasswordService.updatePassword(user, newPassword);
      }
    }
    return super.createSuccessAuthentication(principal, authentication, user);
  }

  private void prepareTimingAttackProtection() {
    if (this.userNotFoundEncodedPassword == null) {
      this.userNotFoundEncodedPassword = this.passwordEncoder.encode(USER_NOT_FOUND_PASSWORD);
    }
  }

  private void mitigateAgainstTimingAttack(Authentication authentication) {
    if (authentication.getCredentials() != null) {
      String presentedPassword = authentication.getCredentials().toString();
      this.passwordEncoder.matches(presentedPassword, this.userNotFoundEncodedPassword);
    }
  }

  /**
   * Sets the PasswordEncoder instance to be used to encode and validate passwords. If not set, the
   * password will be compared using
   * {@link PasswordEncoderFactories#createDelegatingPasswordEncoder()}
   *
   * @param passwordEncoder must be an instance of one of the {@code PasswordEncoder} types.
   */
  public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
    Assert.notNull(passwordEncoder, "passwordEncoder cannot be null");
    this.passwordEncoder = passwordEncoder;
    this.userNotFoundEncodedPassword = null;
  }

  protected PasswordEncoder getPasswordEncoder() {
    return this.passwordEncoder;
  }

  public LinkSecretCheckService getLinkSecretCheckService() {
    return linkSecretCheckService;
  }

  public void setLinkSecretCheckService(LinkSecretCheckService linkSecretCheckService) {
    this.linkSecretCheckService = linkSecretCheckService;
  }

  public void setUserDetailsService(UserDetailsService userDetailsService) {
    this.userDetailsService = userDetailsService;
  }

  protected UserDetailsService getUserDetailsService() {
    return this.userDetailsService;
  }

  public void setUserDetailsPasswordService(UserDetailsPasswordService userDetailsPasswordService) {
    this.userDetailsPasswordService = userDetailsPasswordService;
  }

  /**
   * Sets the {@link CompromisedPasswordChecker} to be used before creating a successful
   * authentication. Defaults to {@code null}.
   *
   * @param compromisedPasswordChecker the {@link CompromisedPasswordChecker} to use
   */
  public void setCompromisedPasswordChecker(CompromisedPasswordChecker compromisedPasswordChecker) {
    this.compromisedPasswordChecker = compromisedPasswordChecker;
  }

}
