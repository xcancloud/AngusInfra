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

public class AlphanumericSpaceTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testMethods() {
    AlphanumericSpaceBeanGetters alphanumericSpaceBeanGetters = new AlphanumericSpaceBeanGetters();

    Set<ConstraintViolation<AlphanumericSpaceBeanGetters>> violations =
        validator.validate(alphanumericSpaceBeanGetters);

    assertEquals(2, violations.size());
  }

  @Test
  public void testFields() {
    AlphanumericSpaceFields alphanumericSpaceFields = new AlphanumericSpaceFields();

    Set<ConstraintViolation<AlphanumericSpaceFields>> violations =
        validator.validate(alphanumericSpaceFields);

    assertEquals(2, violations.size());

  }

  @Test
  public void testType() {
    AlphanumericSpaceType alphanumericSpaceType = new AlphanumericSpaceType();

    Set<ConstraintViolation<AlphanumericSpaceType>> violations =
        validator.validate(alphanumericSpaceType);

    assertEquals(2, violations.size());
  }
}

@Data
class AlphanumericSpaceFields {

  @AlphanumericSpace
  private String alphanumericSpace = " aA12 21 zz";

  @AlphanumericSpace
  private String alphanumericSpaceEmpty = "";

  @AlphanumericSpace
  private String alphanumericSpaceNull = null;

  @AlphanumericSpace
  private String alphanumericSpaceWrong = "; adsds 11 ZZZ AA";
}

@Data
class AlphanumericSpaceBeanGetters {

  @AlphanumericSpace
  public String getAlphanumericSpace() {
    return " aA12 21 zz";
  }

  @AlphanumericSpace
  public String getAlphanumericSpaceEmpty() {
    return "";
  }

  @AlphanumericSpace
  public String getAlphanumericSpaceNull() {
    return null;
  }

  @AlphanumericSpace
  public String getAlphanumericSpaceWrong() {
    return "; adsds 11 ZZZ AA";
  }
}

@Data
class AlphanumericSpaceType {

  private List<@AlphanumericSpace String> list = Arrays
      .asList(" aA12 21 zz", "", null, "; adsds 11 ZZZ AA");
}
