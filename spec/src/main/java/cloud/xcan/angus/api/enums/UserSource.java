package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum UserSource implements EnumMessage<String> {
  PLATFORM_SIGNUP,
  INVITATION_CODE_SIGNUP,
  INVITATION_JOIN,
  BACKGROUND_SIGNUP,
  BACKGROUND_ADDED,
  /**
   * Three-party login first registration
   */
  THIRD_PARTY_LOGIN,
  LDAP_SYNCHRONIZE;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isPlatformSignup() {
    return PLATFORM_SIGNUP.equals(this);
  }

  public static boolean isNewSignup(final UserSource source) {
    return PLATFORM_SIGNUP.equals(source) || BACKGROUND_SIGNUP.equals(source)
        || THIRD_PARTY_LOGIN.equals(source);
  }

  public static boolean isAddUser(final UserSource source) {
    return INVITATION_CODE_SIGNUP.equals(source) || BACKGROUND_ADDED.equals(source)
        || LDAP_SYNCHRONIZE.equals(source);
  }
}
