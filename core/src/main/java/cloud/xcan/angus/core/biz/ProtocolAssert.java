package cloud.xcan.angus.core.biz;

import static cloud.xcan.angus.core.biz.exception.BizException.M.OPT_OBJ_IS_EMPTY;
import static cloud.xcan.angus.remote.message.CommProtocolException.M.RESOURCE_ID_EMPTY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.remote.ExceptionLevel;
import cloud.xcan.angus.remote.message.CommProtocolException;
import cloud.xcan.angus.remote.message.ResultMessage;
import cloud.xcan.angus.remote.message.http.Forbidden;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.message.http.Unauthorized;
import cloud.xcan.angus.spec.experimental.Entity;
import cloud.xcan.angus.spec.utils.EnumUtils;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;

public class ProtocolAssert {

  public static void throw0(String message) {
    throw CommProtocolException.of(message);
  }

  public static void throw0(String message, Throwable cause) {
    throw CommProtocolException.of(message, cause);
  }

  public static void throw0(String message, String eKey) {
    throw CommProtocolException.of(message, eKey);
  }

  public static void throw0(String message, String eKey, Throwable cause) {
    throw CommProtocolException.of(message, eKey, cause);
  }

  public static void throw0(String message, Object[] args) {
    throw CommProtocolException.of(message, args);
  }

  public static void throw0(String message, Object[] args, Throwable cause) {
    throw CommProtocolException.of(message, args, cause);
  }

  public static void throw0(String message, String eKey, Object[] args) {
    throw CommProtocolException.of(message, eKey, args);
  }

  public static void throw0(String message, String eKey, Object[] args, Throwable cause) {
    throw CommProtocolException.of(message, eKey, args, cause);
  }

  public static void assertUnauthorized(boolean condition, String message) {
    if (!condition) {
      throw Unauthorized.of(message);
    }
  }

  public static void assertUnauthorized(boolean condition, String message, Throwable cause) {
    if (!condition) {
      throw Unauthorized.of(message, cause);
    }
  }

  public static void assertUnauthorized(boolean condition, String message, String eKey) {
    if (!condition) {
      throw Unauthorized.of(message, eKey);
    }
  }

  public static void assertUnauthorized(boolean condition, String message, String eKey,
      Throwable cause) {
    if (!condition) {
      throw Unauthorized.of(message, eKey, cause);
    }
  }

  public static void assertUnauthorized(boolean condition, String message, Object[] args) {
    if (!condition) {
      throw Unauthorized.of(message, args);
    }
  }

  public static void assertUnauthorized(boolean condition, String message, Object[] args,
      Throwable cause) {
    if (!condition) {
      throw Unauthorized.of(message, args, cause);
    }
  }

  public static void assertUnauthorized(boolean condition, String message, Object[] args,
      String eKey) {
    if (!condition) {
      throw Unauthorized.of(message, args, eKey);
    }
  }

  public static void assertUnauthorized(boolean condition, String message, Object[] args,
      String eKey, Throwable cause) {
    if (!condition) {
      throw Unauthorized.of(message, args, eKey, cause);
    }
  }

  public static <T> void assertUnauthorized(T resource, String message) {
    if (isEmpty(resource)) {
      throw Unauthorized.of(message);
    }
  }

  public static <T> void assertUnauthorized(T resource, String message, Throwable cause) {
    if (isEmpty(resource)) {
      throw Unauthorized.of(message, cause);
    }
  }

  public static <T> void assertUnauthorized(T resource, String message, String eKey) {
    if (isEmpty(resource)) {
      throw Unauthorized.of(message, eKey);
    }
  }

  public static <T> void assertUnauthorized(T resource, String message, String eKey,
      Throwable cause) {
    if (isEmpty(resource)) {
      throw Unauthorized.of(message, eKey, cause);
    }
  }

  public static <T> void assertUnauthorized(T resource, String message, Object[] args) {
    if (isEmpty(resource)) {
      throw Unauthorized.of(message, args);
    }
  }

