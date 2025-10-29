package cloud.xcan.angus.cache.entry;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Cache Entry Entity for JPA persistence Stores cache key-value pairs with expiration information
 */
@Entity
@Table(name = "cache_entries", indexes = {
    @Index(name = "idx_cache_key", columnList = "cache_key", unique = true),
    @Index(name = "idx_expire_time", columnList = "expire_at")
})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CacheEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cache_key", nullable = false, unique = true, length = 256)
  private String key;

  @Column(name = "cache_value", columnDefinition = "LONGTEXT")
  private String value;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Column(name = "expire_at")
  private LocalDateTime expireAt;

  @Column(name = "ttl_seconds")
  private Long ttlSeconds;

  @Column(name = "is_expired", nullable = false)
  private Boolean isExpired = false;

  /**
   * Check if cache entry is expired either by flag or by expireAt timestamp
   */
  public boolean hasExpired() {
    if (Boolean.TRUE.equals(isExpired)) {
      return true;
    }
    if (expireAt == null) {
      return false;
    }
    return LocalDateTime.now().isAfter(expireAt);
  }

  /**
   * Set TTL in seconds and calculate expiration time
   */
  public void setTTL(long seconds) {
    this.ttlSeconds = seconds;
    this.expireAt = LocalDateTime.now().plusSeconds(seconds);
  }
}
