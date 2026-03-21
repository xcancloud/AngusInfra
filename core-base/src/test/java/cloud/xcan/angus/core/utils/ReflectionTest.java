package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.experimental.Value;
import java.util.Set;
import org.junit.Test;
import org.reflections.Reflections;

public class ReflectionTest {

  @Test
  public void testTypesAnnotatedWith() {
    Reflections reflections = new Reflections("cloud.xcan");
    Set<Class<?>> classes = reflections.getTypesAnnotatedWith(EndpointRegister.class);
    assertFalse(classes.isEmpty());
  }

  @Test
  public void testSubTypesOf() {
    Reflections reflections = new Reflections("cloud.xcan");
    Set<Class<? extends Value>> classes = reflections
        .getSubTypesOf(Value.class);
    assertFalse(classes.isEmpty());
  }

}
