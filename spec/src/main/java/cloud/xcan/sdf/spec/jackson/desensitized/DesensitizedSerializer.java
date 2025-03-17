package cloud.xcan.sdf.spec.jackson.desensitized;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import java.io.IOException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Serialization annotation custom implementation JsonSerializer<String>： Appoint String type
 * ,serialize() HttpMethod is used to load the modified data
 */
public class DesensitizedSerializer extends JsonSerializer<String> implements
    ContextualSerializer {

  private SensitiveType strategy;

  @Override
  public Class<String> handledType() {
    return String.class;
  }

  @Override
  public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    String newValue = value;
    if (StringUtils.isNotBlank(value)) {
      switch (strategy) {
        case CHINESE_NAME:
          newValue = DesensitizedUtils.chineseName(value);
          break;
        case ID_CARD:
          newValue = DesensitizedUtils.idCardNum(value);
          break;
        case FIXED_PHONE:
          newValue = DesensitizedUtils.fixedPhone(value);
          break;
        case MOBILE:
          newValue = DesensitizedUtils.mobilePhone(value);
          break;
        case ADDRESS:
          newValue = DesensitizedUtils.address(value, 8);
          break;
        case EMAIL:
          newValue = DesensitizedUtils.email(value);
          break;
        case BANK_CARD:
          newValue = DesensitizedUtils.bankCard(value);
          break;
        case PASSD:
          newValue = DesensitizedUtils.password(value);
          break;
        case CAR_NUMBER:
          newValue = DesensitizedUtils.carNumber(value);
          break;
        default:
      }
    }
    gen.writeString(newValue);
  }

  /**
   * Get the annotation attribute on the attribute
   */
  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
      throws JsonMappingException {
    Desensitized annotation = property.getAnnotation(Desensitized.class);
    if (Objects.nonNull(annotation) && Objects
        .equals(String.class, property.getType().getRawClass())) {
      this.strategy = annotation.type();
      return this;
    }
    return prov.findValueSerializer(property.getType(), property);
  }
}