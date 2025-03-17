package cloud.xcan.sdf.core.fegin;

import static cloud.xcan.sdf.api.message.CommProtocolException.M.PROTOCOL_UNKNOWN;
import static cloud.xcan.sdf.api.message.CommSysException.M.RPC_API_EXCEPTION;
import static cloud.xcan.sdf.api.message.http.ServiceUnavailable.M.SERVICE_UNAVAILABLE_T;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import cloud.xcan.sdf.api.ApiResult;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.api.message.CommSysException;
import cloud.xcan.sdf.api.message.http.Forbidden;
import cloud.xcan.sdf.api.message.http.GatewayTimeout;
import cloud.xcan.sdf.api.message.http.MediaTypeNotSupported;
import cloud.xcan.sdf.api.message.http.MethodNotSupported;
import cloud.xcan.sdf.api.message.http.ResourceExisted;
import cloud.xcan.sdf.api.message.http.ResourceNotFound;
import cloud.xcan.sdf.api.message.http.ServiceUnavailable;
import cloud.xcan.sdf.api.message.http.Unauthorized;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import lombok.SneakyThrows;

public class CustomErrorDecoder implements ErrorDecoder {

  ObjectMapper objectMapper;

  /**
   * RPC message is already formatted
   */
  Object[] emptyArgs = new Object[]{};

  public CustomErrorDecoder(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @SneakyThrows
  @Override
  public Exception decode(String methodKey, Response response) {
    ApiResult<?> result;
    byte[] body = {};
    if (response.body() != null) {
      try {
        body = Util.toByteArray(response.body().asInputStream());
      } catch (IOException e) {
        throw CommProtocolException.of(PROTOCOL_UNKNOWN);
      }
    }

    String exceptionBody = new String(body);
    switch (response.status()) {
      case 503:
        return ServiceUnavailable.of(SERVICE_UNAVAILABLE_T, new Object[]{exceptionBody});
      case 504:
        return new GatewayTimeout();
      default:
        try {
          result = objectMapper.readValue(exceptionBody, ApiResult.class);
          if (result.isSuccess()){
            // The Json structure is inconsistent with ApiResult
            throw new IllegalStateException("Global unhandled exception");
          }
          switch (response.status()) {
            case 400:
              return CommProtocolException.of(result.getMsg(), result.getEKey());
            case 401:
              return Unauthorized.of(result.getMsg(), result.getEKey());
            case 403:
              return Forbidden.of(result.getMsg(), result.getEKey());
            case 404:
              return ResourceNotFound.of(result.getMsg(), emptyArgs, result.getEKey());
            case 405:
              return MethodNotSupported.of(result.getMsg(), emptyArgs, result.getEKey());
            case 409:
              return ResourceExisted.of(result.getMsg(), emptyArgs);
            case 415:
              return MediaTypeNotSupported.of(result.getMsg(), result.getEKey());
            //      case 429: TODO
            //        return new TooManyRequests(message, request, body);
            case 500:
              return CommSysException.of(result.getCode(),result.getMsg(), result.getEKey());
            default:
              return CommSysException.of(RPC_API_EXCEPTION, isEmpty(result.getEKey())
                  ? exceptionBody : result.getEKey());
          }
        } catch (Exception e) {
          throw CommProtocolException.of(String.format("Unknown protocol Error: %s", exceptionBody));
        }
    }
  }

}