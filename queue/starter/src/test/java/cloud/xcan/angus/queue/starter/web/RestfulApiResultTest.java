package cloud.xcan.angus.queue.starter.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class RestfulApiResultTest {

  @Test
  void successFactoriesAndSuccessFlag() {
    RestfulApiResult<String> r =
        new RestfulApiResult<>(RestfulApiResult.OK_CODE, RestfulApiResult.OK_MSG, "x", null);
    assertTrue(r.isSuccess());
    assertEquals("x", r.getData());
    assertNotNull(r.getTimestamp());

    RestfulApiResult<?> empty = RestfulApiResult.success();
    assertTrue(empty.isSuccess());

    RestfulApiResult<?> msgOnly = RestfulApiResult.success("ok");
    assertEquals("ok", msgOnly.getMessage());
  }

  @Test
  void errorFactories() {
    RestfulApiResult<?> e1 = RestfulApiResult.error();
    assertFalse(e1.isSuccess());
    RestfulApiResult<?> e2 = RestfulApiResult.error("bad");
    assertEquals("bad", e2.getMessage());
    RestfulApiResult<Integer> e3 = RestfulApiResult.error("E9", "m", 1);
    assertEquals("E9", e3.getCode());
    assertEquals(Integer.valueOf(1), e3.getData());
  }

  @Test
  void extHelpers() {
    RestfulApiResult<String> r =
        new RestfulApiResult<>(RestfulApiResult.OK_CODE, RestfulApiResult.OK_MSG, "d", null);
    r.addExt("a", 1);
    assertTrue(r.hasExt("a"));
    assertEquals(Integer.valueOf(1), r.getExtensions("a", Integer.class));
    r.addExtAll(Map.of("b", "v"));
    r.removeExt("a");
    assertFalse(r.hasExt("a"));
    r.setExtensions(null);
    assertTrue(r.getExtensions().isEmpty());
  }

  @Test
  void getEKeyReadsExt() {
    RestfulApiResult<?> r = RestfulApiResult.error("E1", "m", null,
        Map.of(RestfulApiResult.EXT_EKEY_NAME, "k1"));
    assertEquals("k1", r.getEKey());
  }

  @Test
  void withDataAndWithMessagePreserveCode() {
    RestfulApiResult<Integer> r = RestfulApiResult.success(1);
    RestfulApiResult<String> r2 = r.withData("z");
    assertEquals(r.getCode(), r2.getCode());
    RestfulApiResult<Integer> r3 = r.withMessage("nm");
    assertEquals("nm", r3.getMessage());
  }

  @Test
  void validateEnforcesCodeAndTimestamp() {
    RestfulApiResult<String> ok =
        new RestfulApiResult<>(RestfulApiResult.OK_CODE, RestfulApiResult.OK_MSG, "x", null);
    ok.validate();

    RestfulApiResult<String> badCode = new RestfulApiResult<>("  ", "m", "d", null);
    assertThrows(IllegalStateException.class, badCode::validate);

    RestfulApiResult<String> badTs = new RestfulApiResult<>(RestfulApiResult.OK_CODE, "m", "d", null);
    badTs.setTimestamp(null);
    assertThrows(IllegalStateException.class, badTs::validate);
  }

  @Test
  void constructorRejectsNullCode() {
    assertThrows(NullPointerException.class,
        () -> new RestfulApiResult<String>(null, "m", "d", null));
  }

  @Test
  void getEKeyWhenExtMissing() {
    RestfulApiResult<?> r = RestfulApiResult.success();
    assertNull(r.getEKey());
  }
}
