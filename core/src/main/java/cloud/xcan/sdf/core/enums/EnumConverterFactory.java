package cloud.xcan.sdf.core.enums;

import cloud.xcan.sdf.spec.locale.EnumMessage;
import cloud.xcan.sdf.spec.utils.EnumUtils;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nonnull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

public class EnumConverterFactory implements ConverterFactory<String, EnumMessage> {

  private final Map<Class, Converter> converterCache = new WeakHashMap<>();

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public <T extends EnumMessage> Converter<String, T> getConverter(@Nonnull Class<T> targetType) {
    return converterCache.computeIfAbsent(targetType,
        k -> converterCache.put(k, new EnumConverter(k))
    );
  }

  protected class EnumConverter<T extends EnumMessage> implements Converter<String, T> {

    private final Class<T> enumType;

    public EnumConverter(@Nonnull Class<T> enumType) {
      this.enumType = enumType;
    }

    @Override
    public T convert(@Nonnull String value) {
      return EnumUtils.of(this.enumType, value);
    }

  }
}
