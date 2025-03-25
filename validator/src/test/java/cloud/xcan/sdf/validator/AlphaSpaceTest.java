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

public class AlphaSpaceTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    AlphaSpaceBeanFields alphaSpaceBeanFields = new AlphaSpaceBeanFields();

    Set<ConstraintViolation<AlphaSpaceBeanFields>> violations =
        validator.validate(alphaSpaceBeanFields);

    assertEquals(2, violations.size());
  }

  @Test
  public void testMethods() {
    AlphaSpaceBeanMethods alphaSpaceBeanMethods = new AlphaSpaceBeanMethods();

    Set<ConstraintViolation<AlphaSpaceBeanMethods>> violations =
        validator.validate(alphaSpaceBeanMethods);

    assertEquals(2, violations.size());
  }

  @Test
  public void testType() {
    AlphaSpaceList alphaSpaceList = new AlphaSpaceList();

    Set<ConstraintViolation<AlphaSpaceList>> violations =
        validator.validate(alphaSpaceList);

    assertEquals(2, violations.size());
  }
}

@Data
class AlphaSpaceBeanFields {

  @AlphaSpace
  private String alphaSpace = "abc ABC";

  @AlphaSpace
  private String alphaSpaceEmpty = "";

  @AlphaSpace
  private String alphaSpaceNull = null;

  @AlphaSpace
  private String alphaSpaceWrong = "abc 123";
}

@Data
class AlphaSpaceBeanMethods {

  @AlphaSpace
  private String getAlphaSpace() {
    return "abc ABC";
  }

  @AlphaSpace
  private String getAlphaSpaceEmpty() {
    return "";
  }

  @AlphaSpace
  private String getAlphaSpaceNull() {
    return null;
  }

  @AlphaSpace
  private String getAlphaSpaceWrong() {
    return "abc 123";
  }
}

@Data
class AlphaSpaceList {

  private List<@AlphaSpace String> strings = Arrays.asList("abc ABC", "", null, "abc 123");
}
