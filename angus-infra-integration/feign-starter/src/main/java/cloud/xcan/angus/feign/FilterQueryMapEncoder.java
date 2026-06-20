package cloud.xcan.angus.feign;

import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import feign.Param;
import feign.QueryMapEncoder;
import feign.codec.EncodeException;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * Query map encoder that expands {@code filters} ({@link SearchCriteria} list) into indexed query
 * parameters expected by Spring MVC:
 * {@code filters[0].key=...&filters[0].op=...&filters[0].value=...}
 */
public class FilterQueryMapEncoder implements QueryMapEncoder {

  private final Map<Class<?>, ObjectParamMetadata> classToMetadata = new HashMap<>();

  @Override
  public Map<String, Object> encode(Object object) throws EncodeException {
    if (object == null) {
      return Collections.emptyMap();
    }
    try {
      ObjectParamMetadata metadata = getMetadata(object.getClass());
      Map<String, Object> propertyNameToValue = new LinkedHashMap<>();
      for (PropertyDescriptor pd : metadata.objectProperties) {
        Method method = pd.getReadMethod();
        Object value = method.invoke(object);
        if (value == null || value == object) {
          continue;
        }
        Param alias = method.getAnnotation(Param.class);
        String name = alias != null ? alias.value() : pd.getName();
        if (SearchCriteria.FILERS_KEY.equals(name) && value instanceof List<?> filters) {
          appendFilters(propertyNameToValue, filters);
        } else {
          propertyNameToValue.put(name, value);
        }
      }
      return propertyNameToValue;
    } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
      throw new EncodeException("Failure encoding object into query map", e);
    }
  }

  private void appendFilters(Map<String, Object> propertyNameToValue, List<?> filters) {
    if (CollectionUtils.isEmpty(filters)) {
      return;
    }
    int index = 0;
    for (Object item : filters) {
      if (!(item instanceof SearchCriteria criteria)) {
        continue;
      }
      if (criteria.getKey() != null) {
        propertyNameToValue.put("filters[" + index + "].key", criteria.getKey());
      }
      if (criteria.getOp() != null) {
        propertyNameToValue.put("filters[" + index + "].op", encodeFilterOp(criteria.getOp()));
      }
      if (criteria.getValue() != null) {
        propertyNameToValue.put("filters[" + index + "].value",
            encodeFilterValue(criteria.getValue()));
      }
      index++;
    }
  }

  private static String encodeFilterOp(SearchOperation op) {
    return op.getValue();
  }

  private static Object encodeFilterValue(Object value) {
    if (value instanceof Collection<?> collection) {
      return collection.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
    if (value.getClass().isArray()) {
      int length = Array.getLength(value);
      StringBuilder encoded = new StringBuilder();
      for (int i = 0; i < length; i++) {
        if (i > 0) {
          encoded.append(',');
        }
        encoded.append(Array.get(value, i));
      }
      return encoded.toString();
    }
    return value;
  }

  private ObjectParamMetadata getMetadata(Class<?> objectType) throws IntrospectionException {
    ObjectParamMetadata metadata = classToMetadata.get(objectType);
    if (metadata == null) {
      metadata = ObjectParamMetadata.parseObjectType(objectType);
      classToMetadata.put(objectType, metadata);
    }
    return metadata;
  }

  private static class ObjectParamMetadata {

    private final List<PropertyDescriptor> objectProperties;

    private ObjectParamMetadata(List<PropertyDescriptor> objectProperties) {
      this.objectProperties = Collections.unmodifiableList(objectProperties);
    }

    private static ObjectParamMetadata parseObjectType(Class<?> type)
        throws IntrospectionException {
      Map<String, PropertyDescriptor> propertyByName = new LinkedHashMap<>();
      for (Class<?> clazz = type; clazz != null && clazz != Object.class;
          clazz = clazz.getSuperclass()) {
        for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
          if (pd.getReadMethod() == null || "class".equals(pd.getName())) {
            continue;
          }
          propertyByName.putIfAbsent(pd.getName(), pd);
        }
      }
      return new ObjectParamMetadata(new ArrayList<>(propertyByName.values()));
    }
  }
}
