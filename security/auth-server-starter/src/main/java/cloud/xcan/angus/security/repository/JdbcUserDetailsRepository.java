package cloud.xcan.angus.security.repository;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.security.model.AccountNotFoundException;
import cloud.xcan.angus.security.model.CustomOAuth2User;
import cloud.xcan.angus.security.model.CustomOAuth2UserRepository;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.log.LogMessage;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.Assert;

/**
 * Jdbc user management service, based on the same table structure as its parent class,
 * <tt>JdbcUserDetailsRepository</tt>.
 * <p>
 * Provides CRUD operations for users. Note that if the
 * {@link #setEnableAuthorities(boolean) enableAuthorities} property is set to false, calls to
 * createUser, updateUser and deleteUser will not store the authorities from the
 * <tt>UserDetails</tt> . it's important that you take this into account when using this
 * implementation for managing your users.
 *
 * @see JdbcUserDetailsManager
 */
public class JdbcUserDetailsRepository extends JdbcUserAuthoritiesDaoImpl implements
    UserDetailsManager, CustomOAuth2UserRepository {

  // @formatter:off
  public static final String DEF_CREATE_USER_SQL =
      "insert into oauth2_user ("
          + "username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired,"
          + "id, first_name, last_name, full_name, password_strength, sys_admin, to_user, email, mobile, main_dept_id,"
          + "password_expired_date, last_modified_password_date, expired_date, deleted, "
          + "tenant_id, tenant_name, tenant_real_name_status, directory_id, default_language, default_time_zone"
          + ") values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

  public static final String DEF_DELETE_USER_SQL
      = "DELETE FROM oauth2_user where username = ?";

  public static final String DEF_UPDATE_USER_SQL =
      "UPDATE oauth2_user set password = ?, enabled = ?, account_non_expired = ?, account_non_locked = ?, credentials_non_expired = ?, "
          + "first_name = ?, last_name = ?, full_name = ?, password_strength = ?, sys_admin = ?, to_user = ?, email = ?, mobile = ?, main_dept_id = ?,"
          + "password_expired_date = ?, last_modified_password_date = ?, expired_date = ?, deleted = ?, "
          + "tenant_id = ?, tenant_name = ?, tenant_real_name_status = ?, directory_id = ?, default_language = ?, default_time_zone = ? "
          + "where username = ?";

  public static final String DEF_INSERT_AUTHORITY_SQL
      = "INSERT INTO Oauth2_authorities (username, authority) VALUES (?,?)";

  public static final String DEF_DELETE_USER_AUTHORITIES_SQL
      = "DELETE FROM oauth2_authorities WHERE username = ?";

  public static final String DEF_USER_EXISTS_SQL
      = "SELECT username FROM oauth2_user WHERE username = ?";

  public static final String DEF_CHANGE_PASSWORD_SQL
      = "UPDATE oauth2_user SET password = ? WHERE username = ?";

  public static final String DEF_USERS_BY_ACCOUNT_QUERY =
      "SELECT username, password, enabled, account_non_expired, account_non_locked, credentials_non_expired,"
          + "id, first_name, last_name, full_name, password_strength, sys_admin, to_user, email, mobile, main_dept_id,"
          + "password_expired_date, last_modified_password_date, expired_date, deleted, "
          + "tenant_id, tenant_name, tenant_real_name_status, directory_id, default_language, default_time_zone "
          + "FROM oauth2_user WHERE (username = ? OR email = ? OR mobile = ?) AND deleted = 0";

  public static final String DEF_USER_ACCOUNT_EXISTS_SQL
      = "SELECT username FROM oauth2_user WHERE username = ? OR email = ? OR mobile = ?";

  public static final String DEF_USER_DELETED_STATUS_SQL
      = "UPDATE oauth2_user SET deleted = 1 WHERE username = ?";

  // @formatter:on

  protected final Log logger = LogFactory.getLog(getClass());

  private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
      .getContextHolderStrategy();

  private String createUserSql = DEF_CREATE_USER_SQL;

  private String deleteUserSql = DEF_DELETE_USER_SQL;

  private String updateUserSql = DEF_UPDATE_USER_SQL;

  private String createAuthoritySql = DEF_INSERT_AUTHORITY_SQL;

  private String deleteUserAuthoritiesSql = DEF_DELETE_USER_AUTHORITIES_SQL;

  private String userExistsSql = DEF_USER_EXISTS_SQL;

  private String changePasswordSql = DEF_CHANGE_PASSWORD_SQL;

  private String usersByAccountSql = DEF_USERS_BY_ACCOUNT_QUERY;

  private String userAccountExistsSql = DEF_USER_ACCOUNT_EXISTS_SQL;

  private String userDeletedStatusSql = DEF_USER_DELETED_STATUS_SQL;

  private AuthenticationManager authenticationManager;

  @Nullable
  private JdbcUserAuthoritiesLazyRepository authoritiesLazyRepository;

  private UserCache userCache = new NullUserCache();

  public JdbcUserDetailsRepository() {
  }

  public JdbcUserDetailsRepository(DataSource dataSource,
      JdbcUserAuthoritiesLazyRepository authoritiesLazyRepository) {
    setDataSource(dataSource);
  }

  @Override
  protected void initDao() throws ApplicationContextException {
    if (this.authenticationManager == null) {
      logger.info(
          "No authentication manager set. Re-authentication of users when changing passwords will not be performed.");
    }
    super.initDao();
  }

  /**
   * Executes the SQL <tt>usersByUsernameQuery</tt> and returns a list of UserDetails objects. There
   * should normally only be one matching user.
   */
  @Override
  protected List<UserDetails> loadUsersByCompositeAccount(String compositeAccount) {
    return super.loadUsersByCompositeAccount(compositeAccount);
  }

  @Override
  public void createUser(final UserDetails user0) {
    CustomOAuth2User user = (CustomOAuth2User) user0;
    validateUserDetails(user);
    getJdbcTemplate().update(this.createUserSql, (ps) -> {
      ps.setString(1, user.getUsername());
      ps.setString(2, user.getPassword());
      ps.setBoolean(3, user.isEnabled());
      ps.setBoolean(4, !user.isAccountNonLocked());
      ps.setBoolean(5, !user.isAccountNonExpired());
      ps.setBoolean(6, !user.isCredentialsNonExpired());
      // AngusGM User Info.
      ps.setString(7, user.getFirstName());
      ps.setString(8, user.getLastName());
      ps.setString(9, user.getFullName());
      ps.setString(10, user.getPasswordStrength());
      ps.setBoolean(11, user.isSysAdmin());
      ps.setBoolean(12, user.isToUser());
      ps.setString(13, user.getEmail());
      ps.setString(14, user.getMobile());
      ps.setString(15, user.getMainDeptId());
      ps.setTimestamp(16, user.getPasswordExpiredDate() != null
          ? Timestamp.from(user.getPasswordExpiredDate()) : null);
      ps.setTimestamp(17, user.getLastModifiedPasswordDate() != null
          ? Timestamp.from(user.getLastModifiedPasswordDate()) : null);
      ps.setTimestamp(18, user.getExpiredDate() != null
          ? Timestamp.from(user.getExpiredDate()) : null);
      ps.setBoolean(19, user.isDeleted());
      ps.setString(20, user.getTenantId());
      ps.setString(21, user.getTenantName());
      ps.setString(22, user.getTenantRealNameStatus());
      ps.setString(23, user.getUsername());
      ps.setString(24, user.getDirectoryId());
      ps.setString(25, user.getDefaultLanguage());
      ps.setString(26, user.getDefaultTimeZone());
    });
    if (getEnableAuthorities()) {
      insertUserAuthorities(user);
    }
  }

  @Override
  public void updateUser(final UserDetails user0) {
    CustomOAuth2User user = (CustomOAuth2User) user0;
    validateUserDetails(user);
    getJdbcTemplate().update(this.updateUserSql, (ps) -> {
      ps.setString(1, user.getPassword());
      ps.setBoolean(2, user.isEnabled());
      ps.setBoolean(3, !user.isAccountNonLocked());
      ps.setBoolean(4, !user.isAccountNonExpired());
      ps.setBoolean(5, !user.isCredentialsNonExpired());
      // AngusGM User Info.
      ps.setString(6, user.getFirstName());
      ps.setString(7, user.getLastName());
      ps.setString(8, user.getFullName());
      ps.setString(9, user.getPasswordStrength());
      ps.setBoolean(10, user.isSysAdmin());
      ps.setBoolean(11, user.isToUser());
      ps.setString(12, user.getEmail());
      ps.setString(13, user.getMobile());
      ps.setString(14, user.getMainDeptId());
      ps.setTimestamp(15, user.getPasswordExpiredDate() != null
          ? Timestamp.from(user.getPasswordExpiredDate()) : null);
      ps.setTimestamp(16, user.getLastModifiedPasswordDate() != null
          ? Timestamp.from(user.getLastModifiedPasswordDate()) : null);
      ps.setTimestamp(17, user.getExpiredDate() != null
          ? Timestamp.from(user.getExpiredDate()) : null);
      ps.setBoolean(18, user.isDeleted());
      ps.setString(19, user.getTenantId());
      ps.setString(20, user.getTenantName());
      ps.setString(21, user.getTenantRealNameStatus());
      ps.setString(22, user.getDirectoryId());
      ps.setString(23, user.getDefaultLanguage());
      ps.setString(24, user.getDefaultTimeZone());
      ps.setString(25, user.getUsername());
    });
    if (getEnableAuthorities()) {
      deleteUserAuthorities(user.getUsername());
      insertUserAuthorities(user);
    }
    this.userCache.removeUserFromCache(user.getUsername());
  }

  private void insertUserAuthorities(UserDetails user) {
    for (GrantedAuthority auth : user.getAuthorities()) {
      getJdbcTemplate().update(this.createAuthoritySql, user.getUsername(), auth.getAuthority());
    }
  }

  @Override
  public void deleteUser(String username) {
    if (getEnableAuthorities()) {
      deleteUserAuthorities(username);
    }
    getJdbcTemplate().update(this.deleteUserSql, username);
    this.userCache.removeUserFromCache(username);
  }

  private void deleteUserAuthorities(String username) {
    getJdbcTemplate().update(this.deleteUserAuthoritiesSql, username);
  }

  @Override
  public void changePassword(String oldPassword, String newPassword)
      throws AuthenticationException {
    Authentication currentUser = this.securityContextHolderStrategy.getContext()
        .getAuthentication();
    if (currentUser == null) {
      // This would indicate bad coding somewhere
      throw new AccessDeniedException(
          "Can't change password as no Authentication object found in context "
              + "for current user.");
    }
    String username = currentUser.getName();
    // If an authentication manager has been set, re-authenticate the user with the
    // supplied password.
    if (this.authenticationManager != null) {
      logger.debug(
          LogMessage.format("Reauthenticating user '%s' for password change request.", username));
      this.authenticationManager
          .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, oldPassword));
    } else {
      logger.debug("No authentication manager set. Password won't be re-checked.");
    }
    logger.debug("Changing password for user '" + username + "'");
    getJdbcTemplate().update(this.changePasswordSql, newPassword, username);
    Authentication authentication = createNewAuthentication(currentUser, newPassword);
    SecurityContext context = this.securityContextHolderStrategy.createEmptyContext();
    context.setAuthentication(authentication);
    this.securityContextHolderStrategy.setContext(context);
    this.userCache.removeUserFromCache(username);
  }

  protected Authentication createNewAuthentication(Authentication currentAuth, String newPassword) {
    UserDetails user = loadUserByUsername(currentAuth.getName());
    UsernamePasswordAuthenticationToken newAuthentication = UsernamePasswordAuthenticationToken.authenticated(
        user, null, user.getAuthorities());
    newAuthentication.setDetails(currentAuth.getDetails());
    return newAuthentication;
  }

  @Override
  public boolean userExists(String username) {
    List<String> users = getJdbcTemplate().queryForList(this.userExistsSql, new String[]{username},
        String.class);
    if (users.size() > 1) {
      throw new IncorrectResultSizeDataAccessException(
          "More than one user found with name '" + username + "'", 1);
    }
    return users.size() == 1;
  }

  @Override
  public CustomOAuth2User findByAccount(String account) throws AccountNotFoundException {
    List<UserDetails> users = loadUsersByAccount(account);
    if (users.isEmpty()) {
      logger.debug("Query returned no results for user '" + account + "'");
      throw new AccountNotFoundException(this.messages.getMessage("JdbcDaoImpl.notFound",
          new Object[]{account}, "Account {0} not found"));
    }

    // contains no GrantedAuthority[]
    UserDetails user = users.stream().filter(UserDetails::isEnabled).toList().get(0);
    Set<GrantedAuthority> dbAuthsSet = new HashSet<>();
    // Load permissions from the OAuth2 authorization table
    if (getEnableAuthorities()) {
      dbAuthsSet.addAll(loadUserAuthorities(user.getUsername()));
    }
    // Lazy load business permissions, such as: authorization policies, resource, and operational role permissions
    if (nonNull(authoritiesLazyRepository)) {
      dbAuthsSet.addAll(authoritiesLazyRepository.lazyUserAuthorities((CustomOAuth2User)user));
    }

    List<GrantedAuthority> dbAuths = new ArrayList<>(dbAuthsSet);
    addCustomAuthorities(user.getUsername(), dbAuths);
    if (dbAuths.isEmpty()) {
      logger.debug("User '" + account + "' has no authorities and will be treated as 'not found'");
    }
    return createUserDetails(null, user, dbAuths);
  }

  /**
   * Executes the SQL <tt>DEF_USERS_BY_ACCOUNT_QUERY</tt> and returns a list of UserDetails objects.
   * There should normally only be one matching user.
   */
  protected List<UserDetails> loadUsersByAccount(String account) {
    RowMapper<UserDetails> mapper = (rs, rowNum) -> mapToUser(rs);
    return getJdbcTemplate().query(this.usersByAccountSql, mapper, account, account, account);
  }

  @Override
  public boolean accountExists(String account) {
    List<String> usernames = getJdbcTemplate().queryForList(this.userAccountExistsSql,
        new String[]{account, account, account}, String.class);
    return !usernames.isEmpty();
  }

  @Override
  public void updateToDeleted(String username) {
    getJdbcTemplate().update(this.userDeletedStatusSql, username);
  }

  /**
   * Sets the {@link SecurityContextHolderStrategy} to use. The default action is to use the
   * {@link SecurityContextHolderStrategy} stored in {@link SecurityContextHolder}.
   *
   * @since 5.8
   */
  public void setSecurityContextHolderStrategy(
      SecurityContextHolderStrategy securityContextHolderStrategy) {
    Assert.notNull(securityContextHolderStrategy, "securityContextHolderStrategy cannot be null");
    this.securityContextHolderStrategy = securityContextHolderStrategy;
  }

  public void setAuthenticationManager(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  public void setAuthoritiesLazyRepository(
      @Nullable JdbcUserAuthoritiesLazyRepository authoritiesLazyRepository) {
    this.authoritiesLazyRepository = authoritiesLazyRepository;
  }

  public void setCreateUserSql(String createUserSql) {
    Assert.hasText(createUserSql, "createUserSql should have text");
    this.createUserSql = createUserSql;
  }

  public void setDeleteUserSql(String deleteUserSql) {
    Assert.hasText(deleteUserSql, "deleteUserSql should have text");
    this.deleteUserSql = deleteUserSql;
  }

  public void setUpdateUserSql(String updateUserSql) {
    Assert.hasText(updateUserSql, "updateUserSql should have text");
    this.updateUserSql = updateUserSql;
  }

  public void setCreateAuthoritySql(String deleteUserAuthoritiesSql) {
    Assert.hasText(createAuthoritySql, "createAuthoritySql should have text");
    this.createAuthoritySql = createAuthoritySql;
  }

  public void setDeleteUserAuthoritiesSql(String deleteUserAuthoritiesSql) {
    Assert.hasText(deleteUserAuthoritiesSql, "deleteUserAuthoritiesSql should have text");
    this.deleteUserAuthoritiesSql = deleteUserAuthoritiesSql;
  }

  public void setUserExistsSql(String userExistsSql) {
    Assert.hasText(userExistsSql, "userExistsSql should have text");
    this.userExistsSql = userExistsSql;
  }

  public void setChangePasswordSql(String changePasswordSql) {
    Assert.hasText(changePasswordSql, "changePasswordSql should have text");
    this.changePasswordSql = changePasswordSql;
  }

  public String getUserAccountExistsSql() {
    return userAccountExistsSql;
  }

  public void setUserAccountExistsSql(String userAccountExistsSql) {
    Assert.hasText(userAccountExistsSql, "userAccountExistsSql should have text");
    this.userAccountExistsSql = userAccountExistsSql;
  }

  public String getUsersByAccountSql() {
    return usersByAccountSql;
  }

  public void setUsersByAccountSql(String usersByAccountSql) {
    Assert.hasText(usersByAccountSql, "usersByAccountSql should have text");
    this.usersByAccountSql = usersByAccountSql;
  }

  public String getUserDeletedStatusSql() {
    return userDeletedStatusSql;
  }

  public void setUserDeletedStatusSql(String userDeletedStatusSql) {
    Assert.hasText(usersByAccountSql, "userDeletedStatusSql should have text");
    this.userDeletedStatusSql = userDeletedStatusSql;
  }

  /**
   * Optionally sets the UserCache if one is in use in the application. This allows the user to be
   * removed from the cache after updates have taken place to avoid stale data.
   *
   * @param userCache the cache used by the AuthenticationManager.
   */
  public void setUserCache(UserCache userCache) {
    Assert.notNull(userCache, "userCache cannot be null");
    this.userCache = userCache;
  }

  private void validateUserDetails(UserDetails user) {
    Assert.hasText(user.getUsername(), "Username may not be empty or null");
    validateAuthorities(user.getAuthorities());
  }

  private void validateAuthorities(Collection<? extends GrantedAuthority> authorities) {
    Assert.notNull(authorities, "Authorities list must not be null");
    for (GrantedAuthority authority : authorities) {
      Assert.notNull(authority, "Authorities list contains a null entry");
      Assert.hasText(authority.getAuthority(),
          "getAuthority() method must return a non-empty string");
    }
  }

}
