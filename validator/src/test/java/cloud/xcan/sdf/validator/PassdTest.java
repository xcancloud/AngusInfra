package cloud.xcan.sdf.validator;

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

}


@AllArgsConstructor
@Data
class PassBean1 {

  @Passd()
  private String pass1 = null;
}

@AllArgsConstructor
@Data
@NoArgsConstructor
class PassBean2 {

  @Passd(minSize = 3, allowDigits = false, allowSpecialChar = false, allowUpperCase = false)
  private String pass2 = "ab";

}
