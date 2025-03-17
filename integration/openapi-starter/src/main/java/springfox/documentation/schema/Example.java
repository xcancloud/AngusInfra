package springfox.documentation.schema;

import static java.util.Optional.ofNullable;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import springfox.documentation.service.VendorExtension;

/**
 * Fix: https://github.com/springfox/springfox/issues/3511
 * 
 * <pre>
 *   java.lang.NullPointerException: null
 * 	at springfox.documentation.schema.Example.equals(Example.java:131)
 * 	at java.base/java.util.Objects.equals(Objects.java:77)
 * 	at springfox.documentation.service.RequestParameter.equals(RequestParameter.java:132)
 * 	at java.base/java.util.HashMap.putVal(HashMap.java:630)
 * 	at java.base/java.util.HashMap.put(HashMap.java:607)
 * 	at java.base/java.util.HashSet.add(HashSet.java:220)
 * 	at java.base/java.util.stream.ReduceOps$3ReducingSink.accept(ReduceOps.java:169)
 * 	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:195)
 * 	at java.base/java.util.stream.ReferencePipeline$2$1.accept(ReferencePipeline.java:177)
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Example {

  private final String id;
  private final String summary;
  private final String description;
  private final Object value;
  private final String externalValue;
  private final String mediaType;
  private final List<VendorExtension> extensions = new ArrayList<>();

  /**
   * @param value - example literal
   * @deprecated @since 3.0.0 Use @see {@link Example#Example(String, String, String, Object,
   * String, String)}
   */
  @Deprecated
  public Example(Object value) {
    this.value = value;
    this.mediaType = null;
    externalValue = null;
    id = null;
    summary = null;
    description = null;
  }

  /**
   * @param mediaType - media type of the example
   * @param value     - example literal
   * @deprecated @since 3.0.0 Use @see {@link Example#Example(String, String, String, Object,
   * String, String)}
   */
  @Deprecated
  public Example(String mediaType, Object value) {
    this.mediaType = mediaType;
    this.value = value;
    externalValue = null;
    id = null;
    summary = null;
    description = null;
  }

  public Example(
      String id,
      String summary,
      String description,
      Object value,
      String externalValue,
      String mediaType) {
    this.id = id;
    this.summary = summary;
    this.description = description;
    this.value = value;
    this.externalValue = externalValue;
    this.mediaType = mediaType;
  }

  public String getId() {
    return id;
  }

  public String getSummary() {
    return summary;
  }

  public String getDescription() {
    return description;
  }

  public String getExternalValue() {
    return externalValue;
  }

  public List<VendorExtension> getExtensions() {
    return extensions;
  }

  public Object getValue() {
    return value;
  }

  public Optional<String> getMediaType() {
    return ofNullable(mediaType);
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Example example = (Example) o;
    return Objects.equals(id, example.id) &&
        Objects.equals(summary, example.summary) &&
        Objects.equals(description, example.description) &&
        Objects.equals(value, example.value) &&
        Objects.equals(externalValue, example.externalValue) &&
        Objects.equals(mediaType, example.mediaType) &&
        Objects.equals(extensions, example.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        id,
        summary,
        description,
        value,
        externalValue,
        mediaType,
        extensions);
  }
}