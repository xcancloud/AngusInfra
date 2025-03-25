package io.swagger.v3.oas.models.extension;


import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_CONNECT_TIMEOUT;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_READ_TIMEOUT;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_REQUEST_REDIRECTS;
import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_REQUEST_RETRIES;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_REQUEST_REDIRECTS;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_REQUEST_RETRIES;
import static cloud.xcan.angus.spec.utils.ObjectUtils.mapEquals;

import cloud.xcan.angus.spec.annotations.ThirdExtension;
import cloud.xcan.angus.spec.unit.TimeValue;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
@ThirdExtension
public class RequestSetting {

  @Schema(description = "Enable parameter validation. After enabling, the parameters will be verified first, and requests will only be sent after the parameter verification is passed. It is disabled by default.")
  private boolean enableParamValidation = false;

  @Schema(description = "Connection timeout. Specify the maximum waiting time for the client and server to establish a connection, default 6 seconds.")
  private TimeValue connectTimeout = DEFAULT_CONNECT_TIMEOUT;

  @Schema(description = "Read timeout. Specify how long the client has not received a server response to close the connection, default 60 seconds.")
  private TimeValue readTimeout = DEFAULT_READ_TIMEOUT;

  @Max(value = MAX_REQUEST_RETRIES)
  @Schema(description =
      "Retry number. Number of retries when the request fails. Do not retry by default, maximum allowed "
          + MAX_REQUEST_RETRIES + " times")
  private int retryNum = DEFAULT_REQUEST_RETRIES;

  @Max(value = MAX_REQUEST_REDIRECTS)
  @Schema(description =
      "Redirect number. Number of redirects when the request return 3xx status. default "
          + DEFAULT_REQUEST_REDIRECTS + " times, maximum allowed "
          + MAX_REQUEST_REDIRECTS + " times")
  private int maxRedirects;

  private Map<String, Object> extensions;

  @JsonAnyGetter
  public Map<String, Object> getExtensions() {
    return extensions;
  }

  @JsonAnySetter
  public void addExtension(String name, String value) {
    if (name == null || !name.startsWith("x-")) {
      return;
    }
    if (this.extensions == null) {
      this.extensions = new LinkedHashMap<>();
    }
    this.extensions.put(name, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RequestSetting)) {
      return false;
    }
    RequestSetting setting = (RequestSetting) o;
    return enableParamValidation == setting.enableParamValidation &&
        retryNum == setting.retryNum &&
        maxRedirects == setting.maxRedirects &&
        Objects.equals(connectTimeout.toSecond(), setting.connectTimeout.toSecond()) &&
        Objects.equals(readTimeout.toSecond(), setting.readTimeout.toSecond()) &&
        mapEquals(extensions, setting.extensions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enableParamValidation, connectTimeout.toSecond(), readTimeout.toSecond(),
        retryNum, maxRedirects, extensions);
  }
}
