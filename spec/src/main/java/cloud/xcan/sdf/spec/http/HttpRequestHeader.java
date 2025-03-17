package cloud.xcan.sdf.spec.http;

import cloud.xcan.sdf.spec.SpecConstant;
import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;
import cloud.xcan.sdf.spec.locale.MessageHolder;
import cloud.xcan.sdf.spec.locale.SdfLocaleHolder;
import org.apache.commons.lang3.StringUtils;

/**
 * Standard Http request fields.
 *
 * @see <a href="https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Request_fields">List of
 * HTTP header fields</a>
 */
@EndpointRegister
public enum HttpRequestHeader implements EnumMessage<String> {

  // @formatter:off
  /**
   * A-IM: feed
   */
  A_IM("A-IM", "Acceptable instance-manipulations for the request"),
  /**
   * Accept: text/html
   */
  Accept("Accept", "Media type(s) that is/are acceptable for the response"),
  /**
   * Accept-Charset: utf-8
   */
  Accept_Charset("Accept-Charset", "Character sets that are acceptable"),
  /**
   * Accept-Datetime: Thu, 31 May 2007 20:35:00 GMT
   */
  Accept_Datetime("Accept-Datetime", "Indicate to the server the date/time format and range that the client is willing to accept"),
  /**
   * Accept-Encoding: gzip, deflate
   */
  Accept_Encoding("Accept-Encoding", "List of acceptable encodings"),
  /**
   * Accept-Language: en-US
   */
  Accept_Language("Accept-Language", "List of acceptable human languages for response"),
  /**
   * Access-Control-Request-Headers: authorization, content-type
   */
  Access_Control_Request_Method("Access-Control-Request-Method", "Initiates a request for cross-origin resource sharing with Origin"),
  /**
   * Access-Control-Request-Headers: authorization, content-type
   */
  Access_Control_Request_Headers("Access-Control-Request-Headers", "Initiates a request for cross-origin resource sharing with Origin"),
  /**
   * Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
   */
  Authorization("Authorization", "Authentication credentials for HTTP authentication"),
  /**
   * Cache-Control: no-cache
   */
  Cache_Control("Cache-Control", "Used to specify directives that must be obeyed by all caching mechanisms along the request-response chain"),
  /**
   * Connection: keep-alive
   * <p>
   * Connection: Upgrade
   */
  Connection("Connection", "Control options for the current connection and list of hop-by-hop request fields"),
  /**
   * Content-Encoding: gzip
   */
  Content_Encoding("Content-Encoding", "The type of encoding used on the data. See HTTP compression"),
  /**
   * Content-Length: 348
   */
  Content_Length("Content-Length", "The length of the request body in octets (8-bit bytes)"),
  /**
   * Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
   */
  Content_MD5("Content-MD5", "A Base64-encoded binary MD5 sum of the content of the request body"),
  /**
   * Content-Type: application/x-www-form-urlencoded
   */
  Content_Type("Content-Type", "The Media type of the body of the request (used with POST and PUT requests)"),
  /**
   * Cookie: $Version=1; Skin=new;
   */
  Cookie("Cookie", "An HTTP cookie previously sent by the server with Set-Cookie (below)"),
  /**
   * Date: Tue, 15 Nov 1994 08:12:31 GMT
   */
  Date("Date", "The date and time at which the message was originated"),
  /**
   * Expect: 100-continue
   */
  Expect("Expect", "Indicates that particular server behaviors are required by the client"),
  /**
   * Forwarded: for=192.0.2.60;proto=http;by=203.0.113.43 Forwarded: for=192.0.2.43,
   * for=198.51.100.17
   */
  Forwarded("Forwarded", "Disclose original information of a client connecting to a web server through an HTTP proxy"),
  /**
   * From: user@example.com
   */
  From("From", "The email address of the user making the request"),
  /**
   * Host: en.wikipedia.org:8080
   * <p>
   * Host: en.wikipedia.org
   */
  Host("Host", "The domain name of the server (for virtual hosting), and the TCP port (may be omitted) number on which the server is listening"),
  //  /**
  //   * HTTP2-Settings: token64
  //   */
  //  HTTP2_Settings("HTTP2-Settings", "A request that upgrades from HTTP/1.1 to HTTP/2 MUST include exactly one HTTP2-Setting header field"),
  /**
   * If-Match: "737060cd8c284d8af7ad3082f209582d"
   */
  If_Match("If-Match", "Only perform the action if the client supplied entity matches the same entity on the server"),
  /**
   * If-Modified-Since: Sat, 29 Oct 1994 19:43:31 GMT
   */
  If_Modified_Since("If-Modified-Since", "Allows a 304 Not Modified to be returned if content is unchanged"),
  /**
   * If-None-Match: "737060cd8c284d8af7ad3082f209582d"
   */
  If_None_Match("If-None-Match", "Allows a 304 Not Modified to be returned if content is unchanged, see HTTP ETag"),
  /**
   * If-Range: "737060cd8c284d8af7ad3082f209582d"
   */
  If_Range("If-Range", "If the entity is unchanged, send me the part(s) that I am missing; otherwise, send me the entire new entity"),
  /**
   * If-Unmodified-Since: Sat, 29 Oct 1994 19:43:31 GMT
   */
  If_Unmodified_Since("If-Unmodified-Since", "Only send the response if the entity has not been modified since a specific time"),
  /**
   * Max-Forwards: 10
   */
  Max_Forwards("Max-Forwards", "Limit the number of times the message can be forwarded through proxies or gateways"),
  /**
   * Origin: http://www.example-social-network.com
   */
  Origin("Origin", "Initiates a request for cross-origin resource sharing (asks server for Access-Control-* response fields)"),
  /**
   * Pragma: no-cache
   */
  Pragma("Pragma", "Implementation-specific fields that may have various effects anywhere along the request-response chain"),
  /**
   * Prefer: return=representation
   */
  Prefer("Prefer", "Allows client to request that certain behaviors be employed by a server while processing a request"),
  /**
   * Proxy-Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
   */
  Proxy_Authorization("Proxy-Authorization", "Authorization credentials for connecting to a proxy"),
  /**
   * Range: bytes=500-999
   */
  Range("Range", "Request only part of an entity. Bytes are numbered from 0"),
  /**
   * Referer: http://en.wikipedia.org/wiki/Main_Page
   */
  Referer("Referer", "This is the address of the previous web page from which a link to the currently requested page was followed"),
  /**
   * TE: trailers, deflate
   */
  TE("TE", "The transfer encodings the user agent is willing to accept"),
  /**
   * Trailer: Max-Forwards
   */
  Trailer("Trailer", "The Trailer general field value indicates that the given set of header fields is present in the trailer of a message encoded with chunked transfer coding"),
  /**
   * Transfer-Encoding: chunked
   */
  Transfer_Encoding("Transfer-Encoding", "The form of encoding used to safely transfer the entity to the user"),
  /**
   * User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:12.0) Gecko/20100101 Firefox/12.0
   */
  User_Agent("User-Agent", "The user agent string of the user agent"),
  /**
   * Upgrade: h2c, HTTPS/1.3, IRC/6.9, RTA/x11, websocket
   */
  Upgrade("Upgrade", "Ask the server to upgrade to another protocol"),
  /**
   * Via: 1.0 fred, 1.1 example.com (Apache/1.1)
   */
  Via("Via", "Informs the server of proxies through which the request was sent"),
  /**
   * Warning: 199 Miscellaneous warning
   */
  Warning("Warning","A general warning about possible problems with the entity body");
  // @formatter:on

  public final String value;

  public final String reasonPhrase;

  HttpRequestHeader(String value, String reasonPhrase) {
    this.value = value;
    this.reasonPhrase = reasonPhrase;
  }

  @Override
  public String getValue() {
    return value;
  }

  public String getReasonPhrase() {
    return reasonPhrase;
  }

  @Override
  public String getMessage() {
    if (SpecConstant.DEFAULT_LOCALE.equals(SdfLocaleHolder.getLocale())) {
      String message = MessageHolder.message(getMessageKey());
      return StringUtils.isBlank(message) ? this.getValue() : message;
    } else {
      return getReasonPhrase();
    }
  }

  @Override
  public String getMessageKey() {
    return getKeyPrefix() + this.getClass().getSimpleName() + "." + this.name();
  }
}
