package cloud.xcan.sdf.validator;

import static cloud.xcan.sdf.validator.AsciiPrintableTest.EOF;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.junit.Test;

public class AsciiPrintableTest {

  public static final String EOF = "\u0003";

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    AsciiPrintableBeanFields asciiPrintableBeanFields = new AsciiPrintableBeanFields();

    Set<ConstraintViolation<AsciiPrintableBeanFields>> violations =
        validator.validate(asciiPrintableBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    AsciiPrintableBeanMethods asciiPrintableBeanMethods = new AsciiPrintableBeanMethods();

    Set<ConstraintViolation<AsciiPrintableBeanMethods>> violations =
        validator.validate(asciiPrintableBeanMethods);

    assertEquals(1, violations.size());
  }

  public void testType() {
    AsciiPrintableType asciiPrintableType = new AsciiPrintableType();

    Set<ConstraintViolation<AsciiPrintableType>> violations =
        validator.validate(asciiPrintableType);

    assertEquals(1, violations.size());
  }
}

@Data
class AsciiPrintableBeanFields {

  @AsciiPrintable
  private String printable = "abc";

  @AsciiPrintable
  private String nonPrintable = "abc" + EOF;
}

@Data
class AsciiPrintableBeanMethods {

  @AsciiPrintable
  private String getPrintable() {
    return "abc";
  }

  ;

  @AsciiPrintable
  private String getNonPrintable() {
    return "abc" + EOF;
  }
}

@Data
class AsciiPrintableType {

  private List<@AsciiPrintable String> list = Arrays.asList("abc", EOF);
}

