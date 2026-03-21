package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.core.biz.exception.BizException.M.OPT_OBJ_IS_EMPTY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.ResultMessage;
import cloud.xcan.angus.remote.message.SysException;
import org.apache.commons.lang3.StringUtils;

public class SystemAssert {

  public static void throw0(String message) {
    throw SysException.of(message);
  }

  public static void throw0(String message, Throwable cause) {
    throw SysException.of(message, cause);
  }

  public static void throw0(String message, String eKey) {
    throw SysException.of(message, eKey);
  }

  public static void throw0(String message, String eKey, Throwable cause) {
    throw SysException.of(message, eKey, cause);
  }

  public static void throw0(String message, Object[] args) {
    throw SysException.of(message, args);
  }

  public static void throw0(String message, Object[] args, Throwable cause) {
    throw SysException.of(message, args, cause);
  }

  public static void throw0(String message, String eKey, Object[] args) {
    throw SysException.of(message, eKey, args);
  }

  public static void throw0(String message, String eKey, Object[] args, Throwable cause) {
    throw SysException.of(message, eKey, args, cause);
  }


  public static <T> void assertNotEmpty(T object) {
    if (isEmpty(object)) {
      throw SysException.of(OPT_OBJ_IS_EMPTY);
    }
  }

  public static <T> void assertNotEmpty(T object, String message) {
    if (isEmpty(object)) {
      throw SysException.of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY);
    }
  }

  public static <T> void assertNotEmpty(T object, String message, Object[] args) {
    if (isEmpty(object)) {
      throw SysException
          .of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, args);
    }
  }

  public static <T> void assertNotEmpty(T object, String message, String eKey, Object[] args) {
    if (isEmpty(object)) {
      throw SysException
          .of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, eKey, args);
    }
  }

  public static <T> void assertNotEmpty(T object, ResultMessage message) {
    if (isEmpty(object)) {
      throw SysException.of(message.getCode(), message.getMsg(), message.getArgs());
    }
  }

  public static <T> void assertNotEmpty(T object, ResultMessage message, ExceptionLevel level) {
    if (isEmpty(object)) {
      throw SysException.of(message.getCode(), message.getMsg(), level);
    }
  }

  public static <T> void assertNotNull(T object) {
    if (object == null) {
      throw SysException.of(OPT_OBJ_IS_EMPTY);
    }
  }

  public static <T> void assertNotNull(T object, String message) {
    if (object == null) {
      throw SysException.of(message);
    }
  }

  public static <T> void assertNotNull(T object, String message, Object[] args) {
    if (object == null) {
      throw SysException.of(message, args);
    }
  }

  public static <T> void assertNotNull(T object, String message, ExceptionLevel level) {
    if (object == null) {
      throw SysException.of(message, level);
    }
  }

  public static <T> void assertNotNull(T object, String message, String eKey, Object[] args) {
    if (object == null) {
      throw SysException.of(message, eKey, args);
    }
  }

  public static <T> void assertNotNull(T object, String message, Object[] args,
      ExceptionLevel level) {
    if (object == null) {
      throw SysException.of(message, args, level);
    }
  }

  public static <T> void assertNotNull(T object, ResultMessage message) {
    if (object == null) {
      throw SysException.of(message.getCode(), message.getMsg());
    }
  }

  public static <T> void assertNotNull(T object, ResultMessage message,
      ExceptionLevel level) {
    if (object == null) {
      throw SysException.of(message.getCode(), message.getMsg(), level);
    }
  }

  public static void assertTrue(boolean expression, String message) {
    if (!expression) {
      throw SysException.of(message);
    }
  }

  public static void assertTrue(boolean expression, String message, Object[] args) {
    if (!expression) {
      throw SysException.of(message, args);
    }
  }

  public static void assertTrue(boolean expression, String message, ExceptionLevel level) {
    if (!expression) {
      throw SysException.of(message, level);
    }
  }

  public static void assertTrue(boolean expression, String message, String eKey) {
    if (!expression) {
      throw SysException.of(message, eKey);
    }
  }

  public static void assertTrue(boolean expression, String message, String eKey, Object[] args) {
    if (!expression) {
      throw SysException.of(message, eKey, args);
    }
  }

  public static void assertTrue(boolean expression, String message, Object[] args,
      ExceptionLevel level) {
    if (!expression) {
      throw SysException.of(message, args, level);
    }
  }

  public static void assertTrue(boolean expression, ResultMessage message) {
    if (!expression) {
      throw SysException.of(message.getCode(), message.getMsg());
    }
  }

  public static void assertTrue(boolean expression, ResultMessage message,
      ExceptionLevel level) {
    if (!expression) {
      throw SysException.of(message.getCode(), message.getMsg(), level);
    }
  }

}
