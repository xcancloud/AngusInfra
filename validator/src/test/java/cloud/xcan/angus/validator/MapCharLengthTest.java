package cloud.xcan.angus.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

public class MapCharLengthTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testValidMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("key", "value");
    MapBean bean = new MapBean(map);
    Set<ConstraintViolation<MapBean>> violations = validator.validate(bean);
    assertEquals(0, violations.size());
  }

  @Test
  public void testKeyTooLong() {
    Map<String, Object> map = new HashMap<>();
    map.put("a-very-long-key-that-exceeds-max-length", "value");
    MapBean bean = new MapBean(map);
    Set<ConstraintViolation<MapBean>> violations = validator.validate(bean);
    assertEquals(1, violations.size());
  }

  @Test
  public void testValueTooLong() {
    Map<String, Object> map = new HashMap<>();
    map.put("key", "a-very-long-value-that-exceeds-max-length");
    MapBean bean = new MapBean(map);
    Set<ConstraintViolation<MapBean>> violations = validator.validate(bean);
    assertEquals(1, violations.size());
  }

  @Test
  public void testNullMap() {
    MapBean bean = new MapBean(null);
    Set<ConstraintViolation<MapBean>> violations = validator.validate(bean);
    assertEquals(0, violations.size());
  }

  @Test
  public void testMapWithNullValue() {
    Map<String, Object> map = new HashMap<>();
    map.put("key", null);
    map.put("key2", "val");
    MapBean bean = new MapBean(map);
    Set<ConstraintViolation<MapBean>> violations = validator.validate(bean);
    assertEquals(0, violations.size());
  }

  @Test
  public void testEmptyMap() {
    MapBean bean = new MapBean(new HashMap<>());
    Set<ConstraintViolation<MapBean>> violations = validator.validate(bean);
    assertEquals(0, violations.size());
  }
}

@Data
@AllArgsConstructor
class MapBean {

  @MapCharLength(keyMaxLength = 10, valueMaxLength = 20)
  private Map<String, Object> data;
}
