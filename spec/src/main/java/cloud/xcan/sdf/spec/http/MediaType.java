package cloud.xcan.sdf.spec.http;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;

/**
 * copy from javax.ws.rs.core.MediaType
 */
public class MediaType {

  private final String type;
  private final String subtype;
  private final Map<String, String> parameters;

  public static final String CHARSET_PARAMETER = "charset";

  public static final String MEDIA_TYPE_WILDCARD = "*";
  // Common media type constants

  public final static String WILDCARD = "*/*";

  public final static MediaType WILDCARD_TYPE = new MediaType();

  /**
   * A {@code String} constant representing "{@value #APPLICATION_XML}" media type.
   */
  public final static String APPLICATION_XML = "application/xml";
  /**
   * A {@link MediaType} constant representing "{@value #APPLICATION_XML}" media type.
   */
  public final static MediaType APPLICATION_XML_TYPE = new MediaType("application", "xml");
  /**
   * A {@code String} constant representing "{@value #APPLICATION_ATOM_XML}" media type.
   */
  public final static String APPLICATION_ATOM_XML = "application/atom+xml";
  /**
   * A {@link MediaType} constant representing "{@value #APPLICATION_ATOM_XML}" media type.
   */
  public final static MediaType APPLICATION_ATOM_XML_TYPE = new MediaType("application",
      "atom+xml");
  /**
   * A {@code String} constant representing "{@value #APPLICATION_XHTML_XML}" media type.
   */
  public final static String APPLICATION_XHTML_XML = "application/xhtml+xml";
  /**
   * A {@link MediaType} constant representing "{@value #APPLICATION_XHTML_XML}" media type.
   */
  public final static MediaType APPLICATION_XHTML_XML_TYPE = new MediaType("application",
      "xhtml+xml");
  /**
   * A {@code String} constant representing "{@value #APPLICATION_SVG_XML}" media type.
   */
  public final static String APPLICATION_SVG_XML = "application/svg+xml";
  /**
   * A {@link MediaType} constant representing "{@value #APPLICATION_SVG_XML}" media type.
   */
  public final static MediaType APPLICATION_SVG_XML_TYPE = new MediaType("application", "svg+xml");
  /**
   * A {@code String} constant representing "{@value #APPLICATION_JSON}" media type.
   */
  public final static String APPLICATION_JSON = "application/json";
  /**
   * A {@link MediaType} constant representing "{@value #APPLICATION_JSON}" media type.
   */
  public final static MediaType APPLICATION_JSON_TYPE = new MediaType("application", "json");
  /**
   * A {@code String} constant representing "{@value #APPLICATION_FORM_URLENCODED}" media type.
   */
  public final static String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
  /**
   * A {@link MediaType} constant representing "{@value #APPLICATION_FORM_URLENCODED}" media type.
   */
  public final static MediaType APPLICATION_FORM_URLENCODED_TYPE = new MediaType("application",
      "x-www-form-urlencoded");
  /**
   * A {@code String} constant representing "{@value #MULTIPART_FORM_DATA}" media type.
   */
  public final static String MULTIPART_FORM_DATA = "multipart/form-data";
  /**
   * A {@link MediaType} constant representing "{@value #MULTIPART_FORM_DATA}" media type.
   */
  public final static MediaType MULTIPART_FORM_DATA_TYPE = new MediaType("multipart", "form-data");
  /**
   * A {@code String} constant representing "{@value #APPLICATION_OCTET_STREAM}" media type.
   */
  public final static String APPLICATION_OCTET_STREAM = "application/octet-stream";
  /**
   * A {@link MediaType} constant representing "{@value #APPLICATION_OCTET_STREAM}" media type.
   */
  public final static MediaType APPLICATION_OCTET_STREAM_TYPE = new MediaType("application",
      "octet-stream");
  /**
   * A {@code String} constant representing "{@value #TEXT_PLAIN}" media type.
   */
  public final static String TEXT_PLAIN = "text/plain";
  /**
   * A {@link MediaType} constant representing "{@value #TEXT_PLAIN}" media type.
   */
  public final static MediaType TEXT_PLAIN_TYPE = new MediaType("text", "plain");
  /**
   * A {@code String} constant representing "{@value #TEXT_XML}" media type.
   */
  public final static String TEXT_XML = "text/xml";
  /**
   * A {@link MediaType} constant representing "{@value #TEXT_XML}" media type.
   */
  public final static MediaType TEXT_XML_TYPE = new MediaType("text", "xml");
  /**
   * A {@code String} constant representing "{@value #TEXT_HTML}" media type.
   */
  public final static String TEXT_HTML = "text/html";
  /**
   * A {@link MediaType} constant representing "{@value #TEXT_HTML}" media type.
   */
  public final static MediaType TEXT_HTML_TYPE = new MediaType("text", "html");

  public final static String APPLICATION_PROTOBUF = "application/x-protobuf";

  public final static MediaType APPLICATION_PROTOBUF_TYPE = new MediaType("application",
      "x-protobuf");
  public static final MediaType TEXT_JAVASCRIPT = new MediaType("text", "javascript");
  public static final MediaType TEXT_CSS = new MediaType("text", "css");
  ;

  private static TreeMap<String, String> createParametersMap(Map<String, String> initialValues) {
    final TreeMap<String, String> map = new TreeMap<String, String>(String::compareToIgnoreCase);
    if (initialValues != null) {
      for (Map.Entry<String, String> e : initialValues.entrySet()) {
        map.put(e.getKey().toLowerCase(), e.getValue());
      }
    }
    return map;
  }

  /**
   * Creates a new instance of {@code MediaType} with the supplied type, subtype and parameters.
   *
   * @param type       the primary type, {@code null} is equivalent to {@link
   *                   #MEDIA_TYPE_WILDCARD}.
   * @param subtype    the subtype, {@code null} is equivalent to {@link #MEDIA_TYPE_WILDCARD}.
   * @param parameters a map of media type parameters, {@code null} is the same as an empty map.
   */
  public MediaType(String type, String subtype, Map<String, String> parameters) {
    this(type, subtype, null, createParametersMap(parameters));
  }

  /**
   * Creates a new instance of {@code MediaType} with the supplied type and subtype.
   *
   * @param type    the primary type, {@code null} is equivalent to {@link #MEDIA_TYPE_WILDCARD}
   * @param subtype the subtype, {@code null} is equivalent to {@link #MEDIA_TYPE_WILDCARD}
   */
  public MediaType(String type, String subtype) {
    this(type, subtype, null, null);
  }

  /**
   * Creates a new instance of {@code MediaType} with the supplied type, subtype and "{@value
   * #CHARSET_PARAMETER}" parameter.
   *
   * @param type    the primary type, {@code null} is equivalent to {@link #MEDIA_TYPE_WILDCARD}
   * @param subtype the subtype, {@code null} is equivalent to {@link #MEDIA_TYPE_WILDCARD}
   * @param charset the "{@value #CHARSET_PARAMETER}" parameter value. If {@code null} or empty the
   *                "{@value #CHARSET_PARAMETER}" parameter will not be set.
   */
  public MediaType(String type, String subtype, String charset) {
    this(type, subtype, charset, null);
  }

  /**
   * Creates a new instance of {@code MediaType}, both type and subtype are wildcards. Consider
   * using the constant {@link #WILDCARD_TYPE} instead.
   */
  public MediaType() {
    this(MEDIA_TYPE_WILDCARD, MEDIA_TYPE_WILDCARD, null, null);
  }

  private MediaType(String type, String subtype, String charset, Map<String, String> parameterMap) {

    this.type = type == null ? MEDIA_TYPE_WILDCARD : type;
    this.subtype = subtype == null ? MEDIA_TYPE_WILDCARD : subtype;

    if (parameterMap == null) {
      parameterMap = new TreeMap<String, String>(new Comparator<String>() {

        @Override
        public int compare(String o1, String o2) {
          return o1.compareToIgnoreCase(o2);
        }
      });
    }

    if (charset != null && !charset.isEmpty()) {
      parameterMap.put(CHARSET_PARAMETER, charset);
    }
    this.parameters = Collections.unmodifiableMap(parameterMap);
  }

  /**
   * Getter for primary type.
   *
   * @return value of primary type.
   */
  public String getType() {
    return this.type;
  }

  /**
   * Checks if the primary type is a wildcard.
   *
   * @return true if the primary type is a wildcard.
   */
  public boolean isWildcardType() {
    return this.getType().equals(MEDIA_TYPE_WILDCARD);
  }

