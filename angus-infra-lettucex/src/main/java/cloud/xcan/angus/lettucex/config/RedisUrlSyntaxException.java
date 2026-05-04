package cloud.xcan.angus.lettucex.config;

/**
 * EventContent thrown when a Redis URL is malformed or invalid.
 */
class RedisUrlSyntaxException extends RuntimeException {

  private final String url;

  RedisUrlSyntaxException(String url, Exception cause) {
    super(buildMessage(url), cause);
    this.url = url;
  }

  RedisUrlSyntaxException(String url) {
    super(buildMessage(url));
    this.url = url;
  }

  private static String buildMessage(String url) {
    return "Invalid Redis URL '" + url + "'";
  }

  String getUrl() {
    return this.url;
  }

}
