package cloud.xcan.angus.security.repository;

import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.security.authentication.dao.CaffeineCacheBasedClientCache;
import cloud.xcan.angus.security.client.CustomOAuth2ClientRepository;
import cloud.xcan.angus.security.client.CustomOAuth2RegisteredClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.ConfigurationSettingNames;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * A JDBC implementation of a {@link RegisteredClientRepository} that uses a {@link JdbcOperations}
 * for {@link RegisteredClient} persistence.
 *
 * <p>
 * <b>IMPORTANT:</b> This {@code RegisteredClientRepository} depends on the table
 * definition described in
 * "classpath:org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql"
 * and therefore MUST be defined in the database schema.
 *
 * <p>
 * <b>NOTE:</b> This {@code RegisteredClientRepository} is a simplified JDBC
 * implementation that MAY be used in a production environment. However, it does have limitations as
 * it likely won't perform well in an environment requiring high throughput. The expectation is that
 * the consuming application will provide their own implementation of
 * {@code RegisteredClientRepository} that meets the performance requirements for its deployment
 * environment.
 *
 * @see org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
 */
public class JdbcRegisteredClientRepository implements CustomOAuth2ClientRepository,
    RegisteredClientRepository {

  // @formatter:off
  private static final String TABLE_NAME = "oauth2_registered_client";

  private static final String PK_FILTER = "id = ?";

	private static final String ALL_COLUMN_NAMES =
      "id, client_id, client_id_issued_at, client_secret, client_secret_expires_at, client_name, "
			+ "client_authentication_methods, authorization_grant_types, redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings, "
      + "description, enabled, platform, source, biz_tag, tenant_id, tenant_name, created_by, created_date, last_modified_by, last_modified_date";

  private static final String ADD_COLUMN_NAMES =
      "id, client_id, client_id_issued_at, client_secret, client_secret_expires_at, client_name, "
          + "client_authentication_methods, authorization_grant_types, redirect_uris, post_logout_redirect_uris, scopes, client_settings, token_settings";

  private static final String LOAD_REGISTERED_CLIENT_SQL
      = "SELECT " + ALL_COLUMN_NAMES + " FROM " + TABLE_NAME + " WHERE ";

	private static final String INSERT_REGISTERED_CLIENT_SQL
      = "INSERT INTO " + TABLE_NAME + "(" + ADD_COLUMN_NAMES + ") VALUES ("
      + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String INSERT_REGISTERED_CLIENT_FULL_SQL
      = "INSERT INTO " + TABLE_NAME + "(" + ALL_COLUMN_NAMES + ") VALUES ("
      + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?"
      + ", ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private static final String UPDATE_REGISTERED_CLIENT_SQL
      = "UPDATE " + TABLE_NAME
			+ " SET client_secret = ?, client_secret_expires_at = ?, client_name = ?, client_authentication_methods = ?,"
			+ " authorization_grant_types = ?, redirect_uris = ?, post_logout_redirect_uris = ?, scopes = ?, client_settings = ?, token_settings = ? "
      + " WHERE " + PK_FILTER;

  private static final String UPDATE_REGISTERED_CLIENT_FULL_SQL
      = "UPDATE " + TABLE_NAME
      + " SET client_secret = ?, client_secret_expires_at = ?, client_name = ?, client_authentication_methods = ?,"
      + " authorization_grant_types = ?, redirect_uris = ?, post_logout_redirect_uris = ?, scopes = ?, client_settings = ?, token_settings = ? "
      + " ,description = ?, enabled = ?, platform = ?, source = ?, biz_tag = ?, tenant_id = ?,"
      + " tenant_name = ?, created_by = ?, created_date = ?, last_modified_by = ?, last_modified_date = ? "
      + " WHERE " + PK_FILTER;

  private static final String DELETE_REGISTERED_CLIENT_SQL = "DELETE FROM " + TABLE_NAME + " WHERE ";

  private static final String COUNT_REGISTERED_CLIENT_SQL
      = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE ";

  // @formatter:on

  private final JdbcOperations jdbcOperations;

  private RowMapper<CustomOAuth2RegisteredClient> registeredClientRowMapper;

  private Function<RegisteredClient, List<SqlParameterValue>> registeredClientParametersMapper;

  private CaffeineCacheBasedClientCache clientCache;

  /**
   * Constructs a {@code JdbcRegisteredClientRepository} using the provided parameters.
   *
   * @param jdbcOperations the JDBC operations
   */
  public JdbcRegisteredClientRepository(JdbcOperations jdbcOperations) {
    Assert.notNull(jdbcOperations, "jdbcOperations cannot be null");
    this.jdbcOperations = jdbcOperations;
    this.registeredClientRowMapper = new RegisteredClientRowMapper();
    this.registeredClientParametersMapper = new RegisteredClientParametersMapper();
    this.clientCache = new CaffeineCacheBasedClientCache();
  }

  @Override
  public void save(RegisteredClient registeredClient) {
    Assert.notNull(registeredClient, "registeredClient cannot be null");
    RegisteredClient existingRegisteredClient = findBy(PK_FILTER, registeredClient.getId());
    if (existingRegisteredClient != null) {
      updateRegisteredClient(registeredClient);
    } else {
      insertRegisteredClient(registeredClient);
    }
  }

  private void updateRegisteredClient(RegisteredClient registeredClient) {
    List<SqlParameterValue> parameters = new ArrayList<>(
        this.registeredClientParametersMapper.apply(registeredClient));
    SqlParameterValue id = parameters.remove(0);
    parameters.remove(0); // remove client_id
    parameters.remove(0); // remove client_id_issued_at
    parameters.add(id);
    PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters.toArray());
    if (registeredClient instanceof CustomOAuth2RegisteredClient) {
      this.jdbcOperations.update(UPDATE_REGISTERED_CLIENT_FULL_SQL, pss);
    } else {
      this.jdbcOperations.update(UPDATE_REGISTERED_CLIENT_SQL, pss);
    }
  }

  private void insertRegisteredClient(RegisteredClient registeredClient) {
    assertUniqueIdentifiers(registeredClient);
    List<SqlParameterValue> parameters = this.registeredClientParametersMapper.apply(
        registeredClient);
    PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters.toArray());
    if (registeredClient instanceof CustomOAuth2RegisteredClient) {
      this.jdbcOperations.update(INSERT_REGISTERED_CLIENT_FULL_SQL, pss);
    } else {
      this.jdbcOperations.update(INSERT_REGISTERED_CLIENT_SQL, pss);
    }
  }

  private void assertUniqueIdentifiers(RegisteredClient registeredClient) {
    Integer count = this.jdbcOperations.queryForObject(
        COUNT_REGISTERED_CLIENT_SQL + "client_id = ?", Integer.class,
        registeredClient.getClientId());
    if (count != null && count > 0) {
      throw new IllegalArgumentException("Registered client must be unique. "
          + "Found duplicate client identifier: " + registeredClient.getClientId());
    }
    if (StringUtils.hasText(registeredClient.getClientSecret())) {
      count = this.jdbcOperations.queryForObject(COUNT_REGISTERED_CLIENT_SQL + "client_secret = ?",
          Integer.class, registeredClient.getClientSecret());
      if (count != null && count > 0) {
        throw new IllegalArgumentException("Registered client must be unique. "
            + "Found duplicate client secret for identifier: " + registeredClient.getId());
      }
    }
  }

  @Override
  public CustomOAuth2RegisteredClient findById(String id) {
    Assert.hasText(id, "id cannot be empty");
    CustomOAuth2RegisteredClient client = clientCache.getClientFromCache(id);
    if (client == null) {
      client = findBy("id = ?", id);
      clientCache.putClientInCache(id, client);
    }
    return client;
  }

  @Override
  public CustomOAuth2RegisteredClient findByClientId(String clientId) {
    Assert.hasText(clientId, "clientId cannot be empty");
    CustomOAuth2RegisteredClient client = clientCache.getClientFromCache(clientId);
    if (client == null) {
      client = findBy("client_id = ?", clientId);
      clientCache.putClientInCache(clientId, client);
    }
    return client;
  }

  @Override
  public List<CustomOAuth2RegisteredClient> findAllBy(String filter, String... args) {
    return this.jdbcOperations.query(LOAD_REGISTERED_CLIENT_SQL + filter,
        this.registeredClientRowMapper, (Object[]) args);
  }

  @Override
  public void deleteByClientId(String clientId) {
    this.jdbcOperations.update(DELETE_REGISTERED_CLIENT_SQL + "client_id = ?",
        new SqlParameterValue(Types.VARCHAR, clientId));
    clientCache.removeClientFromCache(clientId);
  }

  @Override
  public void deleteById(String id) {
    RegisteredClient client = findById(id);
    if (nonNull(client)) {
      deleteByClientId(client.getClientId());
      clientCache.removeClientFromCache(client.getClientId());
    }
  }

  private CustomOAuth2RegisteredClient findBy(String filter, Object... args) {
    List<CustomOAuth2RegisteredClient> result = this.jdbcOperations.query(
        LOAD_REGISTERED_CLIENT_SQL + filter,
        this.registeredClientRowMapper, args);
    return !result.isEmpty() ? result.get(0) : null;
  }

  /**
   * Sets the {@link RowMapper} used for mapping the current row in {@code java.sql.ResultSet} to
   * {@link RegisteredClient}. The default is {@link RegisteredClientRowMapper}.
   *
   * @param registeredClientRowMapper the {@link RowMapper} used for mapping the current row in
   *                                  {@code ResultSet} to {@link RegisteredClient}
   */
  public final void setRegisteredClientRowMapper(
      RowMapper<CustomOAuth2RegisteredClient> registeredClientRowMapper) {
    Assert.notNull(registeredClientRowMapper, "registeredClientRowMapper cannot be null");
    this.registeredClientRowMapper = registeredClientRowMapper;
  }

  /**
   * Sets the {@code Function} used for mapping {@link RegisteredClient} to a {@code List} of
   * {@link SqlParameterValue}. The default is {@link RegisteredClientParametersMapper}.
   *
   * @param registeredClientParametersMapper the {@code Function} used for mapping
   *                                         {@link RegisteredClient} to a {@code List} of
   *                                         {@link SqlParameterValue}
   */
  public final void setRegisteredClientParametersMapper(
      Function<RegisteredClient, List<SqlParameterValue>> registeredClientParametersMapper) {
    Assert.notNull(registeredClientParametersMapper,
        "registeredClientParametersMapper cannot be null");
    this.registeredClientParametersMapper = registeredClientParametersMapper;
  }

  protected final JdbcOperations getJdbcOperations() {
    return this.jdbcOperations;
  }

  protected final RowMapper<CustomOAuth2RegisteredClient> getRegisteredClientRowMapper() {
    return this.registeredClientRowMapper;
  }

  protected final Function<RegisteredClient, List<SqlParameterValue>> getRegisteredClientParametersMapper() {
    return this.registeredClientParametersMapper;
  }

  public CaffeineCacheBasedClientCache getClientCache() {
    return clientCache;
  }

  public void setClientCache(CaffeineCacheBasedClientCache clientCache) {
    this.clientCache = clientCache;
  }

  /**
   * The default {@link RowMapper} that maps the current row in {@code java.sql.ResultSet} to
   * {@link RegisteredClient}.
   */
  public static class RegisteredClientRowMapper implements RowMapper<CustomOAuth2RegisteredClient> {

    private ObjectMapper objectMapper = new ObjectMapper();

    public RegisteredClientRowMapper() {
      ClassLoader classLoader = JdbcRegisteredClientRepository.class.getClassLoader();
      List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
      this.objectMapper.registerModules(securityModules);
      this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
    }

    @Override
    public CustomOAuth2RegisteredClient mapRow(ResultSet rs, int rowNum) throws SQLException {
      Timestamp clientIdIssuedAt = rs.getTimestamp("client_id_issued_at");
      Timestamp clientSecretExpiresAt = rs.getTimestamp("client_secret_expires_at");
      Set<String> clientAuthenticationMethods = StringUtils
          .commaDelimitedListToSet(rs.getString("client_authentication_methods"));
      Set<String> authorizationGrantTypes = StringUtils
          .commaDelimitedListToSet(rs.getString("authorization_grant_types"));
      Set<String> redirectUris = StringUtils.commaDelimitedListToSet(rs.getString("redirect_uris"));
      Set<String> postLogoutRedirectUris = StringUtils
          .commaDelimitedListToSet(rs.getString("post_logout_redirect_uris"));
      Set<String> clientScopes = StringUtils.commaDelimitedListToSet(rs.getString("scopes"));

      // @formatter:off
      CustomOAuth2RegisteredClient.Builder builder = CustomOAuth2RegisteredClient
          .withId0(rs.getString("id"))
					.clientId(rs.getString("client_id"))
					.clientIdIssuedAt((clientIdIssuedAt != null) ? clientIdIssuedAt.toInstant() : null)
					.clientSecret(rs.getString("client_secret"))
					.clientSecretExpiresAt((clientSecretExpiresAt != null) ? clientSecretExpiresAt.toInstant() : null)
					.clientName(rs.getString("client_name"))
					.clientAuthenticationMethods((authenticationMethods) ->
							clientAuthenticationMethods.forEach((authenticationMethod) ->
									authenticationMethods.add(resolveClientAuthenticationMethod(authenticationMethod))))
					.authorizationGrantTypes((grantTypes) ->
							authorizationGrantTypes.forEach((grantType) ->
									grantTypes.add(resolveAuthorizationGrantType(grantType))))
					.redirectUris((uris) -> uris.addAll(redirectUris))
					.postLogoutRedirectUris((uris) -> uris.addAll(postLogoutRedirectUris))
					.scopes((scopes) -> scopes.addAll(clientScopes));
			// @formatter:on

      Map<String, Object> clientSettingsMap = parseMap(rs.getString("client_settings"));
      builder.clientSettings(ClientSettings.withSettings(clientSettingsMap).build());

      Map<String, Object> tokenSettingsMap = parseMap(rs.getString("token_settings"));
      TokenSettings.Builder tokenSettingsBuilder = TokenSettings.withSettings(tokenSettingsMap);
      if (!tokenSettingsMap.containsKey(ConfigurationSettingNames.Token.ACCESS_TOKEN_FORMAT)) {
        tokenSettingsBuilder.accessTokenFormat(OAuth2TokenFormat.SELF_CONTAINED);
      }
      builder.tokenSettings(tokenSettingsBuilder.build());

      // AngusGM Client Info.
      Timestamp createdDate = rs.getTimestamp("created_date");
      Timestamp lastModifiedDate = rs.getTimestamp("last_modified_date");
      builder.description(rs.getString("description"))
          .enabled(rs.getBoolean("enabled"))
          .platform(rs.getString("platform"))
          .source(rs.getString("source"))
          .bizTag(rs.getString("biz_tag"))
          .tenantId(rs.getString("tenant_id"))
          .tenantName(rs.getString("tenant_name"))
          .createdBy(rs.getString("created_by"))
          .createdDate(createdDate != null ? createdDate.toInstant() : null)
          .lastModifiedBy(rs.getString("last_modified_by"))
          .lastModifiedDate(lastModifiedDate != null ? lastModifiedDate.toInstant() : null);
      return builder.build();
    }

    public final void setObjectMapper(ObjectMapper objectMapper) {
      Assert.notNull(objectMapper, "objectMapper cannot be null");
      this.objectMapper = objectMapper;
    }

    protected final ObjectMapper getObjectMapper() {
      return this.objectMapper;
    }

    private Map<String, Object> parseMap(String data) {
      try {
        return this.objectMapper.readValue(data, new TypeReference<Map<String, Object>>() {
        });
      } catch (Exception ex) {
        throw new IllegalArgumentException(ex.getMessage(), ex);
      }
    }

    private static AuthorizationGrantType resolveAuthorizationGrantType(
        String authorizationGrantType) {
      if (AuthorizationGrantType.AUTHORIZATION_CODE.getValue().equals(authorizationGrantType)) {
        return AuthorizationGrantType.AUTHORIZATION_CODE;
      } else if (AuthorizationGrantType.CLIENT_CREDENTIALS.getValue()
          .equals(authorizationGrantType)) {
        return AuthorizationGrantType.CLIENT_CREDENTIALS;
      } else if (AuthorizationGrantType.REFRESH_TOKEN.getValue().equals(authorizationGrantType)) {
        return AuthorizationGrantType.REFRESH_TOKEN;
      }
      // Custom authorization grant type
      return new AuthorizationGrantType(authorizationGrantType);
    }

    private static ClientAuthenticationMethod resolveClientAuthenticationMethod(
        String clientAuthenticationMethod) {
      if (ClientAuthenticationMethod.CLIENT_SECRET_BASIC.getValue()
          .equals(clientAuthenticationMethod)) {
        return ClientAuthenticationMethod.CLIENT_SECRET_BASIC;
      } else if (ClientAuthenticationMethod.CLIENT_SECRET_POST.getValue()
          .equals(clientAuthenticationMethod)) {
        return ClientAuthenticationMethod.CLIENT_SECRET_POST;
      } else if (ClientAuthenticationMethod.NONE.getValue().equals(clientAuthenticationMethod)) {
        return ClientAuthenticationMethod.NONE;
      }
      // Custom client authentication method
      return new ClientAuthenticationMethod(clientAuthenticationMethod);
    }

  }

  /**
   * The default {@code Function} that maps {@link RegisteredClient} to a {@code List} of
   * {@link SqlParameterValue}.
   */
  public static class RegisteredClientParametersMapper
      implements Function<RegisteredClient, List<SqlParameterValue>> {

    private ObjectMapper objectMapper = new ObjectMapper();

    public RegisteredClientParametersMapper() {
      ClassLoader classLoader = JdbcRegisteredClientRepository.class.getClassLoader();
      List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
      this.objectMapper.registerModules(securityModules);
      this.objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
    }

    @Override
    public List<SqlParameterValue> apply(RegisteredClient registeredClient0) {
      if (registeredClient0 instanceof CustomOAuth2RegisteredClient registeredClient) {
        Timestamp clientIdIssuedAt = (registeredClient0.getClientIdIssuedAt() != null)
            ? Timestamp.from(registeredClient0.getClientIdIssuedAt())
            : Timestamp.from(Instant.now());
        Timestamp clientSecretExpiresAt = (registeredClient0.getClientSecretExpiresAt() != null)
            ? Timestamp.from(registeredClient0.getClientSecretExpiresAt()) : null;
        List<String> clientAuthenticationMethods = new ArrayList<>(
            registeredClient0.getClientAuthenticationMethods().size());
        registeredClient0.getClientAuthenticationMethods()
            .forEach((clientAuthenticationMethod) -> clientAuthenticationMethods
                .add(clientAuthenticationMethod.getValue()));
        List<String> authorizationGrantTypes = new ArrayList<>(
            registeredClient0.getAuthorizationGrantTypes().size());
        registeredClient0.getAuthorizationGrantTypes()
            .forEach((authorizationGrantType) -> authorizationGrantTypes.add(
                authorizationGrantType.getValue()));

        // AngusGM Client Info.
        Timestamp createdDate = (registeredClient.getCreatedDate() != null)
            ? Timestamp.from(registeredClient.getCreatedDate()) : Timestamp.from(Instant.now());
        Timestamp lastModifiedDate = (registeredClient.getLastModifiedDate() != null)
            ? Timestamp.from(registeredClient.getLastModifiedDate())
            : Timestamp.from(Instant.now());

        return Arrays.asList(
            new SqlParameterValue(Types.VARCHAR, registeredClient0.getId()), // Query PK ID
            new SqlParameterValue(Types.VARCHAR, registeredClient0.getClientId()),
            new SqlParameterValue(Types.TIMESTAMP, clientIdIssuedAt),
            new SqlParameterValue(Types.VARCHAR, registeredClient0.getClientSecret()),
            new SqlParameterValue(Types.TIMESTAMP, clientSecretExpiresAt),
            new SqlParameterValue(Types.VARCHAR, registeredClient0.getClientName()),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods)),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes)),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(registeredClient0.getRedirectUris())),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(
                    registeredClient0.getPostLogoutRedirectUris())),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(registeredClient0.getScopes())),
            new SqlParameterValue(Types.VARCHAR,
                writeMap(registeredClient0.getClientSettings().getSettings())),
            new SqlParameterValue(Types.VARCHAR,
                writeMap(registeredClient0.getTokenSettings().getSettings())),
            // AngusGM Client Info.
            new SqlParameterValue(Types.VARCHAR, registeredClient.getDescription()),
            new SqlParameterValue(Types.BOOLEAN, registeredClient.isEnabled()),
            new SqlParameterValue(Types.VARCHAR, registeredClient.getPlatform()),
            new SqlParameterValue(Types.VARCHAR, registeredClient.getSource()),
            new SqlParameterValue(Types.VARCHAR, registeredClient.getBizTag()),
            new SqlParameterValue(Types.BIGINT, nullSafe(registeredClient.getTenantId(), -1L)),
            new SqlParameterValue(Types.VARCHAR, registeredClient.getTenantName()),
            new SqlParameterValue(Types.BIGINT, registeredClient.getCreatedBy()),
            new SqlParameterValue(Types.TIMESTAMP, createdDate),
            new SqlParameterValue(Types.BIGINT, registeredClient.getLastModifiedBy()),
            new SqlParameterValue(Types.VARCHAR, lastModifiedDate)
        );
      } else {
        Timestamp clientIdIssuedAt = (registeredClient0.getClientIdIssuedAt() != null)
            ? Timestamp.from(registeredClient0.getClientIdIssuedAt())
            : Timestamp.from(Instant.now());

        Timestamp clientSecretExpiresAt = (registeredClient0.getClientSecretExpiresAt() != null)
            ? Timestamp.from(registeredClient0.getClientSecretExpiresAt()) : null;

        List<String> clientAuthenticationMethods = new ArrayList<>(
            registeredClient0.getClientAuthenticationMethods().size());
        registeredClient0.getClientAuthenticationMethods()
            .forEach((clientAuthenticationMethod) -> clientAuthenticationMethods
                .add(clientAuthenticationMethod.getValue()));

        List<String> authorizationGrantTypes = new ArrayList<>(
            registeredClient0.getAuthorizationGrantTypes().size());
        registeredClient0.getAuthorizationGrantTypes()
            .forEach((authorizationGrantType) -> authorizationGrantTypes.add(
                authorizationGrantType.getValue()));

        return Arrays.asList(
            new SqlParameterValue(Types.VARCHAR, registeredClient0.getId()),
            new SqlParameterValue(Types.VARCHAR, registeredClient0.getClientId()),
            new SqlParameterValue(Types.TIMESTAMP, clientIdIssuedAt),
            new SqlParameterValue(Types.VARCHAR, registeredClient0.getClientSecret()),
            new SqlParameterValue(Types.TIMESTAMP, clientSecretExpiresAt),
            new SqlParameterValue(Types.VARCHAR, registeredClient0.getClientName()),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(clientAuthenticationMethods)),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(authorizationGrantTypes)),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(registeredClient0.getRedirectUris())),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(
                    registeredClient0.getPostLogoutRedirectUris())),
            new SqlParameterValue(Types.VARCHAR,
                StringUtils.collectionToCommaDelimitedString(registeredClient0.getScopes())),
            new SqlParameterValue(Types.VARCHAR,
                writeMap(registeredClient0.getClientSettings().getSettings())),
            new SqlParameterValue(Types.VARCHAR,
                writeMap(registeredClient0.getTokenSettings().getSettings()))
        );
      }
    }

    public final void setObjectMapper(ObjectMapper objectMapper) {
      Assert.notNull(objectMapper, "objectMapper cannot be null");
      this.objectMapper = objectMapper;
    }

    protected final ObjectMapper getObjectMapper() {
      return this.objectMapper;
    }


    private String writeMap(Map<String, Object> data) {
      try {
        return this.objectMapper.writeValueAsString(data);
      } catch (Exception ex) {
        throw new IllegalArgumentException(ex.getMessage(), ex);
      }
    }
  }

}
