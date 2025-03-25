package cloud.xcan.angus.lettucex.serializer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

@Deprecated
public class GsonRedisSerializer<T> implements RedisSerializer<T> {

  private final Gson gson = new Gson();

  @Override
  public byte[] serialize(T t) throws SerializationException {
    if (t == null) {
      return new byte[0];
    }
    return gson.toJson(t).getBytes();
  }

  @Override
  public T deserialize(byte[] bytes) throws SerializationException {
    if (bytes == null || bytes.length == 0) {
      return null;
    }
    return gson.fromJson(new String(bytes), new TypeToken<T>() {
    }.getType());
  }
}
