package cloud.xcan.angus.cache.web;

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
  void successFactories() {
    assertTrue(RestfulApiResult.success().isSuccess());
    assertEquals("ok", RestfulApiResult.success("ok").getMessage());
    assertEquals(1, RestfulApiResult.success("m", 1).getData());
  }

  @Test
  void errorFactories() {
    RestfulApiResult<?> e = RestfulApiResult.error();
    assertFalse(e.isSuccess());
    assertEquals(RestfulApiResult.BUSINESS_ERROR_CODE, e.getCode());
    RestfulApiResult<?> e2 = RestfulApiResult.error("x", "y", null,
        Map.of(RestfulApiResult.EXT_EKEY_NAME, "ek"));
    assertEquals("ek", e2.getEKey());
  }

  @Test
  void fullConstructor_rejectsNullCode() {
    assertThrows(NullPointerException.class,
        () -> new RestfulApiResult<>(null, "m", "d", null));
  }

  @Test
  void extHelpers() {
    RestfulApiResult<String> r = new RestfulApiResult<>("S", "m", "d");
    r.addExt("a", 1);
    assertTrue(r.hasExt("a"));
    assertEquals(1, r.getExtensions("a", Integer.class));
    assertNull(r.getExtensions("a", Double.class));
    r.addExtAll(Map.of("b", 2));
    r.removeExt("a");
    assertFalse(r.hasExt("a"));
    RestfulApiResult<Integer> copy = r.withData(9);
    assertEquals(9, copy.getData());
    RestfulApiResult<String> msg = r.withMessage("nm");
    assertEquals("nm", msg.getMessage());
  }

  @Test
  void getExtensions_initializesMap() {
    RestfulApiResult<Void> r = new RestfulApiResult<>();
    assertNotNull(r.getExtensions());
    r.getExtensions().put("k", "v");
    assertTrue(r.hasExt("k"));
  }

  @Test
  void setExtensions_defensiveCopy() {
    RestfulApiResult<Void> r = new RestfulApiResult<>();
    Map<String, Object> m = Map.of("x", 1);
    r.setExtensions(m);
    assertTrue(r.hasExt("x"));
  }

  @Test
  void validate_success() {
    RestfulApiResult<Void> r = new RestfulApiResult<>("S", "m");
    r.validate();
  }

  @Test
  void validate_failsOnBlankCode() {
    RestfulApiResult<Void> r = new RestfulApiResult<>("S", "m");
    r.setCode("  ");
    assertThrows(IllegalStateException.class, r::validate);
  }

  @Test
  void validate_failsOnNullTimestamp() {
    RestfulApiResult<Void> r = new RestfulApiResult<>("S", "m");
    r.setTimestamp(null);
    assertThrows(IllegalStateException.class, r::validate);
  }
}
