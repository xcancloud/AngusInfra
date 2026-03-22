package cloud.xcan.angus.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

public class CollectionCharLengthTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testValidCollection() {
    CollectionBean bean = new CollectionBean(Arrays.asList("ab", "cd"));
    Set<ConstraintViolation<CollectionBean>> violations = validator.validate(bean);
    assertEquals(0, violations.size());
  }

  @Test
  public void testExceedingLength() {
    CollectionBean bean = new CollectionBean(Arrays.asList("ab", "toolong"));
    Set<ConstraintViolation<CollectionBean>> violations = validator.validate(bean);
    assertEquals(1, violations.size());
  }

  @Test
  public void testNullCollection() {
    CollectionBean bean = new CollectionBean(null);
    Set<ConstraintViolation<CollectionBean>> violations = validator.validate(bean);
    assertEquals(0, violations.size());
  }

  @Test
  public void testCollectionWithNullElement() {
    ArrayList<String> list = new ArrayList<>();
    list.add("ab");
    list.add(null);
    list.add("cd");
    CollectionBean bean = new CollectionBean(list);
    Set<ConstraintViolation<CollectionBean>> violations = validator.validate(bean);
    assertEquals(0, violations.size());
  }

  @Test
  public void testEmptyCollection() {
    CollectionBean bean = new CollectionBean(new ArrayList<>());
    Set<ConstraintViolation<CollectionBean>> violations = validator.validate(bean);
    assertEquals(0, violations.size());
  }
}

@Data
@AllArgsConstructor
class CollectionBean {

  @CollectionCharLength(maxLength = 5)
  private Collection<String> items;
}
