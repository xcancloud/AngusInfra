package cloud.xcan.angus.plugin.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RestfulApiResultTest {

  @Test
  void constructorsAndFactories() {
    RestfulApiResult<String> a = new RestfulApiResult<>();
    assertTrue(a.isSuccess());
    assertEquals(RestfulApiResult.OK_CODE, a.getCode());

    RestfulApiResult<Integer> b = new RestfulApiResult<>(42);
    assertEquals(42, b.getData());

    RestfulApiResult<?> c = RestfulApiResult.success();
    assertTrue(c.isSuccess());

    RestfulApiResult<?> d = RestfulApiResult.success("msg");
    assertEquals("msg", d.getMessage());

    RestfulApiResult<String> e = RestfulApiResult.success("m", "d");
    assertEquals("d", e.getData());

    RestfulApiResult<?> f = RestfulApiResult.error();
    assertFalse(f.isSuccess());

    RestfulApiResult<?> g = RestfulApiResult.error("oops");
    assertEquals("oops", g.getMessage());

    RestfulApiResult<?> h = RestfulApiResult.error("E9", "m");
    assertEquals("E9", h.getCode());

    Map<String, Object> ext = new HashMap<>();
    ext.put("k", 1);
    RestfulApiResult<?> i = RestfulApiResult.error("E", "m", "d", ext);
    assertEquals(1, i.getExtensions().get("k"));
  }

  @Test
  void extHelpers() {
    RestfulApiResult<String> r = new RestfulApiResult<>("x");
    r.addExt(RestfulApiResult.EXT_EKEY_NAME, "ek");
    assertEquals("ek", r.getEKey());
    assertTrue(r.hasExt(RestfulApiResult.EXT_EKEY_NAME));

    r.addExtAll(Map.of("a", 1));
    assertEquals(1, r.getExtensions("a", Integer.class));
    assertNull(r.getExtensions("a", String.class));

    r.removeExt("a");
    assertFalse(r.hasExt("a"));
  }

  @Test
  void withDataAndMessage() {
    RestfulApiResult<String> r = new RestfulApiResult<>("d");
    RestfulApiResult<Integer> w = r.withData(3);
    assertEquals(3, w.getData());
    assertEquals(r.getCode(), w.getCode());

    RestfulApiResult<String> m = r.withMessage("nm");
    assertEquals("nm", m.getMessage());
  }

  @Test
  void validateAndNullCode() {
    assertThrows(NullPointerException.class,
        () -> new RestfulApiResult<>(null, "m", null, null));

    RestfulApiResult<?> r = RestfulApiResult.success();
    r.validate();

    RestfulApiResult<?> bad = new RestfulApiResult<>("", "x");
    assertThrows(IllegalStateException.class, bad::validate);
  }

  @Test
  void getExtensionsInitializesMap() {
    RestfulApiResult<?> r = new RestfulApiResult<>(RestfulApiResult.OK_CODE, "m", null, null);
    assertNotNull(r.getExtensions());
  }

  @Test
  void setExtensionsDefensiveCopy() {
    RestfulApiResult<?> r = RestfulApiResult.success();
    Map<String, Object> m = new HashMap<>();
    m.put("k", 1);
    r.setExtensions(m);
    m.put("k", 2);
    assertEquals(1, r.getExtensions().get("k"));
  }
}
