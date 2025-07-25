package cloud.xcan.angus.api.pojo.health;

import cloud.xcan.angus.spec.experimental.Assert;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * Represents a health check component that provides status information about
 * application components and their operational state. This class implements
 * the Health interface and provides a comprehensive health reporting mechanism.
 * </p>
 * 
 * <p>
 * Key features:
 * - Immutable health status representation
 * - Detailed health information with custom attributes
 * - Builder pattern for flexible health component construction
 * - JSON serialization support with selective field inclusion
 * - Support for health status hierarchies and aggregation
 * </p>
 * 
 * <p>
 * Health Status Levels:
 * - UP: Component is functioning normally
 * - DOWN: Component is not functioning (with optional exception details)
 * - OUT_OF_SERVICE: Component is temporarily unavailable
 * - UNKNOWN: Component status cannot be determined
 * </p>
 * 
 * <p>
 * Usage examples:
 * <pre>
 * // Simple health status
 * Health health = HealthComponent.up().build();
 * 
 * // Health with details
 * Health dbHealth = HealthComponent.up()
 *     .withDetail("database", "PostgreSQL")
 *     .withDetail("connectionPool", "HikariCP")
 *     .withDetail("activeConnections", 5)
 *     .build();
 * 
 * // Health with exception
 * Health errorHealth = HealthComponent.down(new SQLException("Connection failed"))
 *     .withDetail("url", "jdbc:postgresql://localhost/db")
 *     .build();
 * </pre>
 * </p>
 * 
 * <p>
 * Thread Safety: This class is immutable and thread-safe once constructed.
 * The builder is not thread-safe and should not be shared across threads.
 * </p>
 * 
 * @see Health
 * @see HealthStatus
 */
@JsonInclude(Include.NON_EMPTY)
public class HealthComponent implements Health {

  /**
   * The current status of this health component.
   * This field is never null and represents the overall health state.
   */
  private final HealthStatus status;

  /**
   * Additional details about the health component.
   * This map contains key-value pairs providing context and diagnostic information.
   * The map is immutable to ensure thread safety.
   */
  private final Map<String, Object> details;

  /**
   * <p>
   * Private constructor used by the Builder to create immutable HealthComponent instances.
   * This ensures that all HealthComponent instances are properly validated and immutable.
   * </p>
   *
   * @param builder the builder containing the health component configuration
   * @throws IllegalArgumentException if the builder is null
   */
  private HealthComponent(Builder builder) {
    Assert.assertNotNull(builder, "Builder must not be null");
    this.status = builder.status;
    this.details = Collections.unmodifiableMap(builder.details);
  }

  /**
   * <p>
   * Package-private constructor for internal use.
   * Used for creating HealthComponent instances with pre-validated parameters.
   * </p>
   *
   * @param status the health status
   * @param details the health details map
   */
  HealthComponent(HealthStatus status, Map<String, Object> details) {
    this.status = status;
    this.details = details;
  }

  /**
   * <p>
   * Returns the current health status of this component.
   * </p>
   *
   * @return the health status, never null
   */
  @Override
  public HealthStatus getStatus() {
    return this.status;
  }

  /**
   * <p>
   * Returns the details map containing additional health information.
   * The returned map is immutable and contains key-value pairs with
   * diagnostic and contextual information about the component's health.
   * </p>
   *
   * @return an immutable map of health details, never null
   */
  @JsonInclude(Include.NON_EMPTY)
  public Map<String, Object> getDetails() {
    return this.details;
  }

  /**
   * <p>
   * Creates a new HealthComponent without details.
   * If this component already has no details, returns the same instance.
   * Otherwise, creates a new instance with the same status but no details.
   * </p>
   *
   * @return a Health instance without details
   */
  Health withoutDetails() {
    return this.details.isEmpty() ? this : status(this.getStatus()).build();
  }

  /**
   * <p>
   * Compares this HealthComponent with another object for equality.
   * Two HealthComponents are equal if they have the same status and details.
   * </p>
   *
   * @param obj the object to compare with
   * @return true if the objects are equal, false otherwise
   */
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

