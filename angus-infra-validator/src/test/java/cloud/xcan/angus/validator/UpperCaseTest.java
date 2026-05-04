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

public class UpperCaseTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    UpperCaseBeanFields upperCaseBeanFields = new UpperCaseBeanFields();

    Set<ConstraintViolation<UpperCaseBeanFields>> violations =
        validator.validate(upperCaseBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    UpperCaseBeanMethods upperCaseBeanMethods = new UpperCaseBeanMethods();

    Set<ConstraintViolation<UpperCaseBeanMethods>> violations =
        validator.validate(upperCaseBeanMethods);

    assertEquals(1, violations.size());
  }

  @Test
  public void testType() {
    UpperCaseBeanType upperCaseBeanType = new UpperCaseBeanType();

    Set<ConstraintViolation<UpperCaseBeanType>> violations =
        validator.validate(upperCaseBeanType);

    assertEquals(0, violations.size());
  }
}

@Data
class UpperCaseBeanFields {

  @UpperCase
  private String correct = "ABC";

  @UpperCase
  private String incorrect = "aBC";
}

@Data
class UpperCaseBeanMethods {

  @UpperCase
  private String getCorrect() {
    return "ABC";
  }

  @UpperCase
  private String getIncorrect() {
    return "aBC";
  }
}

@Data
class UpperCaseBeanType {

  private List<@UpperCase String> sts = Arrays.asList("ABC", "AXXX");
}