  public static <T> void assertUnauthorized(T resource, String message, Object[] args,
      Throwable cause) {
    if (isEmpty(resource)) {
      throw Unauthorized.of(message, args, cause);
    }
  }

  public static <T> void assertUnauthorized(T resource, String message, Object[] args,
      String eKey) {
    if (isEmpty(resource)) {
      throw Unauthorized.of(message, args, eKey);
    }
  }

  public static <T> void assertUnauthorized(T resource, String message, Object[] args,
      String eKey, Throwable cause) {
    if (isEmpty(resource)) {
      throw Unauthorized.of(message, args, eKey, cause);
    }
  }

  public static void assertForbidden(boolean condition, String message) {
    if (!condition) {
      throw Forbidden.of(message);
    }
  }

  public static void assertForbidden(boolean condition, String message, Throwable cause) {
    if (!condition) {
      throw Forbidden.of(message, cause);
    }
  }

  public static void assertForbidden(boolean condition, String message, String eKey) {
    if (!condition) {
      throw Forbidden.of(message, eKey);
    }
  }

  public static void assertForbidden(boolean condition, String message, String eKey,
      Throwable cause) {
    if (!condition) {
      throw Forbidden.of(message, eKey, cause);
    }
  }

  public static void assertForbidden(boolean condition, String message, Object[] args) {
    if (!condition) {
      throw Forbidden.of(message, args);
    }
  }

  public static void assertForbidden(boolean condition, String message, Object[] args,
      Throwable cause) {
    if (!condition) {
      throw Forbidden.of(message, args, cause);
    }
  }

  public static void assertForbidden(boolean condition, String message, Object[] args,
      String eKey) {
    if (!condition) {
      throw Forbidden.of(message, args, eKey);
    }
  }

  public static void assertForbidden(boolean condition, String message, Object[] args,
      String eKey, Throwable cause) {
    if (!condition) {
      throw Forbidden.of(message, args, eKey, cause);
    }
  }

  public static <T> void assertForbidden(T resource, String message) {
    if (isEmpty(resource)) {
      throw Forbidden.of(message);
    }
  }

  public static <T> void assertForbidden(T resource, String message, Throwable cause) {
    if (isEmpty(resource)) {
      throw Forbidden.of(message, cause);
    }
  }

  public static <T> void assertForbidden(T resource, String message, String eKey) {
    if (isEmpty(resource)) {
      throw Forbidden.of(message, eKey);
    }
  }

  public static <T> void assertForbidden(T resource, String message, String eKey, Throwable cause) {
    if (isEmpty(resource)) {
      throw Forbidden.of(message, eKey, cause);
    }
  }

  public static <T> void assertForbidden(T resource, String message, Object[] args) {
    if (isEmpty(resource)) {
      throw Forbidden.of(message, args);
    }
  }

  public static <T> void assertForbidden(T resource, String message, Object[] args,
      Throwable cause) {
    if (isEmpty(resource)) {
      throw Forbidden.of(message, args, cause);
    }
  }

  public static <T> void assertForbidden(T resource, String message, Object[] args, String eKey) {
    if (isEmpty(resource)) {
      throw Forbidden.of(message, args, eKey);
    }
  }

  public static <T> void assertForbidden(T resource, String message, Object[] args, String eKey,
      Throwable cause) {
    if (isEmpty(resource)) {
      throw Forbidden.of(message, args, eKey, cause);
    }
  }

  public static void assertResourceNotFound(boolean condition, String id) {
    if (!condition) {
      throw ResourceNotFound.of(id);
    }
  }

  public static void assertResourceNotFound(boolean condition, String id, String name) {
    if (!condition) {
      throw ResourceNotFound.of(id, name);
    }
  }

  public static void assertResourceNotFound(boolean condition, Long id) {
    if (!condition) {
      throw ResourceNotFound.of(id);
    }
  }

  public static void assertResourceNotFound(boolean condition, Long id, String name) {
    if (!condition) {
      throw ResourceNotFound.of(id, name);
    }
  }

