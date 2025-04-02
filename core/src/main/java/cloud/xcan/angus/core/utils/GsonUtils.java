package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT_4;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author XiaoLong Liu
 */
public class GsonUtils {

  private GsonUtils() { /* no instance */ }

  /**
   * Thread safety-compression export
   */
  public static final Gson GSON = new GsonBuilder()
      //.serializeNulls()
      .setDateFormat(DATE_FMT)
      .serializeSpecialFloatingPointValues() // Fix: NaN is not a valid double value as per JSON specification
      .registerTypeAdapter(LocalDateTime.class,
          (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(
              src.format(DateTimeFormatter.ofPattern(DATE_FMT))))
      .registerTypeAdapter(LocalDateTime.class,
          (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
            String datetime = json.getAsJsonPrimitive().getAsString();
            return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern(DATE_FMT));
          })
      .registerTypeAdapter(LocalDate.class,
          (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(
              src.format(DateTimeFormatter.ofPattern(DATE_FMT_4))))
      .registerTypeAdapter(LocalDate.class,
          (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> {
            String datetime = json.getAsJsonPrimitive().getAsString();
            return LocalDate.parse(datetime, DateTimeFormatter.ofPattern(DATE_FMT_4));
          })
      .create();

  /**
   * Thread safety-formalized export
   */
  public static final Gson FORMAT_GSON = new GsonBuilder()
      //.serializeNulls()
      .setDateFormat(DATE_FMT)
      .registerTypeAdapter(LocalDateTime.class,
          (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) -> new JsonPrimitive(
              src.format(DateTimeFormatter.ofPattern(DATE_FMT))))
      .registerTypeAdapter(LocalDateTime.class,
          (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
            String datetime = json.getAsJsonPrimitive().getAsString();
            return LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern(DATE_FMT));
          })
      .registerTypeAdapter(LocalDate.class,
          (JsonSerializer<LocalDate>) (src, typeOfSrc, context) -> new JsonPrimitive(
              src.format(DateTimeFormatter.ofPattern(DATE_FMT_4))))
      .registerTypeAdapter(LocalDate.class,
          (JsonDeserializer<LocalDate>) (json, typeOfT, context) -> {
            String datetime = json.getAsJsonPrimitive().getAsString();
            return LocalDate.parse(datetime, DateTimeFormatter.ofPattern(DATE_FMT_4));
          })
      .setPrettyPrinting()
      .create();


  public static Gson getGson() {
    return GSON;
  }

  public static String toJson(Object object) {
    return GSON.toJson(object);
  }

  public static <T> T fromJson(String json, Class<T> cls) {
    return GSON.fromJson(json, cls);
  }

  public static <T> T fromJson(String json, Type type) {
    return GSON.fromJson(json, type);
  }

  public static Gson getFormatGson() {
    return FORMAT_GSON;
  }

  public static String toFormatJson(Object object) {
    return FORMAT_GSON.toJson(object);
  }

}
