package cloud.xcan.angus.security.remote.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ClientSignVo {

  @JsonProperty("access_token")
  private String accessToken;

  @JsonProperty("expires_in")
  private int expiration;

  @JsonProperty("token_type")
  private String tokenType;

  private Set<String> scope;

}
