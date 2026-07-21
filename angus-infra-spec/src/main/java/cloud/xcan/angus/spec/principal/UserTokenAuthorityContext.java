package cloud.xcan.angus.spec.principal;

/**
 * Thread-local PAT context for authority lazy-load during token issue / introspection.
 */
public final class UserTokenAuthorityContext {

  private static final ThreadLocal<String> TOKEN_NAME = new ThreadLocal<>();

  private UserTokenAuthorityContext() {
  }

  public static void setTokenName(String tokenName) {
    if (tokenName == null || tokenName.isBlank()) {
      TOKEN_NAME.remove();
    } else {
      TOKEN_NAME.set(tokenName);
    }
  }

  public static String getTokenName() {
    return TOKEN_NAME.get();
  }

  public static void clear() {
    TOKEN_NAME.remove();
  }
}
