package cloud.xcan.sdf.validator;

import static jakarta.validation.Validation.buildDefaultValidatorFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.junit.Test;

@Data
@EqualsAndHashCode(callSuper = false)
class IofEveAndAdam {

}

@Data
@EqualsAndHashCode(callSuper = false)
class IofCain extends IofEveAndAdam {

}

@Data
@EqualsAndHashCode(callSuper = false)
class IofAbel extends IofEveAndAdam {

}

@Data
@EqualsAndHashCode(callSuper = false)
class IofSeth extends IofEveAndAdam {

}

public class InstanceOfTest {

  private final Validator validator =
      buildDefaultValidatorFactory().getValidator();

  @Test
  public void testFields() {
    InstanceOfBeanFields instanceOfBeanFields = new InstanceOfBeanFields();

    Set<ConstraintViolation<InstanceOfBeanFields>> violations =
        validator.validate(instanceOfBeanFields);

    assertEquals(1, violations.size());
  }

  @Test
  public void testMethods() {
    InstanceOfBeanMethods instanceOfBeanMethods = new InstanceOfBeanMethods();

    Set<ConstraintViolation<InstanceOfBeanMethods>> violations =
        validator.validate(instanceOfBeanMethods);

    assertEquals(1, violations.size());
  }

  @Test
  public void testType() {
    InstanceOfBeanType instanceOfBeanType = new InstanceOfBeanType();

    Set<ConstraintViolation<InstanceOfBeanType>> violations =
        validator.validate(instanceOfBeanType);

    assertEquals(0, violations.size());
  }
}


@Data
class InstanceOfBeanFields {

  @InstanceOf({IofCain.class, IofAbel.class})
  private IofEveAndAdam correct = new IofCain();

  @InstanceOf({IofSeth.class})
  private IofEveAndAdam incorrect = new IofAbel();

}

@Data
class InstanceOfBeanMethods {

  @InstanceOf({IofCain.class, IofAbel.class})
  private IofEveAndAdam getCorrect() {
    return new IofCain();
  }

  @InstanceOf({IofSeth.class})
  private IofEveAndAdam getIncorrect() {
    return new IofAbel();
  }

}

@Data
class InstanceOfBeanType {

  private List<@InstanceOf({IofCain.class, IofAbel.class, IofSeth.class}) IofEveAndAdam> list =
      Arrays.asList(new IofCain(), new IofAbel(), new IofSeth());
}
