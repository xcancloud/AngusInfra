package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.experimental.Value;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;

@SuppressWarnings("rawtypes")
class ReflectionTest {

  @Test
  void testTypesAnnotatedWith() {
    Reflections reflections = new Reflections("cloud.xcan");
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(EndpointRegister.class);
    assertFalse(classes.isEmpty(),
        () -> "Expected at least one @EndpointRegister type under package cloud.xcan on test classpath");
  }

  @Test
  void testSubTypesOf() {
    Reflections reflections = new Reflections("cloud.xcan");
    Set<Class<? extends Value>> classes = reflections.getSubTypesOf(Value.class);
    assertFalse(classes.isEmpty(),
        () -> "No subtypes of Value under package cloud.xcan (classpath / reflections scan)");
  }
}