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

public class LowerCaseTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    LowerCaseBeanFields lowerCaseBeanFields = new LowerCaseBeanFields();

    Set<ConstraintViolation<LowerCaseBeanFields>> violations =
        validator.validate(lowerCaseBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    LowerCaseBeanMethods lowerCaseBeanMethods = new LowerCaseBeanMethods();

    Set<ConstraintViolation<LowerCaseBeanMethods>> violations =
        validator.validate(lowerCaseBeanMethods);

    assertEquals(1, violations.size());
  }

  @Test
  public void testType() {
    LowerCaseBeanType lowerCaseBeanType = new LowerCaseBeanType();

    Set<ConstraintViolation<LowerCaseBeanType>> violations =
        validator.validate(lowerCaseBeanType);

    assertEquals(0, violations.size());
  }
}

@Data
class LowerCaseBeanFields {

  @LowerCase
  private String correct = "abc";

  @LowerCase
  private String incorrect = "Abc";
}

@Data
class LowerCaseBeanMethods {

  @LowerCase
  private String getCorrect() {
    return "abc";
  }

  @LowerCase
  private String getIncorrect() {
    return "Abc";
  }

}

@Data
class LowerCaseBeanType {

  private List<@LowerCase String> sts = Arrays.asList("abc", "axxx");
}