  public static void assertResourceNotFound(boolean condition, Collection<?> ids, String name) {
    if (!condition) {
      if (isEmpty(ids)) {
        throw ResourceNotFound.of(name);
      }
      Object id = ids.iterator().next();
      if (id instanceof Entity) {
        throw ResourceNotFound.of(((Entity) id).identity().toString(), name);
      }
      throw ResourceNotFound.of(id.toString(), name);
    }
  }

  public static void assertResourceNotFound(boolean condition, String message, Object[] args) {
    if (!condition) {
      throw ResourceNotFound.of(message, args);
    }
  }

  public static <T> void assertResourceNotFound(T resource, String id) {
    if (isEmpty(resource)) {
      throw ResourceNotFound.of(id);
    }
  }

  public static <T> void assertResourceNotFound(T resource, String id, String name) {
    if (isEmpty(resource)) {
      throw ResourceNotFound.of(id, name);
    }
  }

  public static <T> void assertResourceNotFound(T resource, String message, Object[] args) {
    if (isEmpty(resource)) {
      throw ResourceNotFound.of(message, args);
    }
  }

  public static <T> void assertResourceNotFound(T resource, Long id) {
    if (isEmpty(resource)) {
      throw ResourceNotFound.of(id);
    }
  }

  public static <T> void assertResourceNotFound(T resource, Long id, String name) {
    if (isEmpty(resource)) {
      throw ResourceNotFound.of(id, name);
    }
  }

  public static void assertResourceExisted(boolean condition, String id, String name) {
    if (!condition) {
      throw ResourceExisted.of(id, name);
    }
  }

  public static void assertResourceExisted(boolean condition, Long id) {
    if (!condition) {
      throw ResourceExisted.of(id);
    }
  }

  public static void assertResourceExisted(boolean condition, String id) {
    if (!condition) {
      throw ResourceExisted.of(id);
    }
  }

  public static void assertResourceExisted(boolean condition, Long id, String name) {
    if (!condition) {
      throw ResourceExisted.of(id, name);
    }
  }

  public static void assertResourceExisted(boolean condition, Collection<?> ids, String name) {
    if (!condition) {
      if (isEmpty(ids)) {
        throw ResourceExisted.of(name);
      }
      Object id = ids.iterator().next();
      if (id instanceof Entity) {
        throw ResourceExisted.of(((Entity) id).identity().toString(), name);
      }
      throw ResourceExisted.of(id.toString(), name);
    }
  }

  public static void assertResourceExisted(boolean condition, String message, Object[] args) {
    if (!condition) {
      throw ResourceExisted.of(message, args);
    }
  }

  public static <T> void assertResourceExisted(T resource, String id) {
    if (ObjectUtils.isNotEmpty(resource)) {
      throw ResourceExisted.of(id);
    }
  }

  public static <T> void assertResourceExisted(T resource, String id, String name) {
    if (ObjectUtils.isNotEmpty(resource)) {
      throw ResourceExisted.of(id, name);
    }
  }

  public static <T> void assertResourceExisted(T resource, Long id) {
    if (ObjectUtils.isNotEmpty(resource)) {
      throw ResourceExisted.of(id);
    }
  }

  public static <T> void assertResourceExisted(T resource, Long id, String name) {
    if (ObjectUtils.isNotEmpty(resource)) {
      throw ResourceExisted.of(id, name);
    }
  }

  public static <T> void assertResourceExisted(T resource, String message, Object[] args) {
    if (ObjectUtils.isNotEmpty(resource)) {
      throw ResourceExisted.of(message, args);
    }
  }

  public static <ID> void assertIdNotEmpty(ID id) {
    if (id == null || (id instanceof String && StringUtils.isEmpty((String) id))) {
      throw CommProtocolException.of(RESOURCE_ID_EMPTY);
    }
  }

  public static <T> void assertNotEmpty(T object) {
    if (isEmpty(object)) {
      throw CommProtocolException.of(OPT_OBJ_IS_EMPTY);
    }
  }

