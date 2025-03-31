package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ReflectionUtils.getField;
import static cloud.xcan.angus.spec.utils.StringUtils.camelToUnder;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.reflect.FieldUtils.getAllFields;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.experimental.Identity;
import cloud.xcan.angus.spec.utils.ReflectionUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import cloud.xcan.angus.validator.ID;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;

/**
 * @author liuxiaolong
 */
public class BeanFieldUtils {

  public static String getSearchFields(String search, Class<?> clz) {
    if (isEmpty(search)) {
      return null;
    }

    StringBuilder result = new StringBuilder();
    String[] searchFields = search.replaceAll("\\s*", "").split(",");
    Field[] clzFields = clz.getDeclaredFields();
    if (isEmpty(clzFields)) {
      return null;
    }
    for (String searchField : searchFields) {
      if (isEmpty(searchField)) {
        continue;
      }
      for (Field clzField : clzFields) {
        if (searchField.equalsIgnoreCase(clzField.getName())) {
          result.append(camelToUnder(searchField)).append(",");
          break;
        }
      }
    }
    String resultStr = result.toString();
    return isEmpty(resultStr) ? null : resultStr.substring(0, resultStr.length() - 1);
  }

  public static <T> T search2Entity(String search, Class<T> clazz)
      throws IllegalAccessException, InstantiationException {
    T obj = clazz.newInstance();
    if (nonNull(search) && search.contains("=")) {
      String[] pairs = StringUtils.split(search, "&");
      Objects.requireNonNull(pairs);
      for (String pair : pairs) {
        if (pair.contains("=")) {
          String[] kv = pair.split("=");
          String key = kv[0];
          Object value = kv[1];
          if (nonNull(key)) {
            Field field = ReflectionUtils.findField(clazz, key);
            if (nonNull(field)) {
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
    return Arrays.stream(getAllFields(clz)).map(Field::getName).collect(Collectors.toList());
  }

  public static Set<String> getIdAnnotationPropertyNames(Class<?> clz) {
    Field[] fields = getAllFields(clz);
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
      Object fieldValue = getField(field, dto);
      if (nonNull(fieldValue)) {
        set.add(new SearchCriteria(field.getName(), fieldValue, SearchOperation.EQUAL));
      }
    }
    return set;
  }

  @SneakyThrows
  public static String[] getNullPropertyNames(Object source) {
    Field[] fields = getAllFields(source.getClass());
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
