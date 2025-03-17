package swagger;

import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class SchemaTest {

  @Test
  public void AdditionalPropertiesBoolean() {
    Map<String, Schema> schemas = new HashMap<>();

    schemas.put("StringSchema", new StringSchema()
        .description("simple string schema")
        .minLength(3)
        .maxLength(100)
        .example("it works")
        .additionalProperties(true)
    );
  }

  @Test
  public void AdditionalPropertiesSchema() {
    Map<String, Schema> schemas = new HashMap<>();

    schemas.put("IntegerSchema", new IntegerSchema()
        .description("simple integer schema")
        .multipleOf(new BigDecimal(3))
        .minimum(new BigDecimal(6))
        .additionalProperties(new StringSchema())
    );

  }

}
