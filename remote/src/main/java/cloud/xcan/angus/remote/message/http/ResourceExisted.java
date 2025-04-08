package cloud.xcan.angus.remote.message.http;

import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.angus.remote.ExceptionLevel.WARNING;
import static cloud.xcan.angus.remote.message.http.ResourceExisted.M.RESOURCE_ALREADY_EXISTS;
import static cloud.xcan.angus.remote.message.http.ResourceExisted.M.RESOURCE_ALREADY_EXISTS_KEY;
import static cloud.xcan.angus.remote.message.http.ResourceExisted.M.RESOURCE_ALREADY_EXISTS_T;
import static cloud.xcan.angus.remote.message.http.ResourceExisted.M.RESOURCE_ALREADY_EXISTS_T2;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.AbstractResultMessageException;
import java.util.Objects;
import lombok.Getter;
import lombok.ToString;

/**
 * Resource already exists exception class, which will be handled by global exception and return
 * http status 409.
 */
@Getter
@ToString
public class ResourceExisted extends AbstractResultMessageException {

  private final String resId;
  private final String resName;
  private final String code;
  private final String msg;
  private final Object[] args;

  public ResourceExisted() {
    this("", "", RESOURCE_ALREADY_EXISTS, null, RESOURCE_ALREADY_EXISTS_KEY, WARNING);
  }

  private ResourceExisted(String resId, String resName, String message,
      Object[] args, String eKey, ExceptionLevel level) {
    super(message, EventType.PROTOCOL, level, eKey);
    this.code = PROTOCOL_ERROR_CODE;
    this.msg = message;
    this.args = args;
    this.resId = resId;
    this.resName = resName;
  }

  public static ResourceExisted of(String resId) {
    return new ResourceExisted(resId, "", RESOURCE_ALREADY_EXISTS_T2,
        null, RESOURCE_ALREADY_EXISTS_KEY, WARNING);
  }

  public static ResourceExisted of(Long resId) {
    return new ResourceExisted(String.valueOf(resId), "", RESOURCE_ALREADY_EXISTS_T2,
        null, RESOURCE_ALREADY_EXISTS_KEY, WARNING);
  }

  public static ResourceExisted of(String resId, String resName) {
    return new ResourceExisted(resId, resName, RESOURCE_ALREADY_EXISTS_T,
        null, RESOURCE_ALREADY_EXISTS_KEY, WARNING);
  }

  public static ResourceExisted of(Long resId, String resName) {
    return new ResourceExisted(String.valueOf(resId), resName, RESOURCE_ALREADY_EXISTS_T,
        null, RESOURCE_ALREADY_EXISTS_KEY, WARNING);
  }

  public static ResourceExisted of(ExceptionLevel level) {
    return new ResourceExisted("", "", RESOURCE_ALREADY_EXISTS, null,
        RESOURCE_ALREADY_EXISTS_KEY, level);
  }

  public static ResourceExisted of(String resId, ExceptionLevel level) {
    return new ResourceExisted(resId, "", RESOURCE_ALREADY_EXISTS_T2, null,
        RESOURCE_ALREADY_EXISTS_KEY, level);
  }

  public static ResourceExisted of(Long resId, ExceptionLevel level) {
    return new ResourceExisted(String.valueOf(resId), "", RESOURCE_ALREADY_EXISTS_T2, null,
        RESOURCE_ALREADY_EXISTS_KEY, level);
  }

  public static ResourceExisted of(String message, Object[] args) {
    return new ResourceExisted("", "", message, args,
        RESOURCE_ALREADY_EXISTS_KEY, WARNING);
  }

  public static ResourceExisted of(String message, Object[] args, ExceptionLevel level) {
    return new ResourceExisted("", "", message, args,
        RESOURCE_ALREADY_EXISTS_KEY, level);
  }

  public static ResourceExisted of(String resId, String resName, ExceptionLevel level) {
    return new ResourceExisted(resId, resName, RESOURCE_ALREADY_EXISTS_T, null,
        RESOURCE_ALREADY_EXISTS_KEY, level);
  }

  public static ResourceExisted of(Long resId, String resName, ExceptionLevel level) {
    return new ResourceExisted(String.valueOf(resId), resName, RESOURCE_ALREADY_EXISTS_T, null,
        RESOURCE_ALREADY_EXISTS_KEY, level);
  }

  public static ResourceExisted of(String resId, String resName, String message,
      Object[] args, ExceptionLevel level) {
    return new ResourceExisted(resId, resName, message, args, RESOURCE_ALREADY_EXISTS_KEY,
        level);
  }

  public static ResourceExisted of(Long resId, String resName, String message,
      Object[] args, ExceptionLevel level) {
    return new ResourceExisted(String.valueOf(resId), resName, message, args,
        RESOURCE_ALREADY_EXISTS_KEY,
        level);
  }

  @Override
  public boolean is4xxException() {
    return true;
  }

  @Override
  public Object[] getArgs() {
    if (!Objects.isNull(args)) {
      return args;
    }
    return new Object[]{resId, resName};
  }

  public interface M {

    String RESOURCE_ALREADY_EXISTS = "xcm.resource.already.existing";
    String RESOURCE_ALREADY_EXISTS_T = "xcm.resource.already.existing.t";
    String RESOURCE_ALREADY_EXISTS_T2 = "xcm.resource.already.existing.t2";
    String RESOURCE_ALREADY_EXISTS_KEY = "resource_already_exists";

  }

}
