package cloud.xcan.sdf.api.message.http;

import static cloud.xcan.sdf.api.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.sdf.api.ExceptionLevel.WARNING;
import static cloud.xcan.sdf.api.message.http.ResourceNotFound.MKey.RESOURCE_NOT_FOUND;
import static cloud.xcan.sdf.api.message.http.ResourceNotFound.MKey.RESOURCE_NOT_FOUND_KEY;
import static cloud.xcan.sdf.api.message.http.ResourceNotFound.MKey.RESOURCE_NOT_FOUND_T;
import static cloud.xcan.sdf.api.message.http.ResourceNotFound.MKey.RESOURCE_NOT_FOUND_T2;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.api.message.AbstractResultMessageException;
import java.util.Objects;
import lombok.Getter;
import lombok.ToString;

/**
 * Resource not found exception class, which will be handled by global exception and return http
 * status 404.
 * <p>
 * Including pages and data resources.
 */
@Getter
@ToString
public class ResourceNotFound extends AbstractResultMessageException {

  private final String resId;
  private final String resName;
  private final String code;
  private final String msg;
  private final Object[] agrs;

  public ResourceNotFound() {
    this("", "", RESOURCE_NOT_FOUND, null, RESOURCE_NOT_FOUND_KEY, WARNING);
  }

  private ResourceNotFound(String resId, String resName, String message, Object[] agrs,
      String eKey, ExceptionLevel level) {
    super(message, EventType.PROTOCOL, level, eKey);
    this.resId = resId;
    this.resName = resName;
    this.code = PROTOCOL_ERROR_CODE;
    this.msg = message;
    this.agrs = agrs;
  }

  public static ResourceNotFound of() {
    return new ResourceNotFound("", "", RESOURCE_NOT_FOUND, null, RESOURCE_NOT_FOUND_KEY,
        WARNING);
  }

  public static ResourceNotFound of(String resId) {
    return new ResourceNotFound(resId, "", RESOURCE_NOT_FOUND_T, null,
        RESOURCE_NOT_FOUND_KEY, WARNING);
  }

  public static ResourceNotFound of(Long resId) {
    return new ResourceNotFound(String.valueOf(resId), "", RESOURCE_NOT_FOUND_T, null,
        RESOURCE_NOT_FOUND_KEY, WARNING);
  }

  public static ResourceNotFound of(String resId, String resName) {
    return new ResourceNotFound(resId, resName, RESOURCE_NOT_FOUND_T2,
        null, RESOURCE_NOT_FOUND_KEY, WARNING);
  }

  public static ResourceNotFound of(Long resId, String resName) {
    return new ResourceNotFound(String.valueOf(resId), resName, RESOURCE_NOT_FOUND_T2,
        null, RESOURCE_NOT_FOUND_KEY, WARNING);
  }

  public static ResourceNotFound of(ExceptionLevel level) {
    return new ResourceNotFound("", "", RESOURCE_NOT_FOUND, null, RESOURCE_NOT_FOUND_KEY,
        level);
  }

  public static ResourceNotFound of(String message, Object[] agrs) {
    return new ResourceNotFound("", "", message, agrs, null, WARNING);
  }

  public static ResourceNotFound of(String message, Object[] agrs, String eKey) {
    return new ResourceNotFound("", "", message, agrs, eKey, WARNING);
  }

  public static ResourceNotFound of(String resId, String resName, String message,
      String eKey, Object[] agrs) {
    return new ResourceNotFound(resId, resName, message, agrs, eKey, WARNING);
  }

  public static ResourceNotFound of(Long resId, String resName, String message,
      String eKey, Object[] agrs) {
    return new ResourceNotFound(String.valueOf(resId), resName, message, agrs, eKey, WARNING);
  }

  public static ResourceNotFound of(String resId, ExceptionLevel level) {
    return new ResourceNotFound(resId, "", RESOURCE_NOT_FOUND_T, null,
        RESOURCE_NOT_FOUND_KEY, level);
  }

  public static ResourceNotFound of(Long resId, ExceptionLevel level) {
    return new ResourceNotFound(String.valueOf(resId), "", RESOURCE_NOT_FOUND_T, null,
        RESOURCE_NOT_FOUND_KEY, level);
  }

  public static ResourceNotFound of(String resId, String resName, ExceptionLevel level) {
    return new ResourceNotFound(resId, resName, RESOURCE_NOT_FOUND_T2, null,
        RESOURCE_NOT_FOUND_KEY, level);
  }

  public static ResourceNotFound of(Long resId, String resName, ExceptionLevel level) {
    return new ResourceNotFound(String.valueOf(resId), resName, RESOURCE_NOT_FOUND_T2, null,
        RESOURCE_NOT_FOUND_KEY, level);
  }

  public static ResourceNotFound of(String resId, String resName, String message,
      Object[] args, ExceptionLevel level) {
    return new ResourceNotFound(resId, resName, message, args, RESOURCE_NOT_FOUND_KEY, level);
  }

  public static ResourceNotFound of(Long resId, String resName, String message,
      Object[] args, ExceptionLevel level) {
    return new ResourceNotFound(String.valueOf(resId), resName, message, args, RESOURCE_NOT_FOUND_KEY, level);
  }

  @Override
  public boolean is4xxException() {
    return true;
  }

  @Override
  public Object[] getArgs() {
    if (!Objects.isNull(agrs)) {
      return agrs;
    }
    return new Object[]{this.resId, this.resName};
  }

  public interface MKey {

    String RESOURCE_NOT_FOUND = "xcm.resource.not.found";
    String RESOURCE_NOT_FOUND_T = "xcm.resource.not.found.t";
    String RESOURCE_NOT_FOUND_T2 = "xcm.resource.not.found.t2";
    String RESOURCE_NOT_FOUND_KEY = "resource_not_found";

    String HANDLER_NOT_FOUND = "xcm.handler.not.found";
    String HANDLER_NOT_FOUND_T = "xcm.handler.not.found.t";
    String HANDLER_NOT_FOUND_KEY = "handler_not_found";

  }
}
