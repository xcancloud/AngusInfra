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

public class EndsWithTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    EndsWithBeanFields endsWithBeanFields = new EndsWithBeanFields();

    Set<ConstraintViolation<EndsWithBeanFields>> violations =
        validator.validate(endsWithBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    EndsWithBeanMethods endsWithBeanMethods = new EndsWithBeanMethods();

    Set<ConstraintViolation<EndsWithBeanMethods>> violations =
        validator.validate(endsWithBeanMethods);

    assertEquals(1, violations.size());
  }

  @Test
  public void testType() {
    EndsWithBeanType endsWithBeanType = new EndsWithBeanType();

    Set<ConstraintViolation<EndsWithBeanType>> violations =
        validator.validate(endsWithBeanType);

    assertEquals(0, violations.size());
  }
}


@Data
class EndsWithBeanFields {

  @EndsWith("abc")
  private String correct = "abc";

  @EndsWith("abc")
  private String incorrect = "abcd";
}

@Data
class EndsWithBeanMethods {

  @EndsWith("abc")
  private String getCorrect() {
    return "abc";
  }

  @EndsWith("abc")
  private String getIncorrect() {
    return "abcd";
  }
}

@Data
class EndsWithBeanType {

  private List<@EndsWith("abc") String> getStrs = Arrays.asList("abc", "1abc", "0abc");
}
