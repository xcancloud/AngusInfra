package cloud.xcan.angus.security.model;

public interface CustomOAuth2UserRepository {

  /**
   * Locates the user based on the account. In the actual implementation, the search may possibly be
   * case sensitive, or case insensitive depending on how the implementation instance is configured.
   * In this case, the <code>UserDetails</code> object that comes back may have a username that is
   * of a different case than what was actually requested.
   *
   * @param account the username, email or mobile identifying the user whose data is required.
   * @return a fully populated user record (never <code>null</code>)
   * @throws AccountNotFoundException if the user could not be found or the user has no
   *                                  GrantedAuthority
   */
  CustomOAuth2User findByAccount(String account) throws AccountNotFoundException;

  /**
   * Check if a user with the supplied login account exists in the system.
   *
   * @param account the username, email or mobile identifying the user whose data is required.
   */
  boolean accountExists(String account);

  /**
   * Update user to delete status.
   *
   * @param username the username identifying the user whose data is required.
   */
  void updateToDeleted(String username);

}
