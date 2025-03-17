package cloud.xcan.sdf.validator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.Data;
import org.junit.Test;

public class EnumValidatorTest {

  private final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  public void testValidEnumValue() {
    MyEntity entity = new MyEntity();
    entity.setMyField(MyEnum.valid.name());

    assertTrue(validator.validate(entity).isEmpty());
  }

  @Test
  public void testInvalidEnumValue() {
    MyEntity entity = new MyEntity();
    entity.setMyField("invalid");

    assertFalse(validator.validate(entity).isEmpty());
  }

  @Test
  public void testNullValue() {
    MyEntity entity = new MyEntity();
    entity.setMyField(null);

    assertTrue(validator.validate(entity).isEmpty());
  }

  @Test
  public void testIgnoreCase() {
    MyEntity entity = new MyEntity();
    entity.setMyField("VALID");
    assertFalse(validator.validate(entity).isEmpty());

    entity = new MyEntity();
    entity.setMyField2("VALID");
    assertTrue(validator.validate(entity).isEmpty());
  }
}

@Data
class MyEntity {

  @EnumValue(enumClass = MyEnum.class)
  private String myField;

  @EnumValue(enumClass = MyEnum.class, ignoreCase = true)
  private String myField2;
}

enum MyEnum {
  valid
}
