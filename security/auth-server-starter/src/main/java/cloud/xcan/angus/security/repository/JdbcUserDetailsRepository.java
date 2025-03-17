package cloud.xcan.angus.security.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.log.LogMessage;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.util.Assert;

/**
 * Jdbc user management service, based on the same table structure as its parent class,
 * <tt>JdbcUserAuthoritiesDaoImpl</tt>.
 * <p>
 * Provides CRUD operations for users. Note that if the
 * {@link #setEnableAuthorities(boolean) enableAuthorities} property is set to false, calls to
 * createUser, updateUser and deleteUser will not store the authorities from the
 * <tt>UserDetails</tt> . it's important that you take this into account when using this
 * implementation for managing your users.
 */
public class JdbcUserDetailsRepository extends JdbcUserAuthoritiesDaoImpl implements
    UserDetailsManager {

  // @formatter:off
  // TODO Customization definition
  public static final String DEF_CREATE_USER_SQL = "insert into oauth2_user (username, password, enabled) values (?,?,?)";

  public static final String DEF_DELETE_USER_SQL = "delete from oauth2_user where username = ?";

  public static final String DEF_UPDATE_USER_SQL = "update oauth2_user set password = ?, enabled = ? where username = ?";

  // TODO Customization definition
  public static final String DEF_INSERT_AUTHORITY_SQL = "insert into oauth2_authorities (username, authority) values (?,?)";

  public static final String DEF_DELETE_USER_AUTHORITIES_SQL = "delete from oauth2_authorities where username = ?";

  public static final String DEF_USER_EXISTS_SQL = "select username from oauth2_user where username = ?";

  public static final String DEF_CHANGE_PASSWORD_SQL = "update oauth2_user set password = ? where username = ?";

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

  private AuthenticationManager authenticationManager;

  private UserCache userCache = new NullUserCache();

  public JdbcUserDetailsRepository() {
  }

  public JdbcUserDetailsRepository(DataSource dataSource) {
    setDataSource(dataSource);
  }

  @Override
  protected void initDao() throws ApplicationContextException {
    if (this.authenticationManager == null) {
      this.logger.info(
          "No authentication manager set. Reauthentication of users when changing passwords will not be performed.");
    }
    super.initDao();
  }

  /**
   * Executes the SQL <tt>usersByUsernameQuery</tt> and returns a list of UserDetails objects. There
   * should normally only be one matching user.
   */
  @Override
  protected List<UserDetails> loadUsersByUsername(String username) {
    return getJdbcTemplate().query(getUsersByUsernameQuery(), this::mapToUser, username);
  }

  private UserDetails mapToUser(ResultSet rs, int rowNum) throws SQLException {
    String userName = rs.getString(1);
    String password = rs.getString(2);
    boolean enabled = rs.getBoolean(3);
    boolean accLocked = false;
    boolean accExpired = false;
    boolean credsExpired = false;
    if (rs.getMetaData().getColumnCount() > 3) {
      // NOTE: acc_locked, acc_expired and creds_expired are also to be loaded
      accLocked = rs.getBoolean(4);
      accExpired = rs.getBoolean(5);
      credsExpired = rs.getBoolean(6);
    }
    return new User(userName, password, enabled, !accExpired, !credsExpired, !accLocked,
        AuthorityUtils.NO_AUTHORITIES);
  }

  @Override
  public void createUser(final UserDetails user) {
    validateUserDetails(user);
    getJdbcTemplate().update(this.createUserSql, (ps) -> {
      ps.setString(1, user.getUsername());
      ps.setString(2, user.getPassword());
      ps.setBoolean(3, user.isEnabled());
      int paramCount = ps.getParameterMetaData().getParameterCount();
      if (paramCount > 3) {
        // NOTE: acc_locked, acc_expired and creds_expired are also to be inserted
        ps.setBoolean(4, !user.isAccountNonLocked());
        ps.setBoolean(5, !user.isAccountNonExpired());
        ps.setBoolean(6, !user.isCredentialsNonExpired());
      }
    });
    if (getEnableAuthorities()) {
      insertUserAuthorities(user);
    }
  }

  @Override
  public void updateUser(final UserDetails user) {
    validateUserDetails(user);
    getJdbcTemplate().update(this.updateUserSql, (ps) -> {
      ps.setString(1, user.getPassword());
      ps.setBoolean(2, user.isEnabled());
      int paramCount = ps.getParameterMetaData().getParameterCount();
      if (paramCount == 3) {
        ps.setString(3, user.getUsername());
      } else {
        // NOTE: acc_locked, acc_expired and creds_expired are also updated
        ps.setBoolean(3, !user.isAccountNonLocked());
        ps.setBoolean(4, !user.isAccountNonExpired());
        ps.setBoolean(5, !user.isCredentialsNonExpired());
        ps.setString(6, user.getUsername());
      }
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
      this.logger.debug(
          LogMessage.format("Reauthenticating user '%s' for password change request.", username));
      this.authenticationManager
          .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, oldPassword));
    } else {
      this.logger.debug("No authentication manager set. Password won't be re-checked.");
    }
    this.logger.debug("Changing password for user '" + username + "'");
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
        user,
        null, user.getAuthorities());
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

  private GrantedAuthority mapToGrantedAuthority(ResultSet rs, int rowNum) throws SQLException {
    String roleName = getRolePrefix() + rs.getString(3);
    return new SimpleGrantedAuthority(roleName);
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