  /**
   * Getter for subtype.
   *
   * @return value of subtype.
   */
  public String getSubtype() {
    return this.subtype;
  }

  /**
   * Checks if the subtype is a wildcard.
   *
   * @return true if the subtype is a wildcard.
   */
  public boolean isWildcardSubtype() {
    return this.getSubtype().equals(MEDIA_TYPE_WILDCARD);
  }

  /**
   * Getter for a read-only parameter map. Keys are case-insensitive.
   *
   * @return an immutable map of parameters.
   */
  public Map<String, String> getParameters() {
    return parameters;
  }

  /**
   * Create a new {@code MediaType} instance with the same type, subtype and parameters copied from
   * the original instance and the supplied "{@value #CHARSET_PARAMETER}" parameter.
   *
   * @param charset the "{@value #CHARSET_PARAMETER}" parameter value. If {@code null} or empty the
   *                "{@value #CHARSET_PARAMETER}" parameter will not be set or updated.
   * @return copy of the current {@code MediaType} instance with the "{@value #CHARSET_PARAMETER}"
   * parameter set to the supplied value.
   * @since 2.0
   */
  public MediaType withCharset(String charset) {
    return new MediaType(this.type, this.subtype, charset, createParametersMap(this.parameters));
  }

  /**
   * Check if this media type is compatible with another media type. E.g. image/* is compatible with
   * image/jpeg, image/png, etc. Media type parameters are ignored. The function is commutative.
   *
   * @param other the media type to compare with.
   * @return true if the types are compatible, false otherwise.
   */
  public boolean isCompatible(MediaType other) {
    return other != null && // return false if other is null, else
        (type.equals(MEDIA_TYPE_WILDCARD) || other.type.equals(MEDIA_TYPE_WILDCARD) ||
            // both are wildcard
            // types, or
            (type.equalsIgnoreCase(other.type)
                && (subtype.equals(MEDIA_TYPE_WILDCARD) || other.subtype
                .equals(MEDIA_TYPE_WILDCARD)))
            || // same types, wildcard sub-types, or
            (type.equalsIgnoreCase(other.type) && this.subtype
                .equalsIgnoreCase(other.subtype))); // same
    // types
    // &
    // sub-types
  }

  public static MediaType valueOf(String mediaType) {
    if (StringUtils.isBlank(mediaType)) {
      return null;
    }

    String[] parts = mediaType.split(";");

    String fullType = parts[0].trim();
    if (WILDCARD.equals(fullType)) {
      fullType = "*/*";
    }
    int subIndex = fullType.indexOf('/');
    if (subIndex == -1) {
      throw new IllegalArgumentException("\"" + mediaType + "\" does not contain '/'");
    }
    if (subIndex == fullType.length() - 1) {
      throw new IllegalArgumentException(
          "\"" + mediaType + "\" does not contain subtype after '/'");
    }
    String type = fullType.substring(0, subIndex);
    String subtype = fullType.substring(subIndex + 1, fullType.length());
    if (MEDIA_TYPE_WILDCARD.equals(type) && !MEDIA_TYPE_WILDCARD.equals(subtype)) {
      throw new IllegalArgumentException(
          "A wildcard type is legal only in '*/*' (all media types).");
    }

    Map<String, String> parameters = null;
    if (parts.length > 1) {
      parameters = new LinkedHashMap<String, String>(parts.length - 1);
      for (int i = 1; i < parts.length; i++) {
        String parameter = parts[i];
        int eqIndex = parameter.indexOf('=');
        if (eqIndex != -1) {
          String attribute = parameter.substring(0, eqIndex);
          String value = parameter.substring(eqIndex + 1);
          parameters.put(attribute, value);
        }
      }
    }
    return new MediaType(type, subtype, parameters);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MediaType)) {
      return false;
    }

    MediaType other = (MediaType) obj;
    return (this.type.equalsIgnoreCase(other.type) && this.subtype.equalsIgnoreCase(other.subtype));
  }


  @Override
  public int hashCode() {
    return (this.type.toLowerCase() + this.subtype.toLowerCase()).hashCode();
  }

  @Override
  public String toString() {
    String contentType = String.format("%s/%s", this.type, this.subtype);
    String charset = this.parameters.get(CHARSET_PARAMETER);
    if (charset != null) {
      return String.format("%s; charset=%s", contentType, charset);
    }
    return contentType;
  }
}
