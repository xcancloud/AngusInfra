package cloud.xcan.angus.lettucex.serializer;

import cloud.xcan.angus.spec.utils.StringUtils;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link RedisSerializer} that can read and write JSON using
 * <a href="https://github.com/FasterXML/jackson-core">Jackson's</a> and
 * <a href="https://github.com/FasterXML/jackson-databind">Jackson Databind</a> {@link
 * ObjectMapper}.
 * <p>
 * This converter can be used to bind to typed beans, or untyped {@link java.util.HashMap HashMap}
 * instances.
 * <b>Note:</b>Null objects are serialized as empty arrays and vice versa.
 *
 * @author Thomas Darimont
 * @since 1.2
 */
public class Jackson2JsonRedisSerializer<T> implements RedisSerializer<T> {

  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private final JavaType javaType;

  private ObjectMapper objectMapper;

  public static String clzzTypeName = "@class";

  /**
   * Creates a new {@link Jackson2JsonRedisSerializer} for the given target {@link Class}.
   *
   * @param type
   */
  public Jackson2JsonRedisSerializer(Class<T> type, ObjectMapper objectMapper) {
    this.javaType = getJavaType(type);
    this.objectMapper = objectMapper;
  }

  /**
   * Creates a new {@link Jackson2JsonRedisSerializer} for the given target {@link JavaType}.
   *
   * @param javaType
   */
  public Jackson2JsonRedisSerializer(JavaType javaType, ObjectMapper objectMapper) {
    this.javaType = javaType;
    this.objectMapper = objectMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public T deserialize(@Nullable byte[] bytes) throws SerializationException {
    if (isEmpty(bytes)) {
      return null;
    }
    try {
      JsonNode jsonNode = this.objectMapper.readTree(new String(bytes, DEFAULT_CHARSET));
      if (jsonNode.isObject()) {
        Object clz = jsonNode.get(clzzTypeName);
        if (clz != null) {
          return (T) this.objectMapper.readValue(bytes, 0, bytes.length,
              Class.forName(StringUtils.remove(clz.toString(), "\"")));
        }
      }
      return (T) this.objectMapper.readValue(bytes, 0, bytes.length, javaType);
    } catch (Exception ex) {
      throw new SerializationException("Could not read JSON: " + ex.getMessage(), ex);
    }
  }

  @Override
  public byte[] serialize(@Nullable Object t) throws SerializationException {
    try {
      return this.objectMapper.writeValueAsBytes(t);
    } catch (Exception ex) {
      throw new SerializationException("Could not write JSON: " + ex.getMessage(), ex);
    }
  }

  /**
   * Sets the {@code ObjectMapper} for this view. If not set, a default
   * {@link ObjectMapper#ObjectMapper() ObjectMapper} is used.
   * <p>
   * Setting a custom-configured {@code ObjectMapper} is one way to take further control of the JSON
   * serialization process. For example, an extended {@link SerializerFactory} can be configured
   * that provides custom serializers for specific types. The other option for refining the
   * serialization process is to use Jackson's provided annotations on the types to be serialized,
   * in which case a custom-configured ObjectMapper is unnecessary.
   */
  public void setObjectMapper(ObjectMapper objectMapper) {

    Assert.notNull(objectMapper, "'objectMapper' must not be null");
    this.objectMapper = objectMapper;
  }

  /**
   * Returns the Jackson {@link JavaType} for the specific class.
   * <p>
   * Default implementation returns {@link TypeFactory#constructType(java.lang.reflect.Type)}, but
   * this can be overridden in subclasses, to allow for custom generic collection handling. For
   * instance:
   *
   * <pre class="code">
   * protected JavaType getJavaType(Class&lt;?&gt; clazz) {
   * 	if (List.class.isAssignableFrom(clazz)) {
   * 		return TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, MyBean.class);
   *  } else {
   * 		return super.getJavaType(clazz);
   *  }
   * }
   * </pre>
   *
   * @param clazz the class to return the java type for
   * @return the java type
   */
  protected JavaType getJavaType(Class<?> clazz) {
    return TypeFactory.defaultInstance().constructType(clazz);
  }

  public static boolean isEmpty(@Nullable byte[] data) {
    return (data == null || data.length == 0);
  }
}
