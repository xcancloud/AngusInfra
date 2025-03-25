package cloud.xcan.angus.core.biz;


import static cloud.xcan.angus.core.biz.exception.BizException.M.OPT_OBJ_IS_EMPTY;

import cloud.xcan.angus.core.biz.exception.BizException;
import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.ResultMessage;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class BizAssert {

  public static <T> T assertNotEmpty(T object) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(OPT_OBJ_IS_EMPTY);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, Throwable cause) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(OPT_OBJ_IS_EMPTY, cause);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, String message) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, String message, Throwable cause) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, cause);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, String message, Object[] args) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, args);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, String message, Object[] args, Throwable cause) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException
          .of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, args, cause);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, String code, String message) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(code, StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, String code, String message, Throwable cause) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException
          .of(code, StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, cause);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, String code, String message, Object[] args) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException
          .of(code, StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, args);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, ResultMessage resultMessage) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(resultMessage.getCode(), resultMessage.getMsg());
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, ResultMessage resultMessage,
      Throwable throwable) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(resultMessage.getCode(), resultMessage.getMsg(), throwable);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, ResultMessage resultMessage,
      ExceptionLevel level) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(resultMessage.getCode(), resultMessage.getMsg(), level);
    }
    return object;
  }

  public static <T> T assertNotEmpty(T object, ResultMessage resultMessage,
      ExceptionLevel level, Throwable cause) {
    if (ObjectUtils.isEmpty(object)) {
      throw BizException.of(resultMessage.getCode(), resultMessage.getMsg(), level, cause);
    }
    return object;
  }

  public static <T> T assertNotNull(T object) {
    if (object == null) {
      throw BizException.of(OPT_OBJ_IS_EMPTY);
    }
    return object;
  }

  public static <T> T assertNotNull(T object, String message) {
    if (object == null) {
      throw BizException.of(message);
    }
    return object;
  }

  public static <T> T assertNotNull(T object, String message, Object[] args) {
    if (object == null) {
      throw BizException.of(message, args);
    }
    return object;
  }

  public static <T> T assertNotNull(T object, String code, String message) {
    if (object == null) {
      throw BizException.of(code, message);
    }
    return object;
  }

  public static <T> T assertNotNull(T object, String code, String message, Object[] args) {
    if (object == null) {
      throw BizException.of(message, code, args);
    }
    return object;
  }

  public static <T> T assertNotNull(T object, String message, ExceptionLevel level) {
    if (object == null) {
      throw BizException.of(message, level);
    }
    return object;
  }

  public static <T> T assertNotNull(T object, String message, Object[] args,
      ExceptionLevel level) {
    if (object == null) {
      throw BizException.of(message, args, level);
    }
    return object;
  }

  public static <T> T assertNotNull(T object, String code, String message, Object[] args,
      ExceptionLevel level) {
    if (object == null) {
      throw BizException.of(code, message, args, level);
    }
    return object;
  }

  public static <T> T assertNotNull(T object, ResultMessage resultMessage) {
    if (object == null) {
      throw BizException.of(resultMessage.getCode(), resultMessage.getMsg());
    }
    return object;
  }

  public static <T> T assertNotNull(T object, ResultMessage resultMessage,
      ExceptionLevel level) {
    if (object == null) {
      throw BizException.of(resultMessage.getCode(), resultMessage.getMsg(), level);
    }
    return object;
  }

  public static void assertTrue(boolean expression, String message) {
    if (!expression) {
      throw BizException.of(message);
    }
  }

  public static void assertTrue(boolean expression, String message, Object[] args) {
    if (!expression) {
      throw BizException.of(message, args);
    }
  }

  public static void assertTrue(boolean expression, String code, String message) {
    if (!expression) {
      throw BizException.of(code, message);
    }
  }

  public static void assertTrue(boolean expression, String code, String message, Object[] args) {
    if (!expression) {
      throw BizException.of(code, message, args);
    }
  }

  public static void assertTrue(boolean expression, String message, ExceptionLevel level) {
    if (!expression) {
      throw BizException.of(message, level);
    }
  }

  public static void assertTrue(boolean expression, String message, Object[] args,
      ExceptionLevel level) {
    if (!expression) {
      throw BizException.of(message, args, level);
    }
  }

  public static void assertTrue(boolean expression, String code, String message, Object[] args,
      ExceptionLevel level) {
    if (!expression) {
      throw BizException.of(code, message, args, level);
    }
  }

  public static void assertTrue(boolean expression, ResultMessage resultMessage) {
    if (!expression) {
      throw BizException.of(resultMessage.getCode(), resultMessage.getMsg());
    }
  }

  public static void assertTrue(boolean expression, ResultMessage resultMessage,
      ExceptionLevel level) {
    if (!expression) {
      throw BizException.of(resultMessage.getCode(), resultMessage.getMsg(), level);
    }
  }

}