  /**
   * <p>
   * Returns the hash code for this HealthComponent.
   * The hash code is computed based on the status and details.
   * </p>
   *
   * @return the hash code value
   */
  @Override
  public int hashCode() {
    int hashCode = this.status.hashCode();
    return 13 * hashCode + this.details.hashCode();
  }

  /**
   * <p>
   * Returns a string representation of this HealthComponent.
   * The format includes both the status and details for debugging purposes.
   * </p>
   *
   * @return a string representation of this health component
   */
  @Override
  public String toString() {
    return this.getStatus() + " " + this.getDetails();
  }

  /* ==================== Static Factory Methods ==================== */

  /**
   * <p>
   * Creates a builder for a health component with UNKNOWN status.
   * Use this when the component's health status cannot be determined.
   * </p>
   *
   * @return a new Builder with UNKNOWN status
   */
  public static Builder unknown() {
    return status(HealthStatus.UNKNOWN);
  }

  /**
   * <p>
   * Creates a builder for a health component with UP status.
   * Use this when the component is functioning normally.
   * </p>
   *
   * @return a new Builder with UP status
   */
  public static Builder up() {
    return status(HealthStatus.UP);
  }

  /**
   * <p>
   * Creates a builder for a health component with DOWN status and exception details.
   * Use this when the component has failed and you want to include exception information.
   * </p>
   *
   * @param ex the exception that caused the component to be down
   * @return a new Builder with DOWN status and exception details
   */
  public static Builder down(Exception ex) {
    return down().withException(ex);
  }

  /**
   * <p>
   * Creates a builder for a health component with DOWN status.
   * Use this when the component is not functioning properly.
   * </p>
   *
   * @return a new Builder with DOWN status
   */
  public static Builder down() {
    return status(HealthStatus.DOWN);
  }

  /**
   * <p>
   * Creates a builder for a health component with OUT_OF_SERVICE status.
   * Use this when the component is temporarily unavailable but not necessarily broken.
   * </p>
   *
   * @return a new Builder with OUT_OF_SERVICE status
   */
  public static Builder outOfService() {
    return status(HealthStatus.OUT_OF_SERVICE);
  }

  /**
   * <p>
   * Creates a builder for a health component with a custom status code.
   * </p>
   *
   * @param statusCode the custom status code
   * @return a new Builder with the specified status
   */
  public static Builder status(String statusCode) {
    return status(new HealthStatus(statusCode));
  }

  /**
   * <p>
   * Creates a builder for a health component with the specified status.
   * </p>
   *
   * @param status the health status to use
   * @return a new Builder with the specified status
   */
  public static Builder status(HealthStatus status) {
    return new Builder(status);
  }

  /**
   * <p>
   * Builder class for constructing HealthComponent instances using the builder pattern.
   * This class provides a fluent API for setting health status and adding details.
   * </p>
   * 
   * <p>
   * The builder allows method chaining for convenient health component construction:
   * <pre>
   * HealthComponent health = HealthComponent.up()
   *     .withDetail("version", "1.0.0")
   *     .withDetail("uptime", "5 days")
   *     .build();
   * </pre>
   * </p>
   * 
   * <p>
   * Thread Safety: This builder is not thread-safe and should not be shared
   * across multiple threads without external synchronization.
   * </p>
   */
  public static class Builder {

    /**
     * The health status for the component being built.
     */
    private HealthStatus status;

    /**
     * The mutable details map used during construction.
     * This will be made immutable when the HealthComponent is built.
     */
    private final Map<String, Object> details;

    /**
     * <p>
     * Creates a new builder with UNKNOWN status and empty details.
     * </p>
     */
    public Builder() {
      this.status = HealthStatus.UNKNOWN;
      this.details = new LinkedHashMap<>();
    }

