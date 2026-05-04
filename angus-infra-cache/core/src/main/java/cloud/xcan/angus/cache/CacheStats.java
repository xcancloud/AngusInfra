package cloud.xcan.angus.cache;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * Cache Statistics Information
 * <p>
 * Provides basic metrics about the cache, including counts for entries and hit/miss statistics.
 */
@Data
@Builder
@Schema(description = "Cache runtime statistics summary")
public class CacheStats {

  @Schema(description = "Total number of entries stored in the persistent store (database)")
  private long totalEntries;

  @Schema(description = "Number of entries that have expired in the persistent store")
  private long expiredEntries;

  @Schema(description = "Number of active (non-expired) entries in the persistent store")
  private long activeEntries;

  @Schema(description = "Approximate number of entries currently held in memory cache")
  private long memorySize;

  @Schema(description = "Size of the persistent storage (usually same as totalEntries)")
  private long databaseSize;

  @Schema(description = "Number of cache hits served from memory or persistence")
  private long hits;

  @Schema(description = "Number of cache misses")
  private long misses;

  @Schema(description = "Hit rate computed as hits / (hits + misses). Value in range [0,1]")
  private double hitRate;
}
