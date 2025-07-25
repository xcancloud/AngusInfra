package cloud.xcan.angus.lettucex.config;

/**
 * <p>
 * Exception thrown when a Redis URL is malformed, invalid, or cannot be parsed.
 * This exception provides detailed information about URL syntax errors to help
 * with debugging Redis connection configuration issues.
 * </p>
 * 
 * <p>
 * Common scenarios that trigger this exception:
 * - Invalid URL scheme (must be redis:// or rediss:// for SSL)
 * - Malformed host or port specifications
 * - Invalid characters in URL components
 * - Missing required URL components
 * - Unsupported URL parameters or formats
 * </p>
 * 
 * <p>
 * Usage example:
 * <pre>
 * try {
 *     RedisConnectionFactory factory = createConnection(redisUrl);
 * } catch (RedisUrlSyntaxException e) {
 *     log.error("Invalid Redis URL: {}", e.getUrl(), e);
 *     // Handle configuration error
 * }
 * </pre>
 * </p>
 * 
 * <p>
 * Valid Redis URL formats:
 * - redis://localhost:6379
 * - redis://user:password@localhost:6379/0
 * - rediss://localhost:6380 (SSL)
 * - redis://localhost:6379?timeout=2000
 * </p>
 * 
 * @see RuntimeException
 */
class RedisUrlSyntaxException extends RuntimeException {

  /**
   * The malformed Redis URL that caused this exception.
   * This field is preserved for diagnostic and logging purposes.
   */
  private final String url;

  /**
   * <p>
   * Constructs a new RedisUrlSyntaxException with the specified URL and underlying cause.
   * This constructor is used when the URL parsing fails due to an underlying exception.
   * </p>
   *
   * @param url the malformed Redis URL that caused the exception
   * @param cause the underlying exception that caused the URL parsing to fail
   */
  RedisUrlSyntaxException(String url, Exception cause) {
    super(buildMessage(url), cause);
    this.url = url;
  }

  /**
   * <p>
   * Constructs a new RedisUrlSyntaxException with the specified URL.
   * This constructor is used when the URL is syntactically invalid
   * without an underlying exception.
   * </p>
   *
   * @param url the malformed Redis URL that caused the exception
   */
  RedisUrlSyntaxException(String url) {
    super(buildMessage(url));
    this.url = url;
  }

  /**
   * <p>
   * Builds a descriptive error message for the malformed Redis URL.
   * The message includes the problematic URL to aid in troubleshooting.
   * </p>
   *
   * @param url the malformed Redis URL
   * @return a descriptive error message
   */
  private static String buildMessage(String url) {
    return "Invalid Redis URL '" + url + "'. Expected format: redis://[user:password@]host:port[/database][?options]";
  }

  /**
   * <p>
   * Returns the malformed Redis URL that caused this exception.
   * This method provides access to the problematic URL for logging
   * and diagnostic purposes.
   * </p>
   *
   * @return the malformed Redis URL, never null
   */
  String getUrl() {
    return this.url;
  }
}
