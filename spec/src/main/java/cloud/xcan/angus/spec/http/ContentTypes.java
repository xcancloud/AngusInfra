package cloud.xcan.angus.spec.http;

import static org.apache.commons.lang3.StringUtils.isBlank;

import cloud.xcan.angus.api.pojo.Pair;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.StandardCharsets;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Content type information consisting of a MIME type and an optional charset.
 * <p>
 * `Content-Type: [type]/[subtype]; parameter`
 *
 * <p>
 * This class makes no attempts to verify validity of the MIME type. The input parameters of the
 * {@link #create(String, String)} method, however, may not contain characters {@code <">, <;>, <,>}
 * reserved by the HTTP specification.
 */
public class ContentTypes implements ContentType, Serializable {

  // constants
  public static ContentTypes APPLICATION_ATOM_XML = create(
      "application/atom+xml", StandardCharsets.ISO_8859_1);
  public static ContentTypes APPLICATION_FORM_URLENCODED = create(
      "application/x-www-form-urlencoded", StandardCharsets.ISO_8859_1);
  public static ContentTypes APPLICATION_JSON = create(
      "application/json", StandardCharsets.UTF_8);
  public static ContentTypes APPLICATION_OCTET_STREAM = create(
      "application/octet-stream", (Charset) null);
  public static ContentTypes APPLICATION_PDF = create(
      "application/pdf", (Charset) null);
  public static ContentTypes APPLICATION_MSWORD = create(
      "application/msword", (Charset) null);
  public static ContentTypes APPLICATION_SOAP_XML = create(
      "application/soap+xml", StandardCharsets.UTF_8);
  public static ContentTypes APPLICATION_SVG_XML = create(
      "application/svg+xml", StandardCharsets.ISO_8859_1);
  public static ContentTypes APPLICATION_XHTML_XML = create(
      "application/xhtml+xml", StandardCharsets.ISO_8859_1);
  public static ContentTypes APPLICATION_XML = create(
      "application/xml", StandardCharsets.ISO_8859_1);

  public static ContentTypes MULTIPART_FORM_DATA = create(
      "multipart/form-data", StandardCharsets.ISO_8859_1);
  public static ContentTypes TEXT_HTML = create(
      "text/html", StandardCharsets.ISO_8859_1);
  public static ContentTypes TEXT_PLAIN = create(
      "text/plain", StandardCharsets.ISO_8859_1);
  public static ContentTypes TEXT_XML = create(
      "text/xml", StandardCharsets.ISO_8859_1);
  public static ContentTypes WILDCARD = create(
      "*/*", (Charset) null);

  public static ContentTypes IMAGE_BMP = create("image/bmp");
  public static ContentTypes IMAGE_GIF = create("image/gif");
  public static ContentTypes IMAGE_JPEG = create("image/jpeg");
  public static ContentTypes IMAGE_PNG = create("image/png");
  public static ContentTypes IMAGE_SVG = create("image/svg+xml");
  public static ContentTypes IMAGE_TIFF = create("image/tiff");
  public static ContentTypes IMAGE_WEBP = create("image/webp");

  public static ContentTypes VIDEO_MP4 = create("video/mp4");
  public static ContentTypes VIDEO_MSVIDEO = create("video/x-msvideo");
  public static ContentTypes VIDEO_X_MS_WMV = create("video/x-ms-wmv");
  public static ContentTypes VIDEO_QUICKTIME = create("video/quicktime");
  public static ContentTypes VIDEO_X_MATROSKA = create("video/x-matroska");
  public static ContentTypes VIDEO_X_FLV = create("video/x-flv");

  public static ContentTypes AUDIO_MP3 = create("audio/mpeg");
  public static ContentTypes AUDIO_X_WAV = create("audio/x-wav");
  public static ContentTypes AUDIO_OGG = create("audio/ogg");
  public static ContentTypes AUDIO_FLAC = create("audio/flac");
  public static ContentTypes AUDIO_AAC = create("audio/aac");

  private static Map<String, ContentTypes> CONTENT_TYPE_MAP;

  static {
    ContentTypes[] contentTypes = {
        APPLICATION_ATOM_XML,
        APPLICATION_FORM_URLENCODED,
        APPLICATION_JSON,
        APPLICATION_OCTET_STREAM,
        APPLICATION_PDF,
        APPLICATION_MSWORD,
        APPLICATION_SOAP_XML,
        APPLICATION_SVG_XML,
        APPLICATION_XHTML_XML,
        APPLICATION_XML,

        MULTIPART_FORM_DATA,
        TEXT_HTML,
        TEXT_PLAIN,
        TEXT_XML,

        IMAGE_BMP,
        IMAGE_GIF,
        IMAGE_JPEG,
        IMAGE_PNG,
        IMAGE_SVG,
        IMAGE_TIFF,
        IMAGE_WEBP,

        VIDEO_MP4,
        VIDEO_MSVIDEO,
        VIDEO_X_MS_WMV,
        VIDEO_QUICKTIME,
        VIDEO_X_MATROSKA,
        VIDEO_X_FLV,

        AUDIO_MP3,
        AUDIO_X_WAV,
        AUDIO_OGG,
        AUDIO_FLAC,
        AUDIO_AAC
    };
    HashMap<String, ContentTypes> map = new HashMap<>();
    for (ContentTypes contentType : contentTypes) {
      map.put(contentType.getMimeType(), contentType);
    }
    CONTENT_TYPE_MAP = Collections.unmodifiableMap(map);
  }

  // defaults
  public static ContentTypes DEFAULT_TEXT = TEXT_PLAIN;
  public static ContentTypes DEFAULT_BINARY = APPLICATION_OCTET_STREAM;

  private final String mimeType;
  private final Charset charset;
  private final List<Pair<String, String>> params;

  public ContentTypes(String mimeType, Charset charset) {
    this.mimeType = mimeType;
    this.charset = charset;
    this.params = null;
  }

  public ContentTypes(String mimeType, Charset charset,
      List<Pair<String, String>> params) {
    this.mimeType = mimeType;
    this.charset = charset;
    this.params = params;
  }

  public String getMimeType() {
    return this.mimeType;
  }

  public Charset getCharset() {
    return this.charset;
  }

  public String getParameter(String name) {
    Assert.assertNotEmpty(name, "Parameter name");
    if (this.params == null) {
      return null;
    }
    for (Pair<String, String> param : this.params) {
      if (param.value.equalsIgnoreCase(name)) {
        return param.value;
      }
    }
    return null;
  }

  /**
   * Generates textual representation of this content type which can be used as the value of a
   * {@code Content-Type} header.
   */
  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append(this.mimeType);
    if (this.params != null) {
      buf.append("; ");
      buf.append(Pair.format(params, "; ", "="));
    } else if (this.charset != null) {
      buf.append("; charset=");
      buf.append(this.charset.name());
    }
    return buf.toString();
  }

  private static boolean valid(String s) {
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (ch == '"' || ch == ',' || ch == ';') {
        return false;
      }
    }
    return true;
  }

  /**
   * Creates a new instance of {@link ContentTypes}.
   *
   * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain characters
   *                 {@code <">, <;>, <,>} reserved by the HTTP specification.
   * @param charset  charset.
   * @return content type
   */
  public static ContentTypes create(String mimeType, Charset charset) {
    String normalizedMimeType = Assert.assertNotBlank(mimeType, "MIME type")
        .toLowerCase(Locale.ROOT);
    Assert.assertTrue(valid(normalizedMimeType), "MIME type may not contain reserved characters");
    return new ContentTypes(normalizedMimeType, charset);
  }

  /**
   * Creates a new instance of {@link ContentTypes} without a charset.
   *
   * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain characters
   *                 {@code <">, <;>, <,>} reserved by the HTTP specification.
   * @return content type
   */
  public static ContentTypes create(String mimeType) {
    return create(mimeType, (Charset) null);
  }

  /**
   * Creates a new instance of {@link ContentTypes}.
   *
   * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain characters
   *                 {@code <">, <;>, <,>} reserved by the HTTP specification.
   * @param charset  charset. It may not contain characters {@code <">, <;>, <,>} reserved by the
   *                 HTTP specification. This parameter is optional.
   * @return content type
   * @throws UnsupportedCharsetException Thrown when the named charset is not available in this
   *                                     instance of the Java virtual machine
   */
  public static ContentTypes create(String mimeType, String charset)
      throws UnsupportedCharsetException {
    return create(mimeType, !isBlank(charset) ? Charset.forName(charset) : null);
  }

  private static ContentTypes create(String mimeType, List<Pair<String, String>> params,
      boolean strict) {
    Charset charset = null;
    for (Pair<String, String> param : params) {
      if ("charset".equalsIgnoreCase(param.name)) {
        String s = param.value;
        if (!isBlank(s)) {
          try {
            charset = Charset.forName(s);
          } catch (UnsupportedCharsetException ex) {
            if (strict) {
              throw ex;
            }
          }
        }
        break;
      }
    }
    return new ContentTypes(mimeType, charset, params != null && params.size() > 0 ? params : null);
  }

  /**
   * Creates a new instance of {@link ContentTypes} with the given parameters.
   *
   * @param mimeType MIME type. It may not be {@code null} or empty. It may not contain characters
   *                 {@code <">, <;>, <,>} reserved by the HTTP specification.
   * @param params   parameters.
   * @return content type
   */
  public static ContentTypes create(String mimeType, Pair<String, String>... params)
      throws UnsupportedCharsetException {
    String type = Assert.assertNotBlank(mimeType, "MIME type").toLowerCase(Locale.ROOT);
    Assert.assertTrue(valid(type), "MIME type may not contain reserved characters");
    return create(mimeType, Arrays.asList(params), true);
  }

  /**
   * Parses textual representation of {@code Content-Type} value.
   *
   * @param s text
   * @return content type
   */
  public static ContentTypes parse(String s) {
    return parse(s, false);
  }

  /**
   * Parses textual representation of {@code Content-Type} value.
   *
   * @param s text
   * @return content type
   */
  public static ContentTypes parse(String s, boolean strict) {
    Assert.assertNotNull(s, "Content type");
    String[] elements = s.split(";", 2);
    if (elements.length > 0) {
      return create(elements[0].trim(), elements.length > 1
          ? Pair.parse(elements[1].trim()) : Collections.emptyList(), strict);
    }
    throw new IllegalArgumentException("Invalid content type: " + s);
  }

  /**
   * Returns {@code Content-Type} for the given MIME type.
   *
   * @param mimeType MIME type
   * @return content type or {@code null} if not known.
   */
  public static ContentTypes getByMimeType(String mimeType) {
    if (mimeType == null) {
      return null;
    }
    return CONTENT_TYPE_MAP.get(mimeType);
  }

  /**
   * Creates a new instance with this MIME type and the given Charset.
   *
   * @param charset charset
   * @return a new instance with this MIME type and the given Charset.
   */
  public ContentTypes withCharset(Charset charset) {
    return create(this.getMimeType(), charset);
  }

  /**
   * Creates a new instance with this MIME type and the given Charset name.
   *
   * @param charset name
   * @return a new instance with this MIME type and the given Charset name.
   * @throws UnsupportedCharsetException Thrown when the named charset is not available in this
   *                                     instance of the Java virtual machine
   */
  public ContentTypes withCharset(String charset) {
    return create(this.getMimeType(), charset);
  }

  public static boolean isFormData(String contentType) {
    return MULTIPART_FORM_DATA.mimeType.equals(contentType);
  }

  public static boolean isFormUrlencoded(String contentType) {
    return APPLICATION_FORM_URLENCODED.mimeType.equals(contentType);
  }

  public static boolean isForm(String contentType) {
    return isFormData(contentType) || isFormUrlencoded(contentType);
  }

  /**
   * @param contentDisposition format: `Content-Disposition: attachment; filename="fname.ext"`
   */
  public static boolean isFileDownload(ContentTypes contentTypes, String contentDisposition) {
    if (contentDisposition != null
        && (contentDisposition.startsWith("attachment") || contentDisposition.startsWith("inline"))
        && contentDisposition.indexOf("filename=") > 0) {
      return APPLICATION_OCTET_STREAM.equals(contentTypes);
    }
    return false;
  }

  /**
   * @param contentDisposition format: `Content-Disposition: attachment; filename="fname.ext"`
   */
  public static boolean isFileDownload(String contentType, String contentDisposition) {
    if (contentDisposition != null
        && (contentDisposition.startsWith("attachment") || contentDisposition.startsWith("inline"))
        && contentDisposition.indexOf("filename=") > 0) {
      return APPLICATION_OCTET_STREAM.mimeType.equals(contentType);
    }
    return false;
  }

  public static boolean isBinaryContent(ContentTypes contentTypes) {
    return isBinaryContent(contentTypes.getMimeType());
  }

  public static boolean isBinaryContent(String contentType) {
    return contentType.startsWith("image") || contentType.startsWith("audio")
        || contentType.startsWith("video") || APPLICATION_OCTET_STREAM.mimeType.equals(contentType)
        || APPLICATION_PDF.mimeType.equals(contentType)
        || APPLICATION_MSWORD.mimeType.equals(contentType);
  }

  public static boolean isTextContent(ContentTypes contentTypes) {
    return isTextContent(contentTypes.getMimeType());
  }

  public static boolean isTextContent(String contentType) {
    return contentType.startsWith(APPLICATION_ATOM_XML.getMimeType())
        || contentType.startsWith(APPLICATION_JSON.getMimeType())
        || contentType.startsWith(APPLICATION_SVG_XML.getMimeType())
        || contentType.startsWith(APPLICATION_XHTML_XML.getMimeType())
        || contentType.startsWith(APPLICATION_XML.getMimeType())
        || contentType.startsWith(TEXT_HTML.getMimeType())
        || contentType.startsWith(TEXT_PLAIN.getMimeType())
        || contentType.startsWith(TEXT_XML.getMimeType());
  }
}