  public static <T> void assertNotEmpty(T object, String message) {
    if (isEmpty(object)) {
      throw CommProtocolException.of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY);
    }
  }

  public static <T> void assertNotEmpty(T object, String message, Object[] args) {
    if (isEmpty(object)) {
      throw CommProtocolException
          .of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, args);
    }
  }

  public static <T> void assertNotEmpty(T object, String message, String eKey, Object[] args) {
    if (isEmpty(object)) {
      throw CommProtocolException
          .of(StringUtils.isNotEmpty(message) ? message : OPT_OBJ_IS_EMPTY, eKey, args);
    }
  }

  public static <T> void assertNotEmpty(T object, ResultMessage message) {
    if (isEmpty(object)) {
      throw CommProtocolException.of(message.getCode(), message.getMsg(), message.getArgs());
    }
  }

  public static <T> void assertNotEmpty(T object, ResultMessage message, ExceptionLevel level) {
    if (isEmpty(object)) {
      throw CommProtocolException.of(message.getCode(), message.getMsg(), level);
    }
  }

  public static <T> void assertNotNull(T object) {
    if (object == null) {
      throw CommProtocolException.of(OPT_OBJ_IS_EMPTY);
    }
  }

  public static <T> void assertNotNull(T object, String message) {
    if (object == null) {
      throw CommProtocolException.of(message);
    }
  }

  public static <T> void assertNotNull(T object, String message, Object[] args) {
    if (object == null) {
      throw CommProtocolException.of(message, args);
    }
  }

  public static <T> void assertNotNull(T object, String message, ExceptionLevel level) {
    if (object == null) {
      throw CommProtocolException.of(message, level);
    }
  }

  public static <T> void assertNotNull(T object, String message, String eKey, Object[] args) {
    if (object == null) {
      throw CommProtocolException.of(message, eKey, args);
    }
  }

  public static <T> void assertNotNull(T object, String message, Object[] args,
      ExceptionLevel level) {
    if (object == null) {
      throw CommProtocolException.of(message, args, level);
    }
  }

  public static <T> void assertNotNull(T object, ResultMessage message) {
    if (object == null) {
      throw CommProtocolException.of(message.getCode(), message.getMsg());
    }
  }

  public static <T> void assertNotNull(T object, ResultMessage message,
      ExceptionLevel level) {
    if (object == null) {
      throw CommProtocolException.of(message.getCode(), message.getMsg(), level);
    }
  }

  public static void assertTrue(boolean expression, String message) {
    if (!expression) {
      throw CommProtocolException.of(message);
    }
  }

  public static void assertTrue(boolean expression, String message, Object[] args) {
    if (!expression) {
      throw CommProtocolException.of(message, args);
    }
  }

  public static void assertTrue(boolean expression, String message, ExceptionLevel level) {
    if (!expression) {
      throw CommProtocolException.of(message, level);
    }
  }

  public static void assertTrue(boolean expression, String message, String eKey) {
    if (!expression) {
      throw CommProtocolException.of(message, eKey);
    }
  }

  public static void assertTrue(boolean expression, String message, String eKey, Object[] args) {
    if (!expression) {
      throw CommProtocolException.of(message, eKey, args);
    }
  }

  public static void assertTrue(boolean expression, String message, Object[] args,
      ExceptionLevel level) {
    if (!expression) {
      throw CommProtocolException.of(message, args, level);
    }
  }

  public static void assertTrue(boolean expression, ResultMessage message) {
    if (!expression) {
      throw CommProtocolException.of(message.getCode(), message.getMsg());
    }
  }

  public static void assertTrue(boolean expression, ResultMessage message, ExceptionLevel level) {
    if (!expression) {
      throw CommProtocolException.of(message.getCode(), message.getMsg(), level);
    }
  }

  public static <T extends Enum<T>> T assertEnumOf(String value, Class<T> clz) {
    try {
      return EnumUtils.valueOf(clz, value);
    } catch (Exception e) {
      throw CommProtocolException.of(e.getMessage());
    }
  }

}