    /**
     * <p>
     * Creates a new builder with the specified status and empty details.
     * </p>
     *
     * @param status the initial health status
     * @throws IllegalArgumentException if status is null
     */
    public Builder(HealthStatus status) {
      Assert.assertNotNull(status, "HealthStatus must not be null");
      this.status = status;
      this.details = new LinkedHashMap<>();
    }

    /**
     * <p>
     * Creates a new builder with the specified status and details.
     * The details map is copied to prevent external modifications.
     * </p>
     *
     * @param status the initial health status
     * @param details the initial health details
     * @throws IllegalArgumentException if status or details is null
     */
    public Builder(HealthStatus status, Map<String, ?> details) {
      Assert.assertNotNull(status, "HealthStatus must not be null");
      Assert.assertNotNull(details, "Details must not be null");
      this.status = status;
      this.details = new LinkedHashMap<>(details);
    }

    /**
     * <p>
     * Adds exception information to the health details.
     * The exception class name and message are stored under the "error" key.
     * </p>
     *
     * @param ex the exception to include in health details
     * @return this builder for method chaining
     * @throws IllegalArgumentException if the exception is null
     */
    public Builder withException(Throwable ex) {
      Assert.assertNotNull(ex, "Exception must not be null");
      return this.withDetail("error", ex.getClass().getName() + ": " + ex.getMessage());
    }

    /**
     * <p>
     * Adds a single detail key-value pair to the health information.
     * </p>
     *
     * @param key the detail key
     * @param value the detail value
     * @return this builder for method chaining
     * @throws IllegalArgumentException if key or value is null
     */
    public Builder withDetail(String key, Object value) {
      Assert.assertNotNull(key, "Key must not be null");
      Assert.assertNotNull(value, "Value must not be null");
      this.details.put(key, value);
      return this;
    }

    /**
     * <p>
     * Adds multiple details from the provided map to the health information.
     * All entries from the provided map are copied to the builder's details.
     * </p>
     *
     * @param details the map of details to add
     * @return this builder for method chaining
     * @throws IllegalArgumentException if details map is null
     */
    public Builder withDetails(Map<String, ?> details) {
      Assert.assertNotNull(details, "Details must not be null");
      this.details.putAll(details);
      return this;
    }

    /* ==================== Status Setting Methods ==================== */

    /**
     * <p>
     * Sets the health status to UNKNOWN.
     * </p>
     *
     * @return this builder for method chaining
     */
    public Builder unknown() {
      return this.status(HealthStatus.UNKNOWN);
    }

    /**
     * <p>
     * Sets the health status to UP.
     * </p>
     *
     * @return this builder for method chaining
     */
    public Builder up() {
      return this.status(HealthStatus.UP);
    }

    /**
     * <p>
     * Sets the health status to DOWN and includes exception details.
     * </p>
     *
     * @param ex the exception that caused the component to be down
     * @return this builder for method chaining
     */
    public Builder down(Throwable ex) {
      return this.down().withException(ex);
    }

    /**
     * <p>
     * Sets the health status to DOWN.
     * </p>
     *
     * @return this builder for method chaining
     */
    public Builder down() {
      return this.status(HealthStatus.DOWN);
    }

    /**
     * <p>
     * Sets the health status to OUT_OF_SERVICE.
     * </p>
     *
     * @return this builder for method chaining
     */
    public Builder outOfService() {
      return this.status(HealthStatus.OUT_OF_SERVICE);
    }

    /**
     * <p>
     * Sets the health status using a custom status code.
     * </p>
     *
     * @param statusCode the custom status code
     * @return this builder for method chaining
     */
    public Builder status(String statusCode) {
      return this.status(new HealthStatus(statusCode));
    }

    /**
     * <p>
     * Sets the health status.
     * </p>
     *
     * @param status the health status to set
     * @return this builder for method chaining
     */
    public Builder status(HealthStatus status) {
      this.status = status;
      return this;
    }

    /**
     * <p>
     * Builds and returns an immutable HealthComponent instance
     * with the configured status and details.
     * </p>
     *
     * @return a new immutable HealthComponent
     */
    public HealthComponent build() {
      return new HealthComponent(this);
    }
  }
}
