package cloud.xcan.angus.core.event.source;

import cloud.xcan.angus.api.enums.ApiType;
import cloud.xcan.angus.api.enums.EventType;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.util.LinkedMultiValueMap;

/**
 * @author liuxiaolong
 */
@Setter
@Getter
@Accessors(chain = true)
@ToString
public class ApiLog extends SimpleSource {

  private String requestId;
  private String remote;
  private String clientId;
  private String clientSource;
  private Long tenantId;
  private String tenantName;
  private Long userId;
  private String fullname;
  private String serviceCode;
  private String serviceName;
  private String instanceId;
  private ApiType apiType;
  private String method;
  private String uri;
  private LocalDateTime requestDate;
  private String queryParam;
  private LinkedMultiValueMap<String, String> requestHeaders;
  private String requestBody;
  private Integer requestSize;

  ////----------------------------------------
  private Integer status;
  private LinkedMultiValueMap<String, String> responseHeaders;
  private String responseBody;
  private LocalDateTime responseDate;
  private Integer responseSize;
  private Long elapsedMillis;

  public ApiLog() {
  }

  private ApiLog(Builder builder) {
    setType(builder.type);
    setCode(builder.code);
    setDescription(builder.description);
    setTimestamp(builder.requestDate);
    setExt(builder.ext);
    setRequestId(builder.requestId);
    setRemote(builder.remote);
    setClientId(builder.clientId);
    setClientSource(builder.clientSource);
    setTenantId(builder.tenantId);
    setTenantName(builder.tenantName);
    setUserId(builder.userId);
    setFullname(builder.fullname);
    setServiceCode(builder.serviceCode);
    setServiceName(builder.serviceName);
    setInstanceId(builder.instanceId);
    setApiType(builder.apiType);
    setMethod(builder.method);
    setUri(builder.uri);
    setRequestDate(builder.requestDate);
    setQueryParam(builder.queryParam);
    setRequestHeaders(builder.requestHeaders);
    setRequestBody(builder.requestBody);
    setRequestSize(builder.requestSize);
    setStatus(builder.status);
    setResponseHeaders(builder.responseHeaders);
    setResponseBody(builder.responseBody);
    setResponseDate(builder.responseDate);
    setResponseSize(builder.responseSize);
    setElapsedMillis(builder.elapsedMillis);
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {

    private final EventType type = EventType.API;
    private String code;
    private String description;
    //private LocalDateTime timestamp;
    private Map<String, Object> ext;

    private String requestId;
    private String remote;
    private String clientId;
    private String clientSource;
    private Long tenantId;
    private String tenantName;
    private Long userId;
    private String fullname;
    private String serviceCode;
    private String serviceName;
    private String instanceId;
    private ApiType apiType;
    private String method;
    private String uri;
    private LocalDateTime requestDate;
    private String queryParam;
    private LinkedMultiValueMap<String, String> requestHeaders;
    private String requestBody;
    private Integer requestSize;
    private Integer status;
    private LinkedMultiValueMap<String, String> responseHeaders;
    private String responseBody;
    private LocalDateTime responseDate;
    private Integer responseSize;
    private Long elapsedMillis;

    private Builder() {
    }

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder ext(Map<String, Object> ext) {
      this.ext = ext;
      return this;
    }

    public Builder requestId(String requestId) {
      this.requestId = requestId;
      return this;
    }

    public Builder remote(String remote) {
      this.remote = remote;
      return this;
    }

    public Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    public Builder clientSource(String clientSource) {
      this.clientSource = clientSource;
      return this;
    }

    public Builder tenantId(Long tenantId) {
      this.tenantId = tenantId;
      return this;
    }

    public Builder tenantName(String tenantName) {
      this.tenantName = tenantName;
      return this;
    }

    public Builder userId(Long userId) {
      this.userId = userId;
      return this;
    }

    public Builder fullname(String fullname) {
      this.fullname = fullname;
      return this;
    }

    public Builder serviceCode(String serviceCode) {
      this.serviceCode = serviceCode;
      return this;
    }

    public Builder serviceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public Builder instanceId(String instanceId) {
      this.instanceId = instanceId;
      return this;
    }

    public Builder apiType(ApiType apiType) {
      this.apiType = apiType;
      return this;
    }

    public Builder method(String method) {
      this.method = method;
      return this;
    }

    public Builder uri(String uri) {
      this.uri = uri;
      return this;
    }

    public Builder requestDate(LocalDateTime requestDate) {
      this.requestDate = requestDate;
      return this;
    }

    public Builder queryParam(String queryParam) {
      this.queryParam = queryParam;
      return this;
    }

    public Builder requestHeaders(LinkedMultiValueMap<String, String> requestHeaders) {
      this.requestHeaders = requestHeaders;
      return this;
    }

    public Builder requestBody(String requestBody) {
      this.requestBody = requestBody;
      return this;
    }

    public Builder requestSize(Integer requestSize) {
      this.requestSize = requestSize;
      return this;
    }

    public Builder status(Integer status) {
      this.status = status;
      return this;
    }

    public Builder responseHeaders(LinkedMultiValueMap<String, String> responseHeaders) {
      this.responseHeaders = responseHeaders;
      return this;
    }

    public Builder responseBody(String responseBody) {
      this.responseBody = responseBody;
      return this;
    }

    public Builder responseDate(LocalDateTime responseDate) {
      this.responseDate = responseDate;
      return this;
    }

    public Builder responseSize(Integer responseSize) {
      this.responseSize = responseSize;
      return this;
    }

    public Builder elapsedMillis(Long elapsedMillis) {
      this.elapsedMillis = elapsedMillis;
      return this;
    }

    public ApiLog build() {
      return new ApiLog(this);
    }
  }
}
