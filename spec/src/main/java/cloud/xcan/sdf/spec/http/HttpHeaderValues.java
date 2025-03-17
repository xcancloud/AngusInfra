package cloud.xcan.sdf.spec.http;

public interface HttpHeaderValues {

  String BASE64 = "base64";
  String BINARY = "binary";
  String BOUNDARY = "boundary";
  String BYTES = "bytes";
  String CHARSET = "charset";
  String CHUNKED = "chunked";
  String CLOSE = "close";
  String COMPRESS = "compress";
  String CONTINUE = "100-continue";
  String DEFLATE = "deflate";
  String GZIP = "gzip";
  String GZIP_DEFLATE = "gzip,deflate";
  String IDENTITY = "identity";
  String KEEP_ALIVE = "keep-alive";
  String MAX_AGE = "max-age";
  String MAX_STALE = "max-stale";
  String MIN_FRESH = "min-fresh";
  String MUST_REVALIDATE = "must-revalidate";
  String NO_CACHE = "no-cache";
  String NO_STORE = "no-store";
  String NO_TRANSFORM = "no-transform";
  String NONE = "none";
  String ONLY_IF_CACHED = "only-if-cached";
  String PRIVATE = "private";
  String PROXY_REVALIDATE = "proxy-revalidate";
  String PUBLIC = "public";
  String QUOTED_PRINTABLE = "quoted-printable";
  String S_MAXAGE = "s-maxage";
  String TRAILERS = "trailers";
  String UPGRADE = "Upgrade";
  String WEBSOCKET = "WebSocket";

  // XCan Extension
  String GZIP_BASE64 = "gzip_base64";

}
