
package cloud.xcan.sdf.core.utils;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.api.search.SearchOperation;
import cloud.xcan.sdf.spec.experimental.Identity;
import cloud.xcan.sdf.spec.utils.ReflectionUtils;
import cloud.xcan.sdf.spec.utils.StringUtils;
import cloud.xcan.sdf.validator.ID;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.ObjectUtils;

/**
 * @author liuxiaolong
 */
public class BeanFieldUtils {

  public static String getSearchFields(String search, Class<?> clz) {
    if (ObjectUtils.isEmpty(search)) {
      return null;
    }

    StringBuilder result = new StringBuilder();
    String[] searchFields = search.replaceAll("\\s*", "").split(",");
    Field[] clzFields = clz.getDeclaredFields();
    if (ObjectUtils.isEmpty(clzFields)) {
      return null;
    }
    for (String searchField : searchFields) {
      if (ObjectUtils.isEmpty(searchField)) {
        continue;
      }
      for (Field clzField : clzFields) {
        if (searchField.equalsIgnoreCase(clzField.getName())) {
          result.append(StringUtils.camelToUnder(searchField)).append(",");
          break;
        }
      }
    }
    String resultStr = result.toString();
    return ObjectUtils.isEmpty(resultStr) ? null :
        resultStr.substring(0, resultStr.length() - 1);
  }

  public static <T> T search2Entity(String search, Class<T> clazz)
      throws IllegalAccessException, InstantiationException {
    T obj = clazz.newInstance();
    if (Objects.nonNull(search) && search.contains("=")) {
      String[] pairs = StringUtils.split(search, "&");
      Objects.requireNonNull(pairs);
      for (String pair : pairs) {
        if (pair.contains("=")) {
          String[] kv = pair.split("=");
          String key = kv[0];
          Object value = kv[1];
          if (Objects.nonNull(key)) {
            Field field = ReflectionUtils.findField(clazz, key);
            if (Objects.nonNull(field)) {
              field.set(obj, value);
            }
          }
        }
      }
    }
    return obj;
  }

  public static boolean hasProperty(Object entity, String name) {
    Field field = FieldUtils.getField(entity.getClass(), name, true);
    return field != null;
  }

  public static void setPropertyValue(Object entity, String fileName, Object value)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = entity.getClass().getDeclaredField(fileName);
    field.setAccessible(true);
    field.set(entity, value);
  }

  public static List<String> getPropertyNames(Class<?> clz) {
    return Arrays.stream(FieldUtils.getAllFields(clz)).map(Field::getName)
        .collect(Collectors.toList());
  }

  public static Set<String> getIdAnnotationPropertyNames(Class<?> clz) {
    Field[] fields = FieldUtils.getAllFields(clz);
    Set<String> names = new HashSet<>();
    for (Field field : fields) {
      if (field.isAnnotationPresent(ID.class) || field.isAnnotationPresent(Identity.class)) {
        names.add(field.getName());
      }
    }
    return names;
  }

  public static <T> Set<SearchCriteria> getDtoSearchCriteria(final Field[] fields, T dto) {
    Set<SearchCriteria> set = new HashSet<>(fields.length);
    for (Field field : fields) {
      field.setAccessible(true);
      Object fieldValue = ReflectionUtils.getField(field, dto);
      if (Objects.nonNull(fieldValue)) {
        set.add(new SearchCriteria(field.getName(), fieldValue, SearchOperation.EQUAL));
      }
    }
    return set;
  }

  @SneakyThrows
  public static String[] getNullPropertyNames(Object source) {
    Field[] fields = FieldUtils.getAllFields(source.getClass());
    Set<String> emptyNames = new HashSet<>();
    for (Field field : fields) {
      field.setAccessible(true);
      Object value = field.get(source);
      if (Objects.isNull(value)) {
        emptyNames.add(field.getName());
      }
    }
    String[] result = new String[emptyNames.size()];
    return emptyNames.toArray(result);
  }

}
