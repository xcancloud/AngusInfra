package cloud.xcan.angus.security.authentication;

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

public class CustomJdbcOAuth2AuthorizationService extends JdbcOAuth2AuthorizationService {

  private static final String TABLE_NAME = "oauth2_authorization";

  private static final String REMOVE_PREVIOUS_AUTHORIZATION_SQL =
      "DELETE FROM " + TABLE_NAME + " WHERE registered_client_id = ? AND principal_name = ?";

  public CustomJdbcOAuth2AuthorizationService(
      JdbcOperations jdbcOperations, RegisteredClientRepository registeredClientRepository) {
    super(jdbcOperations, registeredClientRepository);
  }

  public CustomJdbcOAuth2AuthorizationService(JdbcOperations jdbcOperations,
      RegisteredClientRepository registeredClientRepository, LobHandler lobHandler) {
    super(jdbcOperations, registeredClientRepository, lobHandler);
  }

  @Override
  public void save(OAuth2Authorization authorization) {
    // Limit each account to only log in once for the same client.
    removePrevious(authorization);
    super.save(authorization);
  }

  private void removePrevious(OAuth2Authorization authorization) {
    Assert.notNull(authorization, "authorization cannot be null");
    SqlParameterValue[] parameters = new SqlParameterValue[]{
        new SqlParameterValue(Types.VARCHAR, authorization.getRegisteredClientId()),
        new SqlParameterValue(Types.VARCHAR, authorization.getPrincipalName())};
    PreparedStatementSetter pss = new ArgumentPreparedStatementSetter(parameters);
    this.getJdbcOperations().update(REMOVE_PREVIOUS_AUTHORIZATION_SQL, pss);
  }

}
