package cloud.xcan.angus.cache;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cache entry info DTO (without value, for listing purposes)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheEntryInfo {

  private String key;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private LocalDateTime expireAt;
  private Long ttlSeconds;
}
