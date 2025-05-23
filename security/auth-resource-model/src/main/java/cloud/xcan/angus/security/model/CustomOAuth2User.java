package cloud.xcan.angus.security.model;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.stringSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.enums.PasswordStrength;
import cloud.xcan.angus.spec.experimental.EntitySupport;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Transient;
import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

/**
 * A custom OAuth2 user representation in AngusGM.
 * <p>
 * Authenticate the authorized user and be loaded by principal.
 *
 * @see `cloud.xcan.angus.spec.principal.PrincipalContext`
 * @see `cloud.xcan.angus.spec.principal.Principal`
 */
@Slf4j
@Setter
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class CustomOAuth2User extends EntitySupport<CustomOAuth2User, Long> implements UserDetails,
    CredentialsContainer, Cloneable {

  private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  /**
   * OAuth2 User fields
   */
  protected String username;

  @Nullable
  protected String password;

  protected boolean enabled;

  protected boolean accountNonExpired;

  protected boolean accountNonLocked;

  /**
   * Is password none expired.
   */
  protected boolean credentialsNonExpired;

  @Transient
  protected Set<GrantedAuthority> authorities;

  /**
   * AngusGM User Info.
   */
  protected String id;

  protected String firstName;

  protected String lastName;

  protected String fullName;

  protected String passwordStrength;

  protected boolean sysAdmin;

  protected boolean toUser;

  protected String mobile;

  protected String email;

  protected String mainDeptId;

  protected Instant passwordExpiredDate;

  protected Instant lastModifiedPasswordDate;

  protected Instant expiredDate;

  protected boolean deleted;

  protected String tenantId;

  protected String tenantName;

  protected String tenantRealNameStatus;

  protected String directoryId;

  protected String defaultLanguage;

  protected String defaultTimeZone;

  /**
   * Temp filed for signup.
   */
  @Transient
  protected String itc;
  @Transient
  protected String country;
  @JsonIgnore
  @Transient
  private String clientId;
  @JsonIgnore
  @Transient
  private String clientSource;

  /**
   * Temp filed for business.
   */
  @JsonIgnore
  @Transient
  protected String signupType;
  @JsonIgnore
  @Transient
  protected String signupDeviceId;
  @JsonIgnore
  @Transient
  protected String verificationCode;
  @JsonIgnore
  @Transient
  protected String smsBizKey;
  @JsonIgnore
  @Transient
  protected String emailBizKey;
  @JsonIgnore
  @Transient
  protected String linkSecret;
  @JsonIgnore
  @Transient
  protected boolean setPassword;
  @JsonIgnore
  @Transient
  protected String invitationCode;

  public CustomOAuth2User() {
  }

  @Override
  public Long identity() {
    return Long.parseLong(id);
  }

  /**
   * Calls the more complex constructor with all boolean arguments set to {@code true}.
   */
  public CustomOAuth2User(String username, String password,
      Collection<? extends GrantedAuthority> authorities) {
    this(username, password, true, true, true, true, authorities);
  }

  /**
   * Construct the <code>CustomOAuth2User</code> with the details required by
   * {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}.
   *
   * @param username              the username presented to the
   *                              <code>DaoAuthenticationProvider</code>
   * @param password              the password that should be presented to the
   *                              <code>DaoAuthenticationProvider</code>
   * @param enabled               set to <code>true</code> if the user is enabled
   * @param accountNonExpired     set to <code>true</code> if the account has not expired
   * @param credentialsNonExpired set to <code>true</code> if the credentials have not expired
   * @param accountNonLocked      set to <code>true</code> if the account is not locked
   * @param authorities           the authorities that should be granted to the caller if they
   *                              presented the correct username and password and the user is
   *                              enabled. Not null.
   * @throws IllegalArgumentException if a <code>null</code> value was passed either as a parameter
   *                                  or as an element in the <code>GrantedAuthority</code>
   *                                  collection
   */
  public CustomOAuth2User(String username, String password, boolean enabled,
      boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities) {
    this(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked,
        authorities, "-1", null, null, null, null, false, false, null, null, null, null,
        null, null, false, "-1", null, null, null, null, null);
  }

  public CustomOAuth2User(String username, String password, boolean enabled,
      boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
      Collection<? extends GrantedAuthority> authorities, String id, String firstName,
      String lastName, String fullName, String passwordStrength, boolean sysAdmin, boolean toUser,
      String mobile, String email, String mainDeptId, Instant passwordExpiredDate,
      Instant lastModifiedPasswordDate, Instant expiredDate,
      boolean deleted, String tenantId, String tenantName, String tenantRealNameStatus,
      String directoryId, String defaultLanguage, String defaultTimeZone) {
    Assert.isTrue(username != null && !username.isEmpty() /*&& password != null*/,
        "Cannot username null or empty values to constructor");

    this.username = username;
    this.password = password;
    this.enabled = enabled;
    this.accountNonExpired = accountNonExpired;
    this.credentialsNonExpired = credentialsNonExpired;
    this.accountNonLocked = accountNonLocked;
    this.authorities = Collections.unmodifiableSet(sortAuthorities(authorities));

    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.fullName = fullName;
    this.passwordStrength = stringSafe(passwordStrength, PasswordStrength.UNKNOWN.getValue());
    this.sysAdmin = sysAdmin;
    this.toUser = toUser;
    this.mobile = mobile;
    this.email = email;
    this.mainDeptId = mainDeptId;
    this.passwordExpiredDate = passwordExpiredDate;
    this.lastModifiedPasswordDate = lastModifiedPasswordDate;
    this.expiredDate = expiredDate;
    this.deleted = deleted;
    this.tenantId = tenantId;
    this.tenantName = tenantName;
    this.tenantRealNameStatus = tenantRealNameStatus;
    this.directoryId = directoryId;
    this.defaultLanguage = defaultLanguage;
    this.defaultTimeZone = defaultTimeZone;
  }

  @Override
  public CustomOAuth2User clone() {
    try {
      CustomOAuth2User user = (CustomOAuth2User) super.clone();
      if (isNotEmpty(user.getAuthorities())) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (GrantedAuthority authority : user.getAuthorities()) {
          GrantedAuthority clone = new SimpleGrantedAuthority(authority.getAuthority());
          authorities.add(clone);
        }
        user.setAuthorities(authorities);
      }
      return user;
    } catch (CloneNotSupportedException e) {
      return this;
    }
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public boolean isAccountNonExpired() {
    return this.accountNonExpired && (expiredDate == null || expiredDate.isAfter(Instant.now()));
  }

  @Override
  public boolean isAccountNonLocked() {
    return this.accountNonLocked;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return this.credentialsNonExpired;
  }

  @JsonIgnore
  @Transient
  public boolean isPasswordExpired() {
    return !credentialsNonExpired || nonNull(password)
        && nonNull(passwordExpiredDate) && passwordExpiredDate.isBefore(Instant.now());
  }

  public boolean isSetPassword() {
    return this.setPassword || isNotEmpty(password);
  }

  public boolean supportDirectoryAuth() {
    return Objects.nonNull(directoryId);
  }

  @Override
  public void eraseCredentials() {
    this.password = null;
  }

  @Override
  public Collection<GrantedAuthority> getAuthorities() {
    return this.authorities;
  }

  public static SortedSet<GrantedAuthority> sortAuthorities(
      Collection<? extends GrantedAuthority> authorities) {
    Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");
    // Ensure array iteration order is predictable (as per
    // UserDetails.getAuthorities() contract and SEC-717)
    SortedSet<GrantedAuthority> sortedAuthorities = new TreeSet<>(
        new CustomOAuth2User.AuthorityComparator());
    for (GrantedAuthority grantedAuthority : authorities) {
      Assert.notNull(grantedAuthority, "GrantedAuthority list cannot contain any null elements");
      sortedAuthorities.add(grantedAuthority);
    }
    return sortedAuthorities;
  }

  /**
   * Returns {@code true} if the supplied object is a {@code CustomOAuth2User} instance with the
   * same {@code username} value.
   * <p>
   * In other words, the objects are equal if they have the same username, representing the same
   * principal.
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CustomOAuth2User user) {
      return this.username.equals(user.getUsername());
    }
    return false;
  }

  /**
   * Returns the hashcode of the {@code username}.
   */
  @Override
  public int hashCode() {
    return this.username.hashCode();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("CustomOAuth2User{");
    sb.append("tenantRealNameStatus='").append(tenantRealNameStatus).append('\'');
    sb.append(", tenantName='").append(tenantName).append('\'');
    sb.append(", tenantId=").append(tenantId);
    sb.append(", expiredDate=").append(expiredDate);
    sb.append(", deleted=").append(deleted);
    sb.append(", defaultTimeZone='").append(defaultTimeZone).append('\'');
    sb.append(", defaultLanguage='").append(defaultLanguage).append('\'');
    sb.append(", lastModifiedPasswordDate=").append(lastModifiedPasswordDate);
    sb.append(", passwordExpiredDate=").append(passwordExpiredDate);
    sb.append(", mainDeptId=").append(mainDeptId);
    sb.append(", email='").append(email).append('\'');
    sb.append(", mobile='").append(mobile).append('\'');
    sb.append(", fullName='").append(fullName).append('\'');
    sb.append(", lastName='").append(lastName).append('\'');
    sb.append(", firstName='").append(firstName).append('\'');
    sb.append(", toUser=").append(toUser);
    sb.append(", sysAdmin=").append(sysAdmin);
    sb.append(", passwordStrength='").append(passwordStrength).append('\'');
    sb.append(", id=").append(id);
    sb.append(", authorities=").append(authorities);
    sb.append(", credentialsNonExpired=").append(credentialsNonExpired);
    sb.append(", accountNonLocked=").append(accountNonLocked);
    sb.append(", accountNonExpired=").append(accountNonExpired);
    sb.append(", enabled=").append(enabled);
    sb.append(", username='").append(username).append('\'');
    sb.append('}');
    return sb.toString();
  }

  /**
   * Copy a User with UserBuilder
   */
  public static CustomOAuth2User with(String username, CustomOAuth2User user,
      List<GrantedAuthority> authorities) {
    return builder().username(username).password(user.password)
        .disabled(!user.enabled).accountExpired(!user.accountNonExpired)
        .accountLocked(!user.accountNonLocked).credentialsExpired(!user.credentialsNonExpired)
        .authorities(authorities).id(user.id).firstName(user.firstName).lastName(user.lastName)
        .fullName(user.fullName).passwordStrength(user.passwordStrength).sysAdmin(user.sysAdmin)
        .toUser(user.toUser).mobile(user.mobile).email(user.email).mainDeptId(user.mainDeptId)
        .passwordExpiredDate(user.passwordExpiredDate)
        .lastModifiedPasswordDate(user.lastModifiedPasswordDate).expiredDate(user.expiredDate)
        .deleted(user.deleted).tenantId(user.tenantId).tenantName(user.tenantName)
        .tenantRealNameStatus(user.tenantRealNameStatus)
        .build();
  }

  /**
   * Creates a UserBuilder with a specified username
   *
   * @param username the username to use
   * @return the UserBuilder
   */
  public static CustomOAuth2User.UserBuilder withUsername(String username) {
    return builder().username(username);
  }

  /**
   * Creates a UserBuilder
   *
   * @return the UserBuilder
   */
  public static CustomOAuth2User.UserBuilder builder() {
    return new CustomOAuth2User.UserBuilder();
  }

  /**
   * <p>
   * <b>WARNING:</b> This method is considered unsafe for production and is only
   * intended for sample applications.
   * </p>
   * <p>
   * Creates a user and automatically encodes the provided password using
   * {@code PasswordEncoderFactories.createDelegatingPasswordEncoder()}. For example:
   * </p>
   *
   * <pre>
   * <code>
   * UserDetails user = CustomOAuth2User.withDefaultPasswordEncoder()
   *     .username("user")
   *     .password("password")
   *     .roles("USER")
   *     .build();
   * // outputs {bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
   * System.out.println(user.getPassword());
   * </code> </pre>
   * <p>
   * This is not safe for production (it is intended for getting started experience) because the
   * password "password" is compiled into the source code and then is included in memory at the time
   * of creation. This means there are still ways to recover the plain text password making it
   * unsafe. It does provide a slight improvement to using plain text passwords since the
   * UserDetails password is securely hashed. This means if the UserDetails password is accidentally
   * exposed, the password is securely stored.
   * <p>
   * In a production setting, it is recommended to hash the password ahead of time. For example:
   *
   * <pre>
   * <code>
   * PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
   * // outputs {bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG
   * // remember the password that is printed out and use in the next step
   * System.out.println(encoder.encode("password"));
   * </code> </pre>
   *
   * <pre>
   * <code>
   * UserDetails user = CustomOAuth2User.withUsername("user")
   *     .password("{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG")
   *     .roles("USER")
   *     .build();
   * </code> </pre>
   *
   * @return a UserBuilder that automatically encodes the password with the default PasswordEncoder
   * @deprecated Using this method is not considered safe for production, but is acceptable for
   * demos and getting started. For production purposes, ensure the password is encoded externally.
   * See the method Javadoc for additional details. There are no plans to remove this support. It is
   * deprecated to indicate that this is considered insecure for production purposes.
   */
  @Deprecated
  public static CustomOAuth2User.UserBuilder withDefaultPasswordEncoder() {
    log.warn("CustomOAuth2User.withDefaultPasswordEncoder() is considered unsafe for production "
        + "and is only intended for sample applications.");
    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    return builder().passwordEncoder(encoder::encode);
  }

  public static CustomOAuth2User.UserBuilder withUserDetails(UserDetails userDetails) {
    // @formatter:off
    return withUsername(userDetails.getUsername())
        .password(userDetails.getPassword())
        .accountExpired(!userDetails.isAccountNonExpired())
        .accountLocked(!userDetails.isAccountNonLocked())
        .authorities(userDetails.getAuthorities())
        .credentialsExpired(!userDetails.isCredentialsNonExpired())
        .disabled(!userDetails.isEnabled());
    // @formatter:on
  }

  private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    @Override
    public int compare(GrantedAuthority g1, GrantedAuthority g2) {
      // Neither should ever be null as each entry is checked before adding it to
      // the set. If the authority is null, it is a custom authority and should
      // precede others.
      if (g2.getAuthority() == null) {
        return -1;
      }
      if (g1.getAuthority() == null) {
        return 1;
      }
      return g1.getAuthority().compareTo(g2.getAuthority());
    }

  }

  /**
   * Builds the user to be added. At minimum the username, password, and authorities should
   * provided. The remaining attributes have reasonable defaults.
   */
  public static final class UserBuilder {

    /**
     * OAuth2 User fields
     */
    private String username;
    private String password;
    private boolean disabled = false;
    private boolean accountExpired = false;
    private boolean accountLocked = false;
    private boolean credentialsExpired = false;

    private List<GrantedAuthority> authorities = new ArrayList<>();
    private Function<String, String> passwordEncoder = (password) -> password;

    /**
     * AngusGM User Info.
     */
    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String passwordStrength;
    private boolean sysAdmin = false;
    private boolean toUser = false;
    private String mobile;
    private String email;
    private String mainDeptId;
    private Instant passwordExpiredDate;
    private Instant lastModifiedPasswordDate;
    private Instant expiredDate;
    private boolean deleted = false;
    private String tenantId;
    private String tenantName;
    private String tenantRealNameStatus;

    private String directoryId;
    private String defaultLanguage;
    private String defaultTimeZone;

    /**
     * Creates a new instance
     */
    private UserBuilder() {
    }

    /**
     * Populates the username. This attribute is required.
     *
     * @param username the username. Cannot be null.
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     */
    public CustomOAuth2User.UserBuilder username(String username) {
      Assert.notNull(username, "username cannot be null");
      this.username = username;
      return this;
    }

    /**
     * Populates the password. This attribute is not required.
     *
     * @param password the password. Can be null.
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     */
    public CustomOAuth2User.UserBuilder password(String password) {
      // Assert.notNull(password, "password cannot be null");
      this.password = password;
      return this;
    }

    /**
     * Defines if the account is disabled or not. Default is false.
     *
     * @param disabled true if the account is disabled, false otherwise
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     */
    public CustomOAuth2User.UserBuilder disabled(boolean disabled) {
      this.disabled = disabled;
      return this;
    }

    /**
     * Encodes the current password (if non-null) and any future passwords supplied to
     * {@link #password(String)}.
     *
     * @param encoder the encoder to use
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     */
    public CustomOAuth2User.UserBuilder passwordEncoder(Function<String, String> encoder) {
      Assert.notNull(encoder, "encoder cannot be null");
      this.passwordEncoder = encoder;
      return this;
    }

    /**
     * Defines if the account is expired or not. Default is false.
     *
     * @param accountExpired true if the account is expired, false otherwise
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     */
    public CustomOAuth2User.UserBuilder accountExpired(boolean accountExpired) {
      this.accountExpired = accountExpired;
      return this;
    }

    /**
     * Defines if the account is locked or not. Default is false.
     *
     * @param accountLocked true if the account is locked, false otherwise
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     */
    public CustomOAuth2User.UserBuilder accountLocked(boolean accountLocked) {
      this.accountLocked = accountLocked;
      return this;
    }

    /**
     * Defines if the credentials are expired or not. Default is false.
     *
     * @param credentialsExpired true if the credentials are expired, false otherwise
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     */
    public CustomOAuth2User.UserBuilder credentialsExpired(boolean credentialsExpired) {
      this.credentialsExpired = credentialsExpired;
      return this;
    }

    /**
     * Populates the roles. This method is a shortcut for calling {@link #authorities(String...)},
     * but automatically prefixes each entry with "ROLE_". This means the following:
     *
     * <code>
     * builder.roles("USER","ADMIN");
     * </code>
     * <p>
     * is equivalent to
     *
     * <code>
     * builder.authorities("ROLE_USER","ROLE_ADMIN");
     * </code>
     *
     * <p>
     * This attribute is required, but can also be populated with {@link #authorities(String...)}.
     * </p>
     *
     * @param roles the roles for this user (i.e. USER, ADMIN, etc). Cannot be null, contain null
     *              values or start with "ROLE_"
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     */
    public CustomOAuth2User.UserBuilder roles(String... roles) {
      List<GrantedAuthority> authorities = new ArrayList<>(roles.length);
      for (String role : roles) {
        Assert.isTrue(!role.startsWith("ROLE_"),
            () -> role + " cannot start with ROLE_ (it is automatically added)");
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
      }
      return authorities(authorities);
    }

    /**
     * Populates the authorities. This attribute is required.
     *
     * @param authorities the authorities for this user. Cannot be null, or contain null values
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     * @see #roles(String...)
     */
    public CustomOAuth2User.UserBuilder authorities(GrantedAuthority... authorities) {
      Assert.notNull(authorities, "authorities cannot be null");
      return authorities(Arrays.asList(authorities));
    }

    /**
     * Populates the authorities. This attribute is required.
     *
     * @param authorities the authorities for this user. Cannot be null, or contain null values
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     * @see #roles(String...)
     */
    public CustomOAuth2User.UserBuilder authorities(
        Collection<? extends GrantedAuthority> authorities) {
      Assert.notNull(authorities, "authorities cannot be null");
      this.authorities = new ArrayList<>(authorities);
      return this;
    }

    /**
     * Populates the authorities. This attribute is required.
     *
     * @param authorities the authorities for this user (i.e. ROLE_USER, ROLE_ADMIN, etc). Cannot be
     *                    null, or contain null values
     * @return the {@link CustomOAuth2User.UserBuilder} for method chaining (i.e. to populate
     * additional attributes for this user)
     * @see #roles(String...)
     */
    public CustomOAuth2User.UserBuilder authorities(String... authorities) {
      Assert.notNull(authorities, "authorities cannot be null");
      return authorities(AuthorityUtils.createAuthorityList(authorities));
    }

    public CustomOAuth2User.UserBuilder id(String id) {
      this.id = id;
      return this;
    }

    public CustomOAuth2User.UserBuilder firstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public CustomOAuth2User.UserBuilder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public CustomOAuth2User.UserBuilder fullName(String fullName) {
      this.fullName = fullName;
      return this;
    }

    public CustomOAuth2User.UserBuilder passwordStrength(String passwordStrength) {
      this.passwordStrength = passwordStrength;
      return this;
    }

    public CustomOAuth2User.UserBuilder sysAdmin(boolean sysAdmin) {
      this.sysAdmin = sysAdmin;
      return this;
    }

    public CustomOAuth2User.UserBuilder toUser(boolean toUser) {
      this.toUser = toUser;
      return this;
    }

    public CustomOAuth2User.UserBuilder mobile(String mobile) {
      this.mobile = mobile;
      return this;
    }

    public CustomOAuth2User.UserBuilder email(String email) {
      this.email = email;
      return this;
    }

    public CustomOAuth2User.UserBuilder mainDeptId(String mainDeptId) {
      this.mainDeptId = mainDeptId;
      return this;
    }

    public CustomOAuth2User.UserBuilder passwordExpiredDate(Instant passwordExpiredDate) {
      this.passwordExpiredDate = passwordExpiredDate;
      return this;
    }

    public CustomOAuth2User.UserBuilder lastModifiedPasswordDate(Instant lastModifiedPasswordDate) {
      this.lastModifiedPasswordDate = lastModifiedPasswordDate;
      return this;
    }

    public CustomOAuth2User.UserBuilder expiredDate(Instant expiredDate) {
      this.expiredDate = expiredDate;
      return this;
    }

    public CustomOAuth2User.UserBuilder deleted(boolean deleted) {
      this.deleted = deleted;
      return this;
    }

    public CustomOAuth2User.UserBuilder tenantId(String tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public CustomOAuth2User.UserBuilder tenantName(String tenantName) {
      this.tenantName = tenantName;
      return this;
    }

    public CustomOAuth2User.UserBuilder tenantRealNameStatus(String tenantRealNameStatus) {
      this.tenantRealNameStatus = tenantRealNameStatus;
      return this;
    }

    public CustomOAuth2User.UserBuilder directoryId(String directoryId) {
      this.directoryId = directoryId;
      return this;
    }

    public CustomOAuth2User.UserBuilder defaultLanguage(String defaultLanguage) {
      this.defaultLanguage = defaultLanguage;
      return this;
    }

    public CustomOAuth2User.UserBuilder defaultTimeZone(String defaultTimeZone) {
      this.defaultTimeZone = defaultTimeZone;
      return this;
    }

    public CustomOAuth2User build() {
      String encodedPassword = nonNull(password) ? passwordEncoder.apply(this.password) : null;
      return new CustomOAuth2User(this.username, encodedPassword, !this.disabled,
          !this.accountExpired, !this.credentialsExpired, !this.accountLocked, this.authorities,
          this.id, this.firstName, this.lastName, this.fullName, this.passwordStrength,
          this.sysAdmin, this.toUser, this.mobile, this.email, this.mainDeptId,
          this.passwordExpiredDate, this.lastModifiedPasswordDate, this.expiredDate,
          this.deleted, this.tenantId, this.tenantName, this.tenantRealNameStatus,
          this.directoryId, this.defaultLanguage, this.defaultTimeZone);
    }
  }

}
