package cloud.xcan.angus.api.pojo;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.angus.spec.http.HttpHeader;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Setter
@Getter
@Accessors(chain = true)
public class ApisRequestLog {

  private String requestId;

  private String remote;

  private String exceptionMessage;

  private Long mockServiceId;

  private Long mockApisId;

  private String mockApisName;

  private String method;

  private String uri;

  @JsonIgnore
  private String endpoint;

  private Boolean pushback;

  private String pushbackRequestId;

  private String queryParameters;

  private List<HttpHeader> requestHeaders = new ArrayList<>();

  private String requestContentEncoding;

  private String requestBody;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime requestDate;

  private int responseStatus;

  private List<HttpHeader> responseHeaders = new ArrayList<>();

  private String responseBody;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime responseDate;

  @JsonIgnore
  private long startTime;
  @JsonIgnore
  private long endTime;

  public ApisRequestLog() {
  }

  @JsonIgnore
  public long getDuration() {
    return startTime > 0 && endTime > 0 ? endTime - startTime : 0;
  }

  public ApisRequestLog setRequestDate(LocalDateTime requestDate) {
    this.requestDate = requestDate;
    this.startTime = System.currentTimeMillis();
    return this;
  }

  public ApisRequestLog setResponseDate(LocalDateTime responseDate) {
    this.responseDate = responseDate;
    this.endTime = System.currentTimeMillis();
    return this;
  }

  private ApisRequestLog(Builder builder) {
    setRequestId(builder.requestId);
    setRemote(builder.remote);
    setExceptionMessage(builder.exceptionMessage);
    setMockServiceId(builder.mockServiceId);
    setMockApisId(builder.mockApisId);
    setMockApisName(builder.mockApisName);
    setMethod(builder.method);
    setUri(builder.uri);
    setEndpoint(builder.endpoint);
    setPushback(builder.pushback);
    setPushbackRequestId(builder.pushbackRequestId);
    setQueryParameters(builder.queryParameters);
    setRequestHeaders(builder.requestHeaders);
    setRequestContentEncoding(builder.requestContentEncoding);
    setRequestBody(builder.requestBody);
    setRequestDate(builder.requestDate);
    setResponseStatus(builder.responseStatus);
    setResponseHeaders(builder.responseHeaders);
    setResponseBody(builder.responseBody);
    setResponseDate(builder.responseDate);
    setStartTime(builder.startTime);
    setEndTime(builder.endTime);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {

    private String requestId;
    private String remote;
    private String exceptionMessage;
    private Long mockServiceId;
    private Long mockApisId;
    private String mockApisName;
    private String method;
    private String uri;
    private String endpoint;
    private Boolean pushback;
    private String pushbackRequestId;
    private String queryParameters;
    private List<HttpHeader> requestHeaders = new ArrayList<>();
    private String requestContentEncoding;
    private String requestBody;
    private LocalDateTime requestDate;
    private int responseStatus;
    private List<HttpHeader> responseHeaders = new ArrayList<>();
    private String responseBody;
    private LocalDateTime responseDate;
    @JsonIgnore
    private long startTime;
    @JsonIgnore
    private long endTime;

    private Builder() {
    }

    public Builder requestId(String val) {
      requestId = val;
      return this;
    }

    public Builder exceptionMessage(String val) {
      exceptionMessage = val;
      return this;
    }

    public Builder remote(String val) {
      remote = val;
      return this;
    }

    public Builder mockServiceId(Long val) {
      mockServiceId = val;
      return this;
    }

    public Builder mockApisId(Long val) {
      mockApisId = val;
      return this;
    }

    public Builder mockApisName(String val) {
      mockApisName = val;
      return this;
    }

    public Builder method(String val) {
      method = val;
      return this;
    }

    public Builder uri(String val) {
      uri = val;
      return this;
    }

    public Builder endpoint(String val) {
      endpoint = val;
      return this;
    }

    public Builder pushback(Boolean val) {
      pushback = val;
      return this;
    }

    public Builder pushbackRequestId(String val) {
      pushbackRequestId = val;
      return this;
    }

    public Builder queryParameters(String val) {
      queryParameters = val;
      return this;
    }

    public Builder requestHeaders(List<HttpHeader> val) {
      requestHeaders = val;
      return this;
    }

    public Builder requestContentEncoding(String val) {
      requestContentEncoding = val;
      return this;
    }

    public Builder requestBody(String val) {
      requestBody = val;
      return this;
    }

    public Builder requestDate(LocalDateTime val) {
      requestDate = val;
      startTime = System.currentTimeMillis();
      return this;
    }

    public Builder responseStatus(int val) {
      responseStatus = val;
      return this;
    }

    public Builder responseHeaders(List<HttpHeader> val) {
      responseHeaders = val;
      return this;
    }

    public Builder responseBody(String val) {
      responseBody = val;
      return this;
    }

    public Builder responseDate(LocalDateTime val) {
      responseDate = val;
      endTime = System.currentTimeMillis();
      return this;
    }

    public ApisRequestLog build() {
      return new ApisRequestLog(this);
    }
  }
}
