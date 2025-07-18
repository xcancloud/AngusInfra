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

public class BlankTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    BlankBeanFields blankBeanFields = new BlankBeanFields();

    Set<ConstraintViolation<BlankBeanFields>> violations =
        validator.validate(blankBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    BlankBeanMethods blankBeanMethods = new BlankBeanMethods();

    Set<ConstraintViolation<BlankBeanMethods>> violations =
        validator.validate(blankBeanMethods);

    assertEquals(1, violations.size());
  }

  @Test
  public void testType() {
    BlankBeanType blankBeanType = new BlankBeanType();

    Set<ConstraintViolation<BlankBeanType>> violations =
        validator.validate(blankBeanType);

    assertEquals(1, violations.size());
  }
}

@Data
class BlankBeanFields {

  @Blank
  private String strBlank = "";

  @Blank
  private String strSpaces = "   ";

  @Blank
  private String strNull = null;

  @Blank
  private String strNotBlank = "abc";
}

@Data
class BlankBeanMethods {

  @Blank
  private String getStrBlank() {
    return "";
  }

  @Blank
  private String getStrSpaces() {
    return "   ";
  }

  @Blank
  private String getStrNull() {
    return null;
  }

  @Blank
  private String getStrNotBlank() {
    return "abc";
  }
}

@Data
class BlankBeanType {

  private List<@Blank String> list = Arrays.asList("", "   ", null, "abc");

}
