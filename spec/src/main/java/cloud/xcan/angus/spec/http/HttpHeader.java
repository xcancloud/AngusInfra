package cloud.xcan.angus.spec.http;


import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_VALUE_LENGTH;

import cloud.xcan.angus.spec.experimental.Assert;
import java.util.Comparator;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class HttpHeader implements Comparator<HttpHeader> {

  @Length(max = MAX_PARAM_NAME_LENGTH)
  private String name;

  /**
   * When an HTTP request header field has multiple values, there are two standard formats that can
   * be used:
   * <pre>
   * 1. Separate multiple values with commas, for example:
   * Accept-Language: en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7
   *
   * Note: "q" represents Quality Value, which is used to specify the relative priority of the language
   * or character set preferred by the client in the Accept-Language header of an HTTP request.
   * It has a value range of 0 to 1, where values closer to 1 indicate higher priority.
   *
   * 2. Use multiple identical HTTP request header fields, for example:
   * Cache-Control: no-cache
   * Cache-Control: max-age=0
   * </pre>
   */
  @Length(max = MAX_PARAM_VALUE_LENGTH)
  private String value;

  public HttpHeader() {
  }

  /**
   * Constructs with name and value.
   *
   * @param name  the header name
   * @param value the header value
   */
  public HttpHeader(final String name, final String value) {
    this.name = Assert.assertNotNull(name, "Name is null");
    this.value = value;
  }

  public static HttpHeader of(final String name, final String value) {
    return new HttpHeader(name, value);
  }

  private HttpHeader(Builder builder) {
    setName(builder.name);
    setValue(builder.value);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Override
  public int compare(HttpHeader o1, HttpHeader o2) {
    return o1.getName().compareTo(o2.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof HttpHeader)) {
      return false;
    }
    HttpHeader that = (HttpHeader) o;
    return Objects.equals(name, that.name) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value);
  }

  public static final class Builder {

    private String name;
    private String value;

    private Builder() {
    }

    public Builder name(String val) {
      name = val;
      return this;
    }

    public Builder value(String val) {
      value = val;
      return this;
    }

    public HttpHeader build() {
      return new HttpHeader(this);
    }
  }
}
