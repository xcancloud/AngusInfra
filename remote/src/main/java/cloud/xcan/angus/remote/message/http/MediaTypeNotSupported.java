package cloud.xcan.angus.remote.message.http;

import static cloud.xcan.angus.remote.ApiConstant.ECode.PROTOCOL_ERROR_CODE;
import static cloud.xcan.angus.remote.ExceptionLevel.IGNORABLE;
import static cloud.xcan.angus.remote.message.http.MediaTypeNotSupported.M.MEDIA_TYPE_NOT_SUPPORTED;
import static cloud.xcan.angus.remote.message.http.MediaTypeNotSupported.M.MEDIA_TYPE_NOT_SUPPORTED_KEY;

import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.AbstractResultMessageException;
import cloud.xcan.angus.api.enums.EventType;
import lombok.Getter;
import lombok.ToString;

/**
 * Exception thrown when a client POSTs, PUTs, or PATCHes content of a type not supported by request
 * handler, which will be handled by global exception and return http status 415.
 */
@Getter
@ToString
public class MediaTypeNotSupported extends AbstractResultMessageException {

  private final String code;
  private final String msg;
  private final Object[] args;

  public MediaTypeNotSupported() {
    this(MEDIA_TYPE_NOT_SUPPORTED, null, MEDIA_TYPE_NOT_SUPPORTED_KEY, IGNORABLE);
  }

  private MediaTypeNotSupported(String message, Object[] args, String eKey,
      ExceptionLevel level) {
    super(message, EventType.PROTOCOL, level, eKey);
    this.code = PROTOCOL_ERROR_CODE;
    this.msg = message;
    this.args = args;
  }

  public static MediaTypeNotSupported of(String message) {
    return new MediaTypeNotSupported(message, null, MEDIA_TYPE_NOT_SUPPORTED_KEY,
        IGNORABLE);
  }

  public static MediaTypeNotSupported of(ExceptionLevel level) {
    return new MediaTypeNotSupported(MEDIA_TYPE_NOT_SUPPORTED,
        null, MEDIA_TYPE_NOT_SUPPORTED_KEY, level);
  }

  public static MediaTypeNotSupported of(String message, String eKey) {
    return new MediaTypeNotSupported(message, null, eKey, IGNORABLE);
  }

  public static MediaTypeNotSupported of(String message, String eKey,
      ExceptionLevel level) {
    return new MediaTypeNotSupported(message, null, eKey, level);
  }

  public static MediaTypeNotSupported of(String message, Object[] agrs) {
    return new MediaTypeNotSupported(message, agrs, MEDIA_TYPE_NOT_SUPPORTED_KEY,
        IGNORABLE);
  }

  public static MediaTypeNotSupported of(String message, Object[] agrs, String eKey) {
    return new MediaTypeNotSupported(message, agrs, eKey, IGNORABLE);
  }

  public static MediaTypeNotSupported of(String message, ExceptionLevel level) {
    return new MediaTypeNotSupported(message, null, MEDIA_TYPE_NOT_SUPPORTED_KEY, level);
  }

  public static MediaTypeNotSupported of(String message, Object[] agrs, String eKey,
      ExceptionLevel level) {
    return new MediaTypeNotSupported(message, agrs, eKey, level);
  }

  @Override
  public boolean is4xxException() {
    return true;
  }

  public interface M {

    String MEDIA_TYPE_NOT_SUPPORTED = "xcm.mediaType.not.supported";
    String MEDIA_TYPE_NOT_SUPPORTED_T = "xcm.mediaType.not.supported.t";
    String MEDIA_TYPE_NOT_SUPPORTED_KEY = "media_type_not_supported";

  }

}
