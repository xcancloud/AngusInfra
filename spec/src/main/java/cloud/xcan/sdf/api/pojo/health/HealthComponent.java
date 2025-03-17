package cloud.xcan.sdf.api.pojo.health;

import cloud.xcan.sdf.spec.experimental.Assert;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(Include.NON_EMPTY)
public class HealthComponent implements Health {

  private final HealthStatus status;
  private final Map<String, Object> details;

  private HealthComponent(HealthComponent.Builder builder) {
    Assert.assertNotNull(builder, "Builder must not be null");
    this.status = builder.status;
    this.details = Collections.unmodifiableMap(builder.details);
  }

  HealthComponent(HealthStatus status, Map<String, Object> details) {
    this.status = status;
    this.details = details;
  }

  @Override
  public HealthStatus getStatus() {
    return this.status;
  }

  @JsonInclude(Include.NON_EMPTY)
  public Map<String, Object> getDetails() {
    return this.details;
  }

  Health withoutDetails() {
    return this.details.isEmpty() ? this : status(this.getStatus()).build();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof HealthComponent)) {
      return false;
    } else {
      HealthComponent other = (HealthComponent) obj;
      return this.status.equals(other.status) && this.details.equals(other.details);
    }
  }

  @Override
  public int hashCode() {
    int hashCode = this.status.hashCode();
    return 13 * hashCode + this.details.hashCode();
  }

  @Override
  public String toString() {
    return this.getStatus() + " " + this.getDetails();
  }

  public static HealthComponent.Builder unknown() {
    return status(HealthStatus.UNKNOWN);
  }

  public static HealthComponent.Builder up() {
    return status(HealthStatus.UP);
  }

  public static HealthComponent.Builder down(Exception ex) {
    return down().withException(ex);
  }

  public static HealthComponent.Builder down() {
    return status(HealthStatus.DOWN);
  }

  public static HealthComponent.Builder outOfService() {
    return status(HealthStatus.OUT_OF_SERVICE);
  }

  public static HealthComponent.Builder status(String statusCode) {
    return status(new HealthStatus(statusCode));
  }

  public static HealthComponent.Builder status(HealthStatus status) {
    return new HealthComponent.Builder(status);
  }

  public static class Builder {

    private HealthStatus status;
    private final Map<String, Object> details;

    public Builder() {
      this.status = HealthStatus.UNKNOWN;
      this.details = new LinkedHashMap<>();
    }

    public Builder(HealthStatus status) {
      Assert.assertNotNull(status, "HealthStatus must not be null");
      this.status = status;
      this.details = new LinkedHashMap<>();
    }

    public Builder(HealthStatus status, Map<String, ?> details) {
      Assert.assertNotNull(status, "HealthStatus must not be null");
      Assert.assertNotNull(details, "Details must not be null");
      this.status = status;
      this.details = new LinkedHashMap<>(details);
    }

    public HealthComponent.Builder withException(Throwable ex) {
      Assert.assertNotNull(ex, "Exception must not be null");
      return this.withDetail("error", ex.getClass().getName() + ": " + ex.getMessage());
    }

    public HealthComponent.Builder withDetail(String key, Object value) {
      Assert.assertNotNull(key, "Key must not be null");
      Assert.assertNotNull(value, "Value must not be null");
      this.details.put(key, value);
      return this;
    }

    public HealthComponent.Builder withDetails(Map<String, ?> details) {
      Assert.assertNotNull(details, "Details must not be null");
      this.details.putAll(details);
      return this;
    }

    public HealthComponent.Builder unknown() {
      return this.status(HealthStatus.UNKNOWN);
    }

    public HealthComponent.Builder up() {
      return this.status(HealthStatus.UP);
    }

    public HealthComponent.Builder down(Throwable ex) {
      return this.down().withException(ex);
    }

    public HealthComponent.Builder down() {
      return this.status(HealthStatus.DOWN);
    }

    public HealthComponent.Builder outOfService() {
      return this.status(HealthStatus.OUT_OF_SERVICE);
    }

    public HealthComponent.Builder status(String statusCode) {
      return this.status(new HealthStatus(statusCode));
    }

    public HealthComponent.Builder status(HealthStatus status) {
      this.status = status;
      return this;
    }

    public HealthComponent build() {
      return new HealthComponent(this);
    }
  }
}
