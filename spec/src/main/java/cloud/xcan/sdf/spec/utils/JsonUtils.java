/*
 * Copyright (c) 2021   XCan Company
 *
 *        http://www.xcan.cloud
 *
 * The product is based on the open source project org.asynchttpclient
 * modified or rewritten by the XCan team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * On the basis of Apache License 2.0, other terms need to comply with
 * XCBL License restriction requirements. Detail XCBL license at:
 *
 * http://www.xcan.cloud/licenses/XCBL-1.0
 */
package cloud.xcan.sdf.spec.utils;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT_10;
import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT_4;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;

import cloud.xcan.sdf.spec.jackson.EnumModule;
import cloud.xcan.sdf.spec.jackson.serializer.BigDecimalDeSerializer;
import cloud.xcan.sdf.spec.jackson.serializer.BigDecimalSerializer;
import cloud.xcan.sdf.spec.jackson.serializer.TimeValueDeSerializer;
import cloud.xcan.sdf.spec.jackson.serializer.TimeValueSerializer;
import cloud.xcan.sdf.spec.unit.TimeValue;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonUtils {

  public static final ObjectMapper JSON = new ObjectMapper();

  static {

    JSON.registerModule(new EnumModule());
    //JSON.registerModule(new CoreJackson2Module());
    JSON.registerModule(new JavaTimeModule());

    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(Long.TYPE, new ToStringSerializer(Long.TYPE));
    simpleModule.addSerializer(Long.class, new ToStringSerializer(Long.class));
    simpleModule.addSerializer(TimeValue.class, new TimeValueSerializer());
    simpleModule.addDeserializer(TimeValue.class, new TimeValueDeSerializer());
    simpleModule.addSerializer(BigDecimal.class, new BigDecimalSerializer());
    simpleModule.addDeserializer(BigDecimal.class, new BigDecimalDeSerializer());

    // Note: After JavaTimeModule, override default time serialization
    simpleModule.addSerializer(LocalDateTime.class,
        new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_FMT)));
    simpleModule.addDeserializer(LocalDateTime.class,
        new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FMT)));
    simpleModule.addSerializer(LocalDate.class,
        new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FMT_4)));
    simpleModule.addDeserializer(LocalDate.class,
        new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FMT_4)));
    simpleModule.addSerializer(LocalTime.class,
        new LocalTimeSerializer(DateTimeFormatter.ofPattern(DATE_FMT_10)));
    simpleModule.addDeserializer(LocalTime.class,
        new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FMT_10)));
    JSON.registerModule(simpleModule);

    // Exception: Could not read JSON: The class with cloud.xcan.sdf.api.commonlink.setting.Setting and name of cloud.xcan.sdf.api.commonlink.setting.Setting is not in the allowlist. If you believe this class is safe to deserialize, please provide an explicit mapping using Jackson annotations or by providing a Mixin.
    // If the serialization is only done by a trusted source, you can also enable default typing.
    // objectMapper.addMixIn(CacheMessage.class, CacheMessageMixin.class); // Fix method 1
    // JSON.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL); // Fix method 2, recommend!!!
    // JSON.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

    // When enabling the above configuration and manually specifying the class type deserialization, the following error will be reported
    // com.fasterxml.jackson.databind.exc.MismatchedInputException: Unexpected token (START_OBJECT), expected START_ARRAY: need JSON Array to contain As.WRAPPER_ARRAY type information for class cloud.xcan.sdf.core.log.OperationLogProperties
    // at [Source: (String)"{"enabled":true,"clearBeforeDay":30}"; line: 1, column: 1]

    JSON.setSerializationInclusion(Include.NON_EMPTY);
    JSON.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    JSON.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    JSON.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
    JSON.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    JSON.configure(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    JSON.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // @JsonCreator -> See io.swagger.v3.oas.models.parameters.Parameter#StyleEnum
    JSON.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
  }

  public static String toJson(Object bean) {
    try {
      return JSON.writeValueAsString(bean);
    } catch (Exception e) {
      log.error("Bean {} to json exception", bean, e);
      return null;
    }
  }

  public static <T> T fromJson(String json, Class<T> clazz) {
    try {
      return JSON.readValue(json, clazz);
    } catch (Exception e) {
      log.error("From json {} to clazz {} exception", json, clazz, e);
      return null;
    }
  }

  public static <T> T fromJsonObject(Object json, Class<T> clazz) {
    try {
      return JSON.convertValue(json, clazz);
    } catch (Exception e) {
      log.error("From json {} to clazz {} exception", json, clazz, e);
      return null;
    }
  }

  public static <T> T fromJson(String json, TypeReference<T> type) {
    try {
      return JSON.readValue(json, type);
    } catch (Exception e) {
      log.error("From json {} to type {} exception", json, type, e);
      return null;
    }
  }

  public static <T> T fromJsonObject(Object json, TypeReference<T> type) {
    try {
      return JSON.convertValue(json, type);
    } catch (Exception e) {
      log.error("From json {} to type {} exception", json, type, e);
      return null;
    }
  }

  public static <T> T fromJson(byte[] json, Class<T> clazz) {
    return fromJson(new String(json, UTF_8), clazz);
  }

  public static boolean isJson(String jsonString) {
    if (isEmpty(jsonString)) {
      return false;
    }
    try {
      JSON.readTree(jsonString);
      return true;
    } catch (JsonProcessingException e) {
      return false;
    }
  }

  public static boolean isJsonArray(String jsonString) {
    if (isEmpty(jsonString)) {
      return false;
    }
    try {
      return JSON.readTree(jsonString).isArray();
    } catch (JsonProcessingException e) {
      return false;
    }
  }

  public static boolean isJsonObject(String jsonString) {
    if (isEmpty(jsonString)) {
      return false;
    }
    try {
      return JSON.readTree(jsonString).isObject();
    } catch (JsonProcessingException e) {
      return false;
    }
  }

  public static <T> T convert(String content, Class<T> clazz) throws JsonProcessingException {
    return isEmpty(content) ? null : JSON.readValue(content, clazz);
  }

  public static <T> T convert(String content, TypeReference<T> type)
      throws JsonProcessingException {
    return isEmpty(content) ? null : JSON.readValue(content, type);
  }

  public static Map<String, Object> readValue(String json) throws JsonProcessingException {
    return isEmpty(json) ? null : JSON.readValue(json, new TypeReference<>() {
    });
  }
}
