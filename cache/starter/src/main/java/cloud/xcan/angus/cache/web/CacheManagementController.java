package cloud.xcan.angus.cache.web;

import static cloud.xcan.angus.remote.ApiConstant.ECode.BUSINESS_ERROR_CODE;

import cloud.xcan.angus.cache.CacheStats;
import cloud.xcan.angus.cache.IDistributedCache;
import cloud.xcan.angus.remote.ApiLocaleResult;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Cache Management", description = "APIs to manage and monitor the hybrid cache")
@PreAuthorize("@PPS.isCloudTenantSecurity() && @PPS.isSysAdmin()")
@RestController
@RequestMapping(path = "/api/v1/cache", produces = MediaType.APPLICATION_JSON_VALUE)
public class CacheManagementController {

  private final IDistributedCache cache;

  /**
   * Maximum allowed length for a cache key path variable.
   */
  private static final int MAX_KEY_LENGTH = 256;

  public CacheManagementController(IDistributedCache cache) {
    this.cache = cache;
  }

  /**
   * Validates the key path variable: must be non-blank and within the allowed length. Throws
   * {@link ResponseStatusException} with 400 on violation so callers get a structured HTTP error
   * rather than a 500.
   */
  private void validateKey(String key) {
    if (key == null || key.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cache key must not be blank");
    }
    if (key.length() > MAX_KEY_LENGTH) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Cache key must not exceed " + MAX_KEY_LENGTH + " characters");
    }
  }

  @Operation(operationId = "getCacheStats", summary = "Get cache statistics", description = "Returns aggregated cache metrics such as total entries, expired entries, memory size, hits, misses and hit rate.")
  @GetMapping("/stats")
  public ApiLocaleResult<CacheStats> stats() {
    return ApiLocaleResult.success(cache.getStats());
  }

  @Operation(operationId = "getCacheValue", summary = "Get cache value by key", description = "Retrieve the value for the given cache key. Returns a business error in wrapper when the key does not exist.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Value found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CacheValueResponse.class))),
          @ApiResponse(responseCode = "200", description = "Key not found, returned as business error in wrapper")
      })
  @GetMapping("/{key}")
  public ApiLocaleResult<CacheValueResponse> get(
      @Parameter(description = "Cache key", required = true) @PathVariable("key") String key) {
    validateKey(key);
    return cache.get(key)
        .map(v -> ApiLocaleResult.success(new CacheValueResponse(key, v)))
        .orElseGet(() -> ApiLocaleResult.error(BUSINESS_ERROR_CODE, "Key not found", null));
  }

  @Operation(operationId = "setCacheValue", summary = "Set cache value for a key", description = "Set or update the value for a cache key. Provide optional ttlSeconds for expiration.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "value and optional ttlSeconds", required = true,
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = SetCacheRequest.class))))
  @PutMapping("/{key}")
  public ApiLocaleResult<?> set(
      @Parameter(description = "Cache key", required = true) @PathVariable("key") String key,
      @Valid @RequestBody SetCacheRequest body) {
    validateKey(key);
    cache.set(key, body.getValue(), body.getTtlSeconds());
    return ApiLocaleResult.success();
  }

  @Operation(operationId = "deleteCacheKey", summary = "Delete cache key", description = "Delete a cache entry by key. If the key does not exist, the operation is idempotent and returns success in wrapper.",
      responses = {
          @ApiResponse(responseCode = "200", description = "Deleted or not present (wrapped)")})
  @DeleteMapping("/{key}")
  public ApiLocaleResult<?> delete(
      @Parameter(description = "Cache key", required = true) @PathVariable("key") String key) {
    validateKey(key);
    cache.delete(key);
    return ApiLocaleResult.success();
  }

  @Operation(operationId = "existsCacheKey", summary = "Check if cache key exists", description = "Return whether the given cache key exists and is not expired.")
  @GetMapping("/{key}/exists")
  public ApiLocaleResult<ExistsResponse> exists(@PathVariable("key") String key) {
    validateKey(key);
    return ApiLocaleResult.success(new ExistsResponse(key, cache.exists(key)));
  }

  @Operation(operationId = "getCacheTTL", summary = "Get TTL for a cache key", description = "Return TTL (seconds) for a key: -1 = no expiration, -2 = not found.",
      responses = {
          @ApiResponse(responseCode = "200", description = "TTL returned in wrapper")})
  @GetMapping("/{key}/ttl")
  public ApiLocaleResult<TTLResponse> ttl(@PathVariable("key") String key) {
    validateKey(key);
    return ApiLocaleResult.success(new TTLResponse(key, cache.getTTL(key)));
  }

  @Operation(operationId = "expireCacheKey", summary = "Set expiration (TTL) for an existing key", description = "Set a new TTL (in seconds) for an existing cache key.",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "ttlSeconds body", required = true,
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = ExpireRequest.class))))
  @PostMapping("/{key}/expire")
  public ApiLocaleResult<ExpireResponse> expire(@PathVariable("key") String key,
      @Valid @RequestBody ExpireRequest body) {
    validateKey(key);
    boolean ok = cache.expire(key, body.getTtlSeconds());
    if (!ok) {
      return ApiLocaleResult.error(BUSINESS_ERROR_CODE, "Key not found", null);
    }
    return ApiLocaleResult.success(new ExpireResponse(key, true));
  }

  @Operation(operationId = "clearCache", summary = "Clear all cache entries", description = "Clear both in-memory and persistent cache entries.")
  @PostMapping("/clear")
  public ApiLocaleResult<?> clear() {
    cache.clear();
    return ApiLocaleResult.success();
  }

  @Operation(operationId = "cleanupExpiredEntries", summary = "Cleanup expired entries from persistence", description = "Delete expired entries from the persistent store and return number deleted.")
  @PostMapping("/cleanup")
  public ApiLocaleResult<CleanupResponse> cleanup() {
    int deleted = cache.cleanupExpiredEntries();
    return ApiLocaleResult.success(new CleanupResponse(deleted));
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
