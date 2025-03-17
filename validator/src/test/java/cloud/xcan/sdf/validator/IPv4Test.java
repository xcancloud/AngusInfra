package cloud.xcan.sdf.validator;


import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Data;
import org.junit.Test;

public class IPv4Test {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    IPv4BeanFields iPv4BeanFields = new IPv4BeanFields();

    Set<ConstraintViolation<IPv4BeanFields>> violations =
        validator.validate(iPv4BeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    IPv4BeanMethods iPv4BeanMethods = new IPv4BeanMethods();

    Set<ConstraintViolation<IPv4BeanMethods>> violations =
        validator.validate(iPv4BeanMethods);

    assertEquals(1, violations.size());
  }

  @Test
  public void testType() {
    IPv4BeanType iPv4BeanType = new IPv4BeanType();

    Set<ConstraintViolation<IPv4BeanType>> violations =
        validator.validate(iPv4BeanType);

    assertEquals(0, violations.size());
  }
}

@Data
class IPv4BeanFields {

  @IPv4
  private String correct = "127.0.0.1";

  @IPv4
  private String incorrect = "abc";
}

@Data
class IPv4BeanMethods {

  @IPv4
  private String getCorrect() {
    return "127.0.0.1";
  }

  @IPv4
  private String getIncorrect() {
    return "abc";
  }
}

@Data
class IPv4BeanType {

  private List<@IPv4 String> ips = Arrays.asList("127.0.0.1");
}
