package cloud.xcan.angus.spec.jackson;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_FORMAT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_TIME_FORMAT;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.time.format.DateTimeFormatter.ofPattern;

import com.fasterxml.jackson.core.util.JacksonFeatureSet;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.deser.ValueInstantiators;
import com.fasterxml.jackson.databind.deser.std.StdValueInstantiator;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedClassResolver;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleKeyDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeFeature;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310StringParsableDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.JavaTimeDeserializerModifier;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.MonthDayDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.OffsetTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.YearMonthDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.DurationKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.InstantKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.LocalDateKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.LocalDateTimeKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.LocalTimeKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.MonthDayKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.OffsetDateTimeKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.OffsetTimeKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.PeriodKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.YearKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.YearMonthKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.ZoneIdKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.ZoneOffsetKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.key.ZonedDateTimeKeyDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.DurationSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.JavaTimeSerializerModifier;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.MonthDaySerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearMonthSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.YearSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZoneIdSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.key.ZonedDateTimeKeySerializer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;

/**
 * @see com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
 */
public final class JavaTimeModule extends SimpleModule {

  private static final long serialVersionUID = 1L;
  private JacksonFeatureSet<JavaTimeFeature> _features = JacksonFeatureSet.fromDefaults(
      JavaTimeFeature.values());

  /**
   * Date format string or a fully-qualified date format class name. For instance, 'yyyy-MM-dd
   * HH:mm:ss'.
   */
  private final String dateFormat;

  /**
   * Locale Date format string or a fully-qualified date format class name. For instance,
   * 'yyyy-MM-dd'.
   */
  private final String localeDateFormat;

  /**
   * Locale time format string or a fully-qualified date format class name. For instance,
   * 'HH:mm:ss'.
   */
  private final String localeTimeFormat;

  public JavaTimeModule() {
    this(null, null, null, null);
  }

  public JavaTimeModule(String name, String dateFormat, String localeDateFormat,
      String localeTimeFormat) {
    super(nullSafe(name, "customJavaTimeModule"), PackageVersion.VERSION);
    this.dateFormat = nullSafe(dateFormat, DEFAULT_DATE_TIME_FORMAT);
    this.localeDateFormat = nullSafe(localeDateFormat, DEFAULT_DATE_FORMAT);
    this.localeTimeFormat = nullSafe(localeTimeFormat, DEFAULT_TIME_FORMAT);
  }

  public JavaTimeModule enable(JavaTimeFeature f) {
    this._features = this._features.with(f);
    return this;
  }

  public JavaTimeModule disable(JavaTimeFeature f) {
    this._features = this._features.without(f);
    return this;
  }

  @Override
  public void setupModule(SetupContext context) {
    super.setupModule(context);
    SimpleDeserializers desers = new SimpleDeserializers();
    desers.addDeserializer(Instant.class, InstantDeserializer.INSTANT.withFeatures(this._features));
    desers.addDeserializer(OffsetDateTime.class,
        InstantDeserializer.OFFSET_DATE_TIME.withFeatures(this._features));
    desers.addDeserializer(ZonedDateTime.class,
        InstantDeserializer.ZONED_DATE_TIME.withFeatures(this._features));
    desers.addDeserializer(Duration.class, DurationDeserializer.INSTANCE);
    desers.addDeserializer(LocalDateTime.class,
        new LocalDateTimeDeserializer(ofPattern(dateFormat)));
    desers.addDeserializer(LocalDate.class,
        new LocalDateDeserializer(ofPattern(localeDateFormat)));
    desers.addDeserializer(LocalTime.class,
        new LocalTimeDeserializer(ofPattern(localeTimeFormat)));
    desers.addDeserializer(MonthDay.class, MonthDayDeserializer.INSTANCE);
    desers.addDeserializer(OffsetTime.class, OffsetTimeDeserializer.INSTANCE);
    desers.addDeserializer(Period.class, JSR310StringParsableDeserializer.PERIOD);
    desers.addDeserializer(Year.class, YearDeserializer.INSTANCE);
    desers.addDeserializer(YearMonth.class, YearMonthDeserializer.INSTANCE);
    desers.addDeserializer(ZoneId.class, JSR310StringParsableDeserializer.ZONE_ID);
    desers.addDeserializer(ZoneOffset.class, JSR310StringParsableDeserializer.ZONE_OFFSET);
    context.addDeserializers(desers);
    boolean oneBasedMonthEnabled = this._features.isEnabled(JavaTimeFeature.ONE_BASED_MONTHS);
    context.addBeanDeserializerModifier(new JavaTimeDeserializerModifier(oneBasedMonthEnabled));
    context.addBeanSerializerModifier(new JavaTimeSerializerModifier(oneBasedMonthEnabled));
    if (this._deserializers != null) {
      context.addDeserializers(this._deserializers);
    }

    SimpleSerializers sers = new SimpleSerializers();
    sers.addSerializer(Duration.class, DurationSerializer.INSTANCE);
    sers.addSerializer(Instant.class, InstantSerializer.INSTANCE);
    sers.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(ofPattern(dateFormat)));
    sers.addSerializer(LocalDate.class, new LocalDateSerializer(ofPattern(localeDateFormat)));
    sers.addSerializer(LocalTime.class, new LocalTimeSerializer(ofPattern(localeTimeFormat)));
    sers.addSerializer(MonthDay.class, MonthDaySerializer.INSTANCE);
    sers.addSerializer(OffsetDateTime.class, OffsetDateTimeSerializer.INSTANCE);
    sers.addSerializer(OffsetTime.class, OffsetTimeSerializer.INSTANCE);
    sers.addSerializer(Period.class, new ToStringSerializer(Period.class));
    sers.addSerializer(Year.class, YearSerializer.INSTANCE);
    sers.addSerializer(YearMonth.class, YearMonthSerializer.INSTANCE);
    sers.addSerializer(ZonedDateTime.class, ZonedDateTimeSerializer.INSTANCE);
    sers.addSerializer(ZoneId.class, new ZoneIdSerializer());
    sers.addSerializer(ZoneOffset.class, new ToStringSerializer(ZoneOffset.class));
    context.addSerializers(sers);
    if (this._serializers != null) {
      context.addSerializers(this._serializers);
    }

