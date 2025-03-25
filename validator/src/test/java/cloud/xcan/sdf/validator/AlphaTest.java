package cloud.xcan.angus.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

public class AlphaTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testField() {

    AlphaBeanField alphaBeanField = new AlphaBeanField();

    Set<ConstraintViolation<AlphaBeanField>> violations =
        validator.validate(alphaBeanField);

    assertEquals(3, violations.size());
  }

  @Test
  public void testTypeParam() {
    AlphaBeanType alphaBeanType = new AlphaBeanType();

    Set<ConstraintViolation<AlphaBeanType>> violations =
        validator.validate(alphaBeanType);

    assertEquals(3, violations.size());
  }

  @Test
  public void testGetters() {
    AlphaBeanGetters alphaBeanGetters = new AlphaBeanGetters();

    Set<ConstraintViolation<AlphaBeanGetters>> violations =
        validator.validate(alphaBeanGetters);

    assertEquals(3, violations.size());
  }
}

@Data
class AlphaBeanGetters {

  @Alpha
  private final String getAlphaGood() {
    return "abcAAbb";
  }

  @Alpha
  private final String getAlphaNull() {
    return null;
  }

  @Alpha
  private final String getAlphaBadNumbers() {
    return "abc111";
  }

  @Alpha
  private final String getAlphaEmpty() {
    return "";
  }
}

@Data
class AlphaBeanField {

  @Alpha
  private final String alphaGood = "abcAAbb";

  @Alpha
  private final String alphaNull = null;

  @Alpha
  private final String alphaBadNumbers = "abc111";

  @Alpha
  private final String alphaEmpty = "";
}

@NoArgsConstructor
class AlphaBeanType {

  private List<@Alpha String> list = Arrays.asList("abcAAbb", null, "abc111", "");
}
