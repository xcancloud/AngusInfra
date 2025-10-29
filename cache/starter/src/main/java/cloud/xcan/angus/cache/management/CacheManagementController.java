package cloud.xcan.angus.cache.management;

import cloud.xcan.angus.cache.CacheStats;
import cloud.xcan.angus.cache.IDistributedCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cache Management", description = "APIs to manage and monitor the hybrid cache")
@RestController
@RequestMapping(path = "/api/cache", produces = MediaType.APPLICATION_JSON_VALUE)
public class CacheManagementController {

  private final IDistributedCache cache;

  public CacheManagementController(IDistributedCache cache) {
    this.cache = cache;
  }

  @Operation(operationId = "getCacheStats", summary = "Get cache statistics", description = "Returns aggregated cache metrics such as total entries, expired entries, memory size, hits, misses and hit rate.")
  @GetMapping("/stats")
  public RestfulApiResult<CacheStats> stats() {
    CacheStats stats = cache.getStats();
    return RestfulApiResult.success(stats);
  }

  @Operation(operationId = "getCacheValue", summary = "Get cache value by key", description = "Retrieve the value for the given cache key. Returns a business error in wrapper when the key does not exist.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Value found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CacheValueResponse.class))),
          @ApiResponse(responseCode = "200", description = "Key not found, returned as business error in wrapper")
      })
  @GetMapping("/{key}")
  public RestfulApiResult<CacheValueResponse> get(
      @Parameter(description = "Cache key", required = true) @PathVariable("key") String key) {
    return cache.get(key)
        .map(v -> RestfulApiResult.success(new CacheValueResponse(key, v)))
        .orElseGet(() -> RestfulApiResult.error(RestfulApiResult.BUSINESS_ERROR_CODE, "Key not found"));
  }

  @Operation(operationId = "setCacheValue", summary = "Set cache value for a key", description = "Set or update the value for a cache key. Provide optional ttlSeconds for expiration.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "value and optional ttlSeconds", required = true,
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = SetCacheRequest.class))))
  @PutMapping("/{key}")
  public RestfulApiResult<?> set(
      @Parameter(description = "Cache key", required = true) @PathVariable("key") String key,
      @Valid @RequestBody SetCacheRequest body) {
    cache.set(key, body.getValue(), body.getTtlSeconds());
    return RestfulApiResult.success();
  }

  @Operation(operationId = "deleteCacheKey", summary = "Delete cache key", description = "Delete a cache entry by key. If the key does not exist, the operation is idempotent and returns success in wrapper.",
      responses = {@ApiResponse(responseCode = "200", description = "Deleted or not present (wrapped)")})
  @DeleteMapping("/{key}")
  public RestfulApiResult<?> delete(
      @Parameter(description = "Cache key", required = true) @PathVariable("key") String key) {
    cache.delete(key);
    return RestfulApiResult.success();
  }

  @Operation(operationId = "existsCacheKey", summary = "Check if cache key exists", description = "Return whether the given cache key exists and is not expired.")
  @GetMapping("/{key}/exists")
  public RestfulApiResult<ExistsResponse> exists(@PathVariable("key") String key) {
    boolean e = cache.exists(key);
    return RestfulApiResult.success(new ExistsResponse(key, e));
  }

  @Operation(operationId = "getCacheTTL", summary = "Get TTL for a cache key", description = "Return TTL (seconds) for a key: -1 = no expiration, -2 = not found.",
      responses = {
          @ApiResponse(responseCode = "200", description = "TTL returned in wrapper")})
  @GetMapping("/{key}/ttl")
  public RestfulApiResult<TTLResponse> ttl(@PathVariable("key") String key) {
    long ttl = cache.getTTL(key);
    return RestfulApiResult.success(new TTLResponse(key, ttl));
  }

  @Operation(operationId = "expireCacheKey", summary = "Set expiration (TTL) for an existing key", description = "Set a new TTL (in seconds) for an existing cache key.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ttlSeconds body", required = true,
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExpireRequest.class))))
  @PostMapping("/{key}/expire")
  public RestfulApiResult<ExpireResponse> expire(@PathVariable("key") String key,
      @Valid @RequestBody ExpireRequest body) {
    boolean ok = cache.expire(key, body.getTtlSeconds());
    if (!ok) {
      return RestfulApiResult.error(RestfulApiResult.BUSINESS_ERROR_CODE, "Key not found");
    }
    return RestfulApiResult.success(new ExpireResponse(key, true));
  }

  @Operation(operationId = "clearCache", summary = "Clear all cache entries", description = "Clear both in-memory and persistent cache entries.")
  @PostMapping("/clear")
  public RestfulApiResult<?> clear() {
    cache.clear();
    return RestfulApiResult.success();
  }

  @Operation(operationId = "cleanupExpiredEntries", summary = "Cleanup expired entries from persistence", description = "Delete expired entries from the persistent store and return number deleted.")
  @PostMapping("/cleanup")
  public RestfulApiResult<CleanupResponse> cleanup() {
    int deleted = cache.cleanupExpiredEntries();
    return RestfulApiResult.success(new CleanupResponse(deleted));
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class SetCacheRequest {

    @NotBlank
    @Schema(description = "Value to store")
    private String value;

    @Schema(description = "TTL in seconds (optional). If null, no expiration")
    private Long ttlSeconds;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExpireRequest {

    @Schema(description = "TTL in seconds")
    private long ttlSeconds;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CacheValueResponse {

    @Schema(description = "Cache key")
    private String key;
    @Schema(description = "Cache value")
    private String value;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExistsResponse {

    private String key;
    private boolean exists;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TTLResponse {

    private String key;
    private long ttl;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExpireResponse {

    private String key;
    private boolean success;
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CleanupResponse {

    private int deletedCount;
  }
}
