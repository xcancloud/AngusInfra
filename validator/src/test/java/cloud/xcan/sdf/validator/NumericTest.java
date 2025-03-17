package cloud.xcan.sdf.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.junit.Test;

public class NumericTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    NumericBeanFields numericBeanFields = new NumericBeanFields();

    Set<ConstraintViolation<NumericBeanFields>> violations =
        validator.validate(numericBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    NumericBeanMethods numericBeanMethods = new NumericBeanMethods();

    Set<ConstraintViolation<NumericBeanMethods>> violations =
        validator.validate(numericBeanMethods);

    assertEquals(1, violations.size());
  }

  @Test
  public void testType() {
    NumericBeanType numericBeanType = new NumericBeanType();

    Set<ConstraintViolation<NumericBeanType>> violations =
        validator.validate(numericBeanType);

    assertEquals(0, violations.size());
  }
}


@Data
class NumericBeanFields {

  @Numeric
  private String correct = "123";

  @Numeric
  private String incorrect = "a123";
}

@Data
class NumericBeanMethods {

  @Numeric
  private String getCorrect() {
    return "123";
  }

  @Numeric
  private String getIncorrect() {
    return "a123";
  }
}

@Data
class NumericBeanType {

  private List<@Numeric String> nums = Arrays.asList("123", "0");
}
