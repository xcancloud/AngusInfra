package cloud.xcan.sdf.api.pojo.health;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafeEquals;

import cloud.xcan.sdf.spec.experimental.Assert;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_EMPTY)
public final class HealthStatus {

  public static final HealthStatus UNKNOWN = new HealthStatus("UNKNOWN");
  public static final HealthStatus UP = new HealthStatus("UP");
  public static final HealthStatus DOWN = new HealthStatus("DOWN");
  public static final HealthStatus OUT_OF_SERVICE = new HealthStatus("OUT_OF_SERVICE");
  private final String code;
  private final String description;

  public HealthStatus(String code) {
    this(code, "");
  }

  public HealthStatus(String code, String description) {
    Assert.assertNotNull(code, "Code must not be null");
    Assert.assertNotNull(description, "Description must not be null");
    this.code = code;
    this.description = description;
  }

  @JsonProperty("status")
  public String getCode() {
    return this.code;
  }

  @JsonInclude(Include.NON_EMPTY)
  public String getDescription() {
    return this.description;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else {
      return obj instanceof HealthStatus && nullSafeEquals(this.code, ((HealthStatus) obj).code);
    }
  }

  @Override
  public int hashCode() {
    return this.code.hashCode();
  }

  @Override
  public String toString() {
    return this.code;
  }
}
