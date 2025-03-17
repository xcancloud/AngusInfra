package cloud.xcan.sdf.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

public class OneOfStringsTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    OneOfStringsBeanFields oneOfStringsBeanFields = new OneOfStringsBeanFields("A");

    Set<ConstraintViolation<OneOfStringsBeanFields>> violations =
        validator.validate(oneOfStringsBeanFields);

    assertEquals(0, violations.size());
  }

  @Test
  public void testFieldsIncorrect() {
    OneOfStringsBeanFields oneOfStringsBeanFields = new OneOfStringsBeanFields("Z");

    Set<ConstraintViolation<OneOfStringsBeanFields>> violations =
        validator.validate(oneOfStringsBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testFieldsWithNull() {
    OneOfStringsBeanFields oneOfStringsBeanFields = new OneOfStringsBeanFields(null);

    Set<ConstraintViolation<OneOfStringsBeanFields>> violations =
        validator.validate(oneOfStringsBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    OneOfStringsBeanMethods oneOfStringsBeanMethods = new OneOfStringsBeanMethods("A");

    Set<ConstraintViolation<OneOfStringsBeanMethods>> violations =
        validator.validate(oneOfStringsBeanMethods);

    assertEquals(0, violations.size());
  }

  @Test
  public void testType() {
    OneOfStringsBeanType oneOfStringsBeanType = new OneOfStringsBeanType(Arrays.asList("A"));

    Set<ConstraintViolation<OneOfStringsBeanType>> violations =
        validator.validate(oneOfStringsBeanType);

    assertEquals(0, violations.size());
  }
}

@Data
@AllArgsConstructor
@Valid
class OneOfStringsBeanFields {

  @OneOfStrings({"A", "B"})
  private String aString;
}

@Data
@AllArgsConstructor
class OneOfStringsBeanMethods {

  private String aString;

  @OneOfStrings({"A", "B"})
  private String getAString() {
    return aString;
  }
}

@Data
@AllArgsConstructor
class OneOfStringsBeanType {

  private List<@OneOfStrings({"A", "B"}) String> strs;
}
