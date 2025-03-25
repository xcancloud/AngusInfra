package cloud.xcan.angus.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.junit.Test;

public class StartsWithTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    StartsWithBeanFields startsWithBeanFields = new StartsWithBeanFields();

    Set<ConstraintViolation<StartsWithBeanFields>> violations =
        validator.validate(startsWithBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    StartsWithBeanMethods startsWithBeanMethods = new StartsWithBeanMethods();

    Set<ConstraintViolation<StartsWithBeanMethods>> violations =
        validator.validate(startsWithBeanMethods);

    assertEquals(1, violations.size());
  }

  @Test
  public void testType() {
    StartsWithBeanType startsWithBeanType = new StartsWithBeanType();

    Set<ConstraintViolation<StartsWithBeanType>> violations =
        validator.validate(startsWithBeanType);

    assertEquals(0, violations.size());
  }

}

@Data
class StartsWithBeanFields {

  @StartsWith("A")
  private String correct = "ABC";

  @StartsWith("B")
  private String incorrect = "ABC";
}

@Data
class StartsWithBeanMethods {

  @StartsWith("A")
  private String getCorrect() {
    return "ABC";
  }

  @StartsWith("B")
  private String getIncorrect() {
    return "ABC";
  }
}

class StartsWithBeanType {

  private List<@StartsWith("A") String> strs = Arrays.asList("ABC", "AND");
}
