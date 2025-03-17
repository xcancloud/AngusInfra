package cloud.xcan.sdf.validator;

import static cloud.xcan.sdf.validator.CreditCardType.AMEX;
import static cloud.xcan.sdf.validator.CreditCardType.VISA;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.Data;
import org.junit.Test;

public class CreditCardTest {

  public static final String AMEX_CC = "340000000000009";
  public static final String VISA_CC = "4111111111111111";

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    CreditCardBeanFields blankBeanFields = new CreditCardBeanFields();

    Set<ConstraintViolation<CreditCardBeanFields>> violations =
        validator.validate(blankBeanFields);

    assertEquals(0, violations.size());
  }
}


@Data
class CreditCardBeanFields {

  @CreditCard(AMEX)
  private String amex = CreditCardTest.AMEX_CC;

  @CreditCard({AMEX, VISA})
  private String amexOrVisa = CreditCardTest.AMEX_CC;

  @CreditCard({VISA})
  private String visa = CreditCardTest.VISA_CC;
}
