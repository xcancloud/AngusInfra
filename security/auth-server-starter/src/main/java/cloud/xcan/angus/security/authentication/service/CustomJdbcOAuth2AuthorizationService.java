package cloud.xcan.angus.security.authentication.service;

import static cloud.xcan.angus.spec.experimental.BizConstant.isUserSignInToken;

import cloud.xcan.angus.spec.experimental.BizConstant.AuthKey;
import java.sql.Types;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.Assert;

public class CustomJdbcOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService
    implements cloud.xcan.angus.security.authentication.service.JdbcOAuth2AuthorizationService {

  // @formatter:off
  private static final String TABLE_NAME = "oauth2_authorization";

  private static final String REMOVE_PREVIOUS_AUTHORIZATION_SQL =
      "DELETE FROM " + TABLE_NAME + " WHERE registered_client_id = ? AND principal_name = ? AND user_allow_duplicate_login = ?";

  private static final String REMOVE_AUTHORIZATION_BY_CLIENT_SQL =
      "DELETE FROM " + TABLE_NAME + " WHERE registered_client_id = ?";

  private static final String UPDATE_USER_ALLOW_DUPLICATE_LOGIN_SQL =
      "UPDATE " + TABLE_NAME + " SET user_allow_duplicate_login = ? WHERE id = ?";
  // @formatter:on

  public CustomJdbcOAuth2AuthorizationService(
      JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
    super(jdbcOperations, registeredClientRepository);
  }

  public CustomJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations,
      RegisteredClientRepository registeredClientRepository, LobHandler lobHandler) {
    super(jdbcOperations, registeredClientRepository, lobHandler);
  }

  /**
   * Limit each account to only log in once for the same client.
   */
  @Override
  public void save(OAuth2Authorization authorization) {
    String clientSource = authorization.getAttribute(AuthKey.CLIENT_SOURCE);
    if (isUserSignInToken(clientSource)) {
      removePreviousLogin(authorization);
    }
    super.save(authorization);
    if (isUserSignInToken(clientSource)) {
      setForbidDuplicateLogin(authorization.getId());
    }
  }

  @Override
  public void removeByClientId(String clientId){
    Assert.notNull(clientId, "clientId cannot be null");
    SqlParameterValue[] parameters = new SqlParameterValue[]{
        new SqlParameterValue(Types.VARCHAR, clientId)};
    PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters);
    this.getJdbcOperations().update(REMOVE_AUTHORIZATION_BY_CLIENT_SQL, pss);
  }

  private void removePreviousLogin(OAuth2Authorization authorization) {
    Assert.notNull(authorization, "authorization cannot be null");
    SqlParameterValue[] parameters = new SqlParameterValue[]{
        new SqlParameterValue(Types.VARCHAR, authorization.getRegisteredClientId()),
        new SqlParameterValue(Types.VARCHAR, authorization.getPrincipalName()),
        new SqlParameterValue(Types.BOOLEAN, false)};
    PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters);
    this.getJdbcOperations().update(REMOVE_PREVIOUS_AUTHORIZATION_SQL, pss);
  }

  private void setForbidDuplicateLogin(String id) {
    Assert.notNull(id, "authorization id cannot be null");
    SqlParameterValue[] parameters = new SqlParameterValue[]{
        new SqlParameterValue(Types.BOOLEAN, false),
        new SqlParameterValue(Types.VARCHAR, id)
    };
    PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters);
    this.getJdbcOperations().update(UPDATE_USER_ALLOW_DUPLICATE_LOGIN_SQL, pss);
  }

}
