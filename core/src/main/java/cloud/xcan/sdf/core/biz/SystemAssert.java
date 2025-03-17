package cloud.xcan.sdf.core.biz;

import static cloud.xcan.sdf.core.biz.exception.BizException.M.OPT_OBJ_IS_EMPTY;

import cloud.xcan.sdf.api.ExceptionLevel;
import cloud.xcan.sdf.api.message.CommSysException;
import cloud.xcan.sdf.api.message.ResultMessage;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class SystemAssert {

  public static void throw0(String message) {
    throw CommSysException.of(message);
  }

  public static void throw0(String message, Throwable cause) {
    throw CommSysException.of(message, cause);
  }

  public static void throw0(String message, String eKey) {
    throw CommSysException.of(message, eKey);
  }

  public static void throw0(String message, String eKey, Throwable cause) {
    throw CommSysException.of(message, eKey, cause);
  }

  public static void throw0(String message, Object[] args) {
    throw CommSysException.of(message, args);
  }

  public static void throw0(String message, Object[] args, Throwable cause) {
    throw CommSysException.of(message, args, cause);
  }

  public static void throw0(String message, String eKey, Object[] args) {
    throw CommSysException.of(message, eKey, args);
  }

  public static void throw0(String message, String eKey, Object[] args, Throwable cause) {
    throw CommSysException.of(message, eKey, args, cause);
  }


  public static <T> void assertNotEmpty(T object) {
    if (ObjectUtils.isEmpty(object)) {
      throw CommSysException.of(OPT_OBJ_IS_EMPTY);
    }
  }

  public static <T> void assertNotEmpty(T object, String message) {
    if (ObjectUtils.isEmpty(object)) {
      throw CommSysException.of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY);
    }
  }

  public static <T> void assertNotEmpty(T object, String message, Object[] args) {
    if (ObjectUtils.isEmpty(object)) {
      throw CommSysException
          .of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, args);
    }
  }

  public static <T> void assertNotEmpty(T object, String message, String eKey, Object[] args) {
    if (ObjectUtils.isEmpty(object)) {
      throw CommSysException
          .of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, eKey, args);
    }
  }

  public static <T> void assertNotEmpty(T object, ResultMessage message) {
    if (ObjectUtils.isEmpty(object)) {
      throw CommSysException.of(message.getCode(), message.getMsg(), message.getArgs());
    }
  }

  public static <T> void assertNotEmpty(T object, ResultMessage message, ExceptionLevel level) {
    if (ObjectUtils.isEmpty(object)) {
      throw CommSysException.of(message.getCode(), message.getMsg(), level);
    }
  }

  public static <T> void assertNotNull(T object) {
    if (object == null) {
      throw CommSysException.of(OPT_OBJ_IS_EMPTY);
    }
  }

  public static <T> void assertNotNull(T object, String message) {
    if (object == null) {
      throw CommSysException.of(message);
    }
  }

  public static <T> void assertNotNull(T object, String message, Object[] args) {
    if (object == null) {
      throw CommSysException.of(message, args);
    }
  }

  public static <T> void assertNotNull(T object, String message, ExceptionLevel level) {
    if (object == null) {
      throw CommSysException.of(message, level);
    }
  }

  public static <T> void assertNotNull(T object, String message, String eKey, Object[] args) {
    if (object == null) {
      throw CommSysException.of(message, eKey, args);
    }
  }

  public static <T> void assertNotNull(T object, String message, Object[] args,
      ExceptionLevel level) {
    if (object == null) {
      throw CommSysException.of(message, args, level);
    }
  }

  public static <T> void assertNotNull(T object, ResultMessage message) {
    if (object == null) {
      throw CommSysException.of(message.getCode(), message.getMsg());
    }
  }

  public static <T> void assertNotNull(T object, ResultMessage message,
      ExceptionLevel level) {
    if (object == null) {
      throw CommSysException.of(message.getCode(), message.getMsg(), level);
    }
  }

  public static void assertTrue(boolean expression, String message) {
    if (!expression) {
      throw CommSysException.of(message);
    }
  }

  public static void assertTrue(boolean expression, String message, Object[] args) {
    if (!expression) {
      throw CommSysException.of(message, args);
    }
  }

  public static void assertTrue(boolean expression, String message, ExceptionLevel level) {
    if (!expression) {
      throw CommSysException.of(message, level);
    }
  }

  public static void assertTrue(boolean expression, String message, String eKey) {
    if (!expression) {
      throw CommSysException.of(message, eKey);
    }
  }

  public static void assertTrue(boolean expression, String message, String eKey, Object[] args) {
    if (!expression) {
      throw CommSysException.of(message, eKey, args);
    }
  }

  public static void assertTrue(boolean expression, String message, Object[] args,
      ExceptionLevel level) {
    if (!expression) {
      throw CommSysException.of(message, args, level);
    }
  }

  public static void assertTrue(boolean expression, ResultMessage message) {
    if (!expression) {
      throw CommSysException.of(message.getCode(), message.getMsg());
    }
  }

  public static void assertTrue(boolean expression, ResultMessage message,
      ExceptionLevel level) {
    if (!expression) {
      throw CommSysException.of(message.getCode(), message.getMsg(), level);
    }
  }

}
