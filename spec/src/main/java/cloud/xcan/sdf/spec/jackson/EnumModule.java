package cloud.xcan.sdf.spec.jackson;

import cloud.xcan.sdf.spec.locale.EnumValueMessage;
import cloud.xcan.sdf.spec.utils.EnumUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;


public class EnumModule extends SimpleModule {

  public EnumModule() {
    super("jacksonEnumTypeModule", Version.unknownVersion());
    this.setDeserializers(new EnumDeserializers());
    this.addSerializer(new EnumSerializer());
  }

  private static class EnumDeserializers extends SimpleDeserializers {

    private EnumDeserializers() {
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config,
        BeanDescription beanDesc) throws JsonMappingException {
      return EnumValueMessage.class.isAssignableFrom(type) ? new EnumDeserializer(type) :
          super.findEnumDeserializer(type, config, beanDesc);
    }

    private static class EnumDeserializer<E extends EnumValueMessage> extends
        StdScalarDeserializer<E> {

      private Class<E> enumType;

      private EnumDeserializer(Class<E> clazz) {
        super(clazz);
        this.enumType = clazz;
      }

      @Override
      public E deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        // If the front passes only the value
        if (parser.hasToken(JsonToken.VALUE_STRING)) {
          return EnumUtils.of(this.enumType, parser.getText());
        }
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
          return EnumUtils.of(this.enumType, String.valueOf(parser.getIntValue()));
        }

        // The front is passed as an object
        if (parser.isExpectedStartObjectToken()) {
          TreeNode node = parser.getCodec().readTree(parser).get("value");
          if (!(node instanceof TextNode)) {
            throw new IllegalArgumentException("Enum value is not text type");
          }
          TextNode textNode = (TextNode) node;
          return EnumUtils.of(this.enumType, textNode.textValue());
        }
        throw new IllegalArgumentException("Unknown enum value deserialize");
      }
    }
  }

  private static class EnumSerializer extends StdSerializer<EnumValueMessage> {

    private EnumSerializer() {
      super(EnumValueMessage.class);
    }

    @Override
    public void serialize(EnumValueMessage enumerable, JsonGenerator jsonGenerator,
        SerializerProvider provider) throws IOException {
      jsonGenerator.writeStartObject();
      jsonGenerator.writeStringField("value", enumerable.getValue().toString());
      jsonGenerator.writeStringField("message", enumerable.getMessage());
      jsonGenerator.writeEndObject();
    }
  }
}
