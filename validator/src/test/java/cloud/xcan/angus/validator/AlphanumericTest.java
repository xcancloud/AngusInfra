package cloud.xcan.angus.validator;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.junit.Test;

public class AlphanumericTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    AlphanumericBeanField alphanumericBeanField = new AlphanumericBeanField();

    Set<ConstraintViolation<AlphanumericBeanField>> violations =
        validator.validate(alphanumericBeanField);

    assertEquals(3, violations.size());
  }

  @Test
  public void testTypeParam() {
    AlphanumericList alphanumericList = new AlphanumericList();

    Set<ConstraintViolation<AlphanumericList>> violations =
        validator.validate(alphanumericList);

    assertEquals(3, violations.size());
  }

  @Test
  public void testMethods() {
    AlphanumericBeanMethods alphanumericBeanMethods = new AlphanumericBeanMethods();

    Set<ConstraintViolation<AlphanumericBeanMethods>> violations =
        validator.validate(alphanumericBeanMethods);

    assertEquals(3, violations.size());
  }
}

@Data
class AlphanumericBeanField {

  @Alphanumeric
  private String alphanumeric = "123AAAaaa111";

  @Alphanumeric
  private String alphanumericNull = null;

  @Alphanumeric
  private String alphanumericEmpty = "";

  @Alphanumeric
  private String alphanumericWrong = "   aaa11AA2";
}

@Data
class AlphanumericBeanMethods {

  @Alphanumeric
  private String getAlphanumeric() {
    return "123AAAaaa111";
  }

  @Alphanumeric
  private String getAlphanumericNull() {
    return null;
  }

  @Alphanumeric
  private String getAlphanumericEmpty() {
    return "";
  }

  @Alphanumeric
  private String getAlphanumericWrong() {
    return "   aaa11AA2";
  }
}

@Data
class AlphanumericList {

  private List<@Alphanumeric String> strings = asList("123AAAaaa111", null, "", "   aaa11AA2");

}
