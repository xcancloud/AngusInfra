/*
 * Copyright 2012-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.xcan.angus.web;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT_10;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT_4;
import static cloud.xcan.angus.spec.experimental.Assert.assertNotNull;

import cloud.xcan.angus.core.enums.EnumConverterFactory;
import cloud.xcan.angus.core.jackson.Jackson2ObjectMapperBuilderCustomizer;
import cloud.xcan.angus.core.jackson.JacksonProperties;
import cloud.xcan.angus.spec.jackson.EnumModule;
import cloud.xcan.angus.spec.jackson.serializer.TimeValueDeSerializer;
import cloud.xcan.angus.spec.jackson.serializer.TimeValueSerializer;
import cloud.xcan.angus.spec.unit.TimeValue;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.Ordered;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Auto configuration for Jackson. The following auto-configuration will get applied:
 * <ul>
 * <li>an {@link ObjectMapper} in case none is already configured.</li>
 * <li>a {@link Jackson2ObjectMapperBuilder} in case none is already configured.</li>
 * <li>auto-registration for all {@link Module} beans with all {@link ObjectMapper} beans
 * (including the defaulted ones).</li>
 * </ul>
 *
 * @author XiaoLong Liu
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ObjectMapper.class, WebMvcConfigurer.class})
@EnableConfigurationProperties(JacksonProperties.class)
public class JacksonAutoConfigurer implements WebMvcConfigurer {

  private static final Map<?, Boolean> FEATURE_DEFAULTS;

  static {
    Map<Object, Boolean> featureDefaults = new HashMap<>();
    featureDefaults.put(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    featureDefaults.put(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
    FEATURE_DEFAULTS = Collections.unmodifiableMap(featureDefaults);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverterFactory(new EnumConverterFactory());
  }

  @Bean
  public JsonComponentModule jsonComponentModule() {
    return new JsonComponentModule();
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
  static class JacksonObjectMapperConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
      ObjectMapper mapper = builder.createXmlMapper(false).build();
      // SDF config
      SimpleModule simpleModule = new SimpleModule();
      simpleModule.addSerializer(Long.TYPE, new ToStringSerializer(Long.TYPE));
      simpleModule.addSerializer(Long.class, new ToStringSerializer(Long.class));
      simpleModule.addSerializer(TimeValue.class, new TimeValueSerializer());
      simpleModule.addDeserializer(TimeValue.class, new TimeValueDeSerializer());
      // simpleModule.addSerializer(BigDecimal.class, new BigDecimalSerializer());
      // simpleModule.addDeserializer(BigDecimal.class, new BigDecimalDeSerializer());
      mapper.registerModule(simpleModule);
      mapper.registerModule(new EnumModule());

      // Exception: Could not read JSON: The class with cloud.xcan.angus.api.commonlink.setting.Setting and name of cloud.xcan.angus.api.commonlink.setting.Setting is not in the allowlist. If you believe this class is safe to deserialize, please provide an explicit mapping using Jackson annotations or by providing a Mixin.
      // If the serialization is only done by a trusted source, you can also enable default typing.
      // objectMapper.addMixIn(CacheMessage.class, CacheMessageMixin.class); // Fix method 1
      // mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL); // Fix method 2, recommend!!!
      // mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

      // mapper.setSerializationInclusion(Include.NON_EMPTY); -> Note: The null object is utilized by the front-end.
      mapper.setSerializationInclusion(Include.NON_NULL);
      mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
      mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
      mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
      //mapper.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
      return mapper;
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(ParameterNamesModule.class)
  static class ParameterNamesModuleConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ParameterNamesModule parameterNamesModule() {
      return new ParameterNamesModule(JsonCreator.Mode.DEFAULT);
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
  static class JacksonObjectMapperBuilderConfiguration {

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder(ApplicationContext applicationContext,
        List<Jackson2ObjectMapperBuilderCustomizer> customizers) {
      Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
      builder.applicationContext(applicationContext);
      customize(builder, customizers);
      return builder;
    }

    private void customize(Jackson2ObjectMapperBuilder builder,
        List<Jackson2ObjectMapperBuilderCustomizer> customizers) {
      for (Jackson2ObjectMapperBuilderCustomizer customizer : customizers) {
        customizer.customize(builder);
      }
    }
  }

  @Configuration(proxyBeanMethods = false)
  @ConditionalOnClass(Jackson2ObjectMapperBuilder.class)
  @EnableConfigurationProperties(JacksonProperties.class)
  static class Jackson2ObjectMapperBuilderCustomizerConfiguration {

    @Bean
    StandardJackson2ObjectMapperBuilderCustomizer standardJacksonObjectMapperBuilderCustomizer(
        ApplicationContext applicationContext, JacksonProperties jacksonProperties) {
      return new StandardJackson2ObjectMapperBuilderCustomizer(applicationContext,
          jacksonProperties);
    }

    static final class StandardJackson2ObjectMapperBuilderCustomizer
        implements Jackson2ObjectMapperBuilderCustomizer, Ordered {

      private final ApplicationContext applicationContext;

      private final JacksonProperties jacksonProperties;

      StandardJackson2ObjectMapperBuilderCustomizer(ApplicationContext applicationContext,
          JacksonProperties jacksonProperties) {
        this.applicationContext = applicationContext;
        this.jacksonProperties = jacksonProperties;
      }

      private static <T> Collection<T> getBeans(ListableBeanFactory beanFactory, Class<T> type) {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(beanFactory, type).values();
      }

      @Override
      public int getOrder() {
        return 0;
      }

      @Override
      public void customize(Jackson2ObjectMapperBuilder builder) {

        if (this.jacksonProperties.getDefaultPropertyInclusion() != null) {
          builder.serializationInclusion(this.jacksonProperties.getDefaultPropertyInclusion());
        }
        if (this.jacksonProperties.getTimeZone() != null) {
          builder.timeZone(this.jacksonProperties.getTimeZone());
        }

        builder.serializers(
            new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_FMT)),
            new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FMT_4)),
            new LocalTimeSerializer(DateTimeFormatter.ofPattern(DATE_FMT_10))
        );
        builder.deserializers(
            new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FMT)),
            new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FMT_4)),
            new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FMT_10))
        );
        configureFeatures(builder, FEATURE_DEFAULTS);
        configureVisibility(builder, this.jacksonProperties.getVisibility());
        configureFeatures(builder, this.jacksonProperties.getDeserialization());
        configureFeatures(builder, this.jacksonProperties.getSerialization());
        configureFeatures(builder, this.jacksonProperties.getMapper());
        configureFeatures(builder, this.jacksonProperties.getParser());
        configureFeatures(builder, this.jacksonProperties.getGenerator());
        configureDateFormat(builder);
        configurePropertyNamingStrategy(builder);
        configureModules(builder);
        configureLocale(builder);
      }

      private void configureFeatures(Jackson2ObjectMapperBuilder builder,
          Map<?, Boolean> features) {
        features.forEach((feature, value) -> {
          if (value != null) {
            if (value) {
              builder.featuresToEnable(feature);
            } else {
              builder.featuresToDisable(feature);
            }
          }
        });
      }

      private void configureVisibility(Jackson2ObjectMapperBuilder builder,
          Map<PropertyAccessor, JsonAutoDetect.Visibility> visibilities) {
        visibilities.forEach(builder::visibility);
      }

      private void configureDateFormat(Jackson2ObjectMapperBuilder builder) {
        // We support a fully qualified class name extending DateFormat or a date
        // pattern string value
        String dateFormat = this.jacksonProperties.getDateFormat();
        if (dateFormat != null) {
          try {
            Class<?> dateFormatClass = ClassUtils.forName(dateFormat, null);
            builder.dateFormat((DateFormat) BeanUtils.instantiateClass(dateFormatClass));
          } catch (ClassNotFoundException ex) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            // Since Jackson 2.6.3 we always need to set a TimeZone (see
            // gh-4170). If none in our properties fallback to the Jackson's
            // default
            TimeZone timeZone = this.jacksonProperties.getTimeZone();
            if (timeZone == null) {
              timeZone = new ObjectMapper().getSerializationConfig().getTimeZone();
            }
            simpleDateFormat.setTimeZone(timeZone);
            builder.dateFormat(simpleDateFormat);
          }
        }
      }

      private void configurePropertyNamingStrategy(Jackson2ObjectMapperBuilder builder) {
        // We support a fully qualified class name extending Jackson's
        // PropertyNamingStrategy or a string value corresponding to the constant
        // names in PropertyNamingStrategy which hold default provided
        // implementations
        String strategy = this.jacksonProperties.getPropertyNamingStrategy();
        if (strategy != null) {
          try {
            configurePropertyNamingStrategyClass(builder, ClassUtils.forName(strategy, null));
          } catch (ClassNotFoundException ex) {
            configurePropertyNamingStrategyField(builder, strategy);
          }
        }
      }

      private void configurePropertyNamingStrategyClass(Jackson2ObjectMapperBuilder builder,
          Class<?> propertyNamingStrategyClass) {
        builder.propertyNamingStrategy(
            (PropertyNamingStrategy) BeanUtils.instantiateClass(propertyNamingStrategyClass));
      }

      private void configurePropertyNamingStrategyField(Jackson2ObjectMapperBuilder builder,
          String fieldName) {
        // Find the field (this way we automatically support new constants
        // that may be added by Jackson in the future)
        Field field = ReflectionUtils.findField(PropertyNamingStrategy.class, fieldName,
            PropertyNamingStrategy.class);
        assertNotNull(field, () -> "Constant named '" + fieldName + "' not found on "
            + PropertyNamingStrategy.class.getName());
        try {
          builder.propertyNamingStrategy((PropertyNamingStrategy) field.get(null));
        } catch (Exception ex) {
          throw new IllegalStateException(ex);
        }
      }

      private void configureModules(Jackson2ObjectMapperBuilder builder) {
        Collection<Module> moduleBeans = getBeans(this.applicationContext, Module.class);
        builder.modulesToInstall(moduleBeans.toArray(new Module[0]));
      }

      private void configureLocale(Jackson2ObjectMapperBuilder builder) {
        Locale locale = this.jacksonProperties.getLocale();
        if (locale != null) {
          builder.locale(locale);
        }
      }

    }
  }
}
