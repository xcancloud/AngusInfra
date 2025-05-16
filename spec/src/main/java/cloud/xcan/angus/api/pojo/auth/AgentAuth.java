package cloud.xcan.angus.api.pojo.auth;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class AgentAuth {

  private String clientId;

  private String clientSecret;

  private String accessToken;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AgentAuth agentAuth)) {
      return false;
    }
    return Objects.equals(clientId, agentAuth.clientId) &&
        Objects.equals(clientSecret, agentAuth.clientSecret) &&
        Objects.equals(accessToken, agentAuth.accessToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clientId, clientSecret, accessToken);
  }
}
