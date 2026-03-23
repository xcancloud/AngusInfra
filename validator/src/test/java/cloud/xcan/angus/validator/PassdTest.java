package cloud.xcan.angus.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

public class PassdTest {

  private final Validator validator = Validation
      .buildDefaultValidatorFactory().getValidator();

  @Test
  public void nullPasswordTest() {
    PassBean1 passBean1 = new PassBean1(null);
    Set<ConstraintViolation<PassBean1>> violations = validator.validate(passBean1);
    assertEquals(1, violations.size());
  }

  @Test
  public void minSizeTest() {
    PassBean2 p = new PassBean2();
    Set<ConstraintViolation<PassBean2>> violations = validator.validate(p);
    assertEquals(1, violations.size());
  }

  /**
   * Default {@link Password#maxSize()} is 50 and is inclusive (50 chars valid, 51 invalid).
   */
  @Test
  public void maxSizeInclusiveTest() {
    String exactly50 = "a".repeat(49) + "1";
    assertEquals(50, exactly50.length());
    assertEquals(0, validator.validate(new PassBeanMax(exactly50)).size());

    String fiftyOne = "a".repeat(50) + "1";
    assertEquals(51, fiftyOne.length());
    assertEquals(1, validator.validate(new PassBeanMax(fiftyOne)).size());
  }

}


@AllArgsConstructor
@Data
class PassBean1 {

  @Password()
  private String pass1 = null;
}

@AllArgsConstructor
@Data
@NoArgsConstructor
class PassBean2 {

  @Password(minSize = 3, allowDigits = false, allowSpecialChar = false, allowUpperCase = false)
  private String pass2 = "ab";

}

@AllArgsConstructor
@Data
class PassBeanMax {

  @Password(allowMaxRepeatRate = 1.0)
  private String pass;
}
