package cloud.xcan.angus.spec.http;

import cloud.xcan.angus.spec.SpecConstant;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;
import cloud.xcan.angus.spec.locale.MessageHolder;
import cloud.xcan.angus.spec.locale.SdfLocaleHolder;
import org.apache.commons.lang3.StringUtils;

/**
 * Standard Http response fields.
 *
 * @see <a href="https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Response_fields">List of
 * HTTP header fields</a>
 */
@EndpointRegister
public enum HttpResponseHeader implements EnumMessage<String> {

  // @formatter:off
  /**
   * Accept-CH: UA, Platform
   */
  Accept_CH("Accept-CH", "Requests HTTP Client Hints"),
  /**
   * Access-Control-Allow-Origin: *
   */
  Access_Control_Allow_Origin("Access-Control-Allow-Origin", "Specifying which web sites can participate in cross-origin resource sharing"),
  /**
   * Access-Control-Allow-Credentials:true
   */
  Access_Control_Allow_Credentials("Access-Control-Allow-Credentials", "Specifying third-party sites may be able to carry out privileged actions and retrieve sensitive information"),
  /**
   * Access-Control-Expose-Headers: Content-Encoding, Kuma-Revision
   * <p>
   * The server can respond to non-credentialed requests with a wildcard:
   * <p>
   * Access-Control-Expose-Headers: *
   * <p>
   * However, this won't match the Authorization header, so you need to explicitly specify it if you want to expose it:
   * <p>
   * Access-Control-Expose-Headers: *, Authorization
   */
  Access_Control_Expose_Headers("Access-Control-Expose-Headers", "Specifying allow access to certain header fields in the response that are not included in the default set of accessible headers, which can be accessed by the client (usually a web browser)"),
  /**
   * Access-Control-Max-Age: 60
   */
  Access_Control_Max_Age("Access-Control-Max-Age", "Indicates how long (in seconds) the response to a preflight request (OPTIONS) may be cached"),
  /**
   * Access-Control-Allow-Methods: GET, POST, PUT
   */
  Access_Control_Allow_Methods("Access-Control-Allow-Methods", "Specifying which HTTP methods are allowed"),
  /**
   * Access-Control-Allow-Headers: Content-Type, Authorization
   */
  Access_Control_Allow_Headers("Access-Control-Allow-Headers", "Specifying which HTTP request headers are allowed for the client to use"),
  /**
   * Accept-Patch: text/example;charset=utf-8
   */
  Accept_Patch("Accept-Patch", "Specifies which patch document formats this server supports"),
  /**
   * Accept-Ranges: bytes
   */
  Accept_Ranges("Accept-Ranges", "What partial content range types this server supports via byte serving"),
  /**
   * Age: 12
   */
  Age("Age", "The age the object has been in a proxy cache in seconds"),
  /**
   * Allow: GET, HEAD
   */
  Allow("Allow", "Valid methods for a specified resource"),
  /**
   * Alt-Svc: http/1.1="http2.example.com:8001"; ma=7200
   */
  Alt_Svc("Alt-Svc", "A server uses \"Alt-Svc\" header (meaning Alternative Services) to indicate that its resources can also be accessed at a different network location (host or port) or using a different protocol"),
  /**
   * Cache-Control: max-age=3600
   */
  Cache_Control("Cache-Control", "Tells all caching mechanisms from server to client whether they may cache this object. It is measured in seconds"),
  /**
   * Connection: close
   */
  Connection("Connection", "Control options for the current connection and list of hop-by-hop response fields"),
  /**
   * Content-Disposition: attachment; filename="fname.ext"
   */
  Content_Disposition("Content-Disposition", "An opportunity to raise a \"File Download\" dialogue box for a known MIME type with binary format or suggest a filename for dynamic content. Quotes are necessary with special characters"),
  /**
   * Content-Encoding: gzip
   */
  Content_Encoding("Content-Encoding", "The type of encoding used on the data. See HTTP compression"),
  /**
   * Content-Language: zh_CN
   */
  Content_Language("Content-Language", "The natural language or languages of the intended audience for the enclosed content"),
  /**
   * Content-Length: 348
   */
  Content_Length("Content-Length", "The length of the response body in octets (8-bit bytes)"),
  /**
   * Content-Location: /index.htm
   */
  Content_Location("Content-Location", "An alternate location for the returned data"),
  /**
   * Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
   */
  Content_MD5("Content-MD5", "A Base64-encoded binary MD5 sum of the content of the response"),
  /**
   * Content-Range: bytes 21010-47021/47022
   */
  Content_Range("Content-Range", "Where in a full body message this partial message belongs"),
  /**
   * Content-Type: text/html; charset=utf-8
   */
  Content_Type("Content-Type", "The MIME type of this content"),
  /**
   * Date: Tue, 15 Nov 1994 08:12:31 GMT
   */
  Date("Date", "The date and time that the message was sent"),
  /**
   * Delta-Base: "abc"
   */
  Delta_Base("Delta-Base", "Specifies the delta-encoding entity tag of the response"),
  /**
   * ETag: "737060cd8c284d8af7ad3082f209582d"
   */
  ETag("ETag", "An identifier for a specific version of a resource, often a message digest"),
  /**
   * Expires: Thu, 01 Dec 1994 16:00:00 GMT
   */
  Expires("Expires", "Gives the date/time after which the response is considered stale"),
  /**
   * IM: feed
   */
  IM("IM", "Instance-manipulations applied to the response"),
  /**
   * Last-Modified: Tue, 15 Nov 1994 12:45:26 GMT
   */
  Last_Modified("Last-Modified", "The last modified date for the requested object"),
  /**
   * Link: </feed>; rel="alternate"
   */
  Link("Link", "Used to express a typed relationship with another resource"),
  /**
   * Example 1: Location: http://www.w3.org/pub/WWW/People.html
   * <p>
   * Example 2: Location: /pub/WWW/People.html
   */
  Location("Location", "Used in redirection, or when a new resource has been created"),
  /**
   * P3P: CP="This is not a P3P policy! See https://en.wikipedia.org/wiki/Special:CentralAutoLogin/P3P for more info."
   */
  P3P("P3P", "This field is supposed to set P3P policy, in the form of P3P:CP=\"your_compact_policy\""),
  /**
   * Pragma: no-cache
   */
  Pragma("Pragma", "Implementation-specific fields that may have various effects anywhere along the request-response chain"),
  /**
   * Preference-Applied: return=representation
   */
  Preference_Applied("Preference-Applied", "Indicates which Prefer tokens were honored by the server and applied to the processing of the request"),
  /**
   *Public-Key-Pins: max-age=2592000; pin-sha256="E9CZ9INDbd+2eRQozYqqbQ2yXLVKB9+xcprMF+44U1g=";
   */
  Public_Key_Pins("Public-Key-Pins", "HTTP Public Key Pinning, announces hash of website's authentic TLS certificate"),
  /**
   * Example 1: Retry-After: 120
   * <p>
   * Example 2: Retry-After: Fri, 07 Nov 2014 23:59:59 GMT
   */
  Retry_After("Retry-After", "If an entity is temporarily unavailable, this instructs the client to try again later. Value could be a specified period of time (in seconds) or a HTTP-date"),
  /**
   * Server: Apache/2.4.1 (Unix)
   */
  Server("Server", "A name for the server"),
  /**
   * Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1
   */
  Set_Cookie("Set-Cookie", "An HTTP cookie"),
  /**
   * Strict-Transport-Security: max-age=16070400; includeSubDomains
   */
  Strict_Transport_Security("Strict-Transport-Security", "A HSTS Policy informing the HTTP client how long to cache the HTTPS only policy and whether this applies to subdomains"),
  /**
   * Trailer: Max-Forwards
   */
  Trailer("Trailer", "The Trailer general field value indicates that the given set of header fields is present in the trailer of a message encoded with chunked transfer coding"),
  /**
   * Transfer-Encoding: chunked
   */
  Transfer_Encoding("Transfer-Encoding", "The form of encoding used to safely transfer the entity to the user"),
  /**
   * Tk: ?
   */
  Tk("Tk", "Tracking HealthStatus header, value suggested to be sent in response to a DNT(do-not-track)"),
  /**
   * Upgrade: h2c, HTTPS/1.3, IRC/6.9, RTA/x11, websocket
   */
  Upgrade("Upgrade", "Ask the client to upgrade to another protocol"),
  /**
   * Example 1: Vary: *
   * <p>
   * Example 2: Vary: Accept-Language
   */
  Vary("Vary", "Tells downstream proxies how to match future request headers to decide whether the cached response can be used rather than requesting a fresh one from the origin server"),
  /**
   * Via: 1.0 fred, 1.1 example.com (Apache/1.1)
   */
  Via("Via", "Informs the client of proxies through which the response was sent"),
  /**
   * Warning: 199 Miscellaneous warning
   */
  Warning("Warning","A general warning about possible problems with the entity body"),
  /**
   * WWW-Authenticate: Basic
   */
  WWW_Authenticate("WWW-Authenticate","Indicates the authentication scheme that should be used to access the requested entity"),
  /**
   * X-Frame-Options: deny
   */
  X_Frame_Options("X-Frame-Options","Clickjacking protection: deny - no rendering within a frame, sameorigin - no rendering if origin mismatch, allow-from - allow from specified location, allowall - non-standard, allow from any location");

  // @formatter:on

  public final String value;

  public final String reasonPhrase;

  HttpResponseHeader(String value, String reasonPhrase) {
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