    SimpleSerializers keySers = new SimpleSerializers();
    keySers.addSerializer(ZonedDateTime.class, ZonedDateTimeKeySerializer.INSTANCE);
    context.addKeySerializers(keySers);
    if (this._keySerializers != null) {
      context.addKeySerializers(this._keySerializers);
    }

    SimpleKeyDeserializers keyDesers = new SimpleKeyDeserializers();
    keyDesers.addDeserializer(Duration.class, DurationKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(Instant.class, InstantKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(LocalDateTime.class, LocalDateTimeKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(LocalDate.class, LocalDateKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(LocalTime.class, LocalTimeKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(MonthDay.class, MonthDayKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(OffsetDateTime.class, OffsetDateTimeKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(OffsetTime.class, OffsetTimeKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(Period.class, PeriodKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(Year.class, YearKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(YearMonth.class, YearMonthKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(ZonedDateTime.class, ZonedDateTimeKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(ZoneId.class, ZoneIdKeyDeserializer.INSTANCE);
    keyDesers.addDeserializer(ZoneOffset.class, ZoneOffsetKeyDeserializer.INSTANCE);
    context.addKeyDeserializers(keyDesers);
    if (this._keyDeserializers != null) {
      context.addKeyDeserializers(this._keyDeserializers);
    }

    context.addValueInstantiators(new ValueInstantiators.Base() {
      public ValueInstantiator findValueInstantiator(DeserializationConfig config,
          BeanDescription beanDesc, ValueInstantiator defaultInstantiator) {
        JavaType type = beanDesc.getType();
        Class<?> raw = type.getRawClass();
        if (ZoneId.class.isAssignableFrom(raw)
            && defaultInstantiator instanceof StdValueInstantiator) {
          StdValueInstantiator inst = (StdValueInstantiator) defaultInstantiator;
          AnnotatedClass ac;
          if (raw == ZoneId.class) {
            ac = beanDesc.getClassInfo();
          } else {
            ac = AnnotatedClassResolver.resolve(config, config.constructType(ZoneId.class), config);
          }

          if (!inst.canCreateFromString()) {
            AnnotatedMethod factory = JavaTimeModule.this._findFactory(ac, "of", String.class);
            if (factory != null) {
              inst.configureFromStringCreator(factory);
            }
          }
        }

        return defaultInstantiator;
      }
    });
  }

  protected AnnotatedMethod _findFactory(AnnotatedClass cls, String name, Class<?>... argTypes) {
    int argCount = argTypes.length;
    Iterator var5 = cls.getFactoryMethods().iterator();

    AnnotatedMethod method;
    do {
      if (!var5.hasNext()) {
        return null;
      }

      method = (AnnotatedMethod) var5.next();
    } while (!name.equals(method.getName()) || method.getParameterCount() != argCount);

    for (int i = 0; i < argCount; ++i) {
      Class<?> argType = method.getParameter(i).getRawType();
      if (!argType.isAssignableFrom(argTypes[i])) {
      }
    }

    return method;
  }
}
