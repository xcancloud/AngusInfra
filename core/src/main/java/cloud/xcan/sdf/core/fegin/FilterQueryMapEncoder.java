package cloud.xcan.sdf.core.fegin;

import cloud.xcan.sdf.api.search.SearchCriteria;
import feign.Param;
import feign.QueryMapEncoder;
import feign.codec.EncodeException;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.util.CollectionUtils;

/**
 * the query map will be generated using java beans accessible getter property as query parameter
 * names.
 * <p>
 * eg: "/uri?name={name}&number={number}"
 * <p>
 * order of included query parameters not guaranteed, and as usual, if any value is null, it will be
 * left out
 */
public class FilterQueryMapEncoder implements QueryMapEncoder {

  private final Map<Class<?>, ObjectParamMetadata> classToMetadata = new HashMap<Class<?>, ObjectParamMetadata>();

  @Override
  public Map<String, Object> encode(Object object) throws EncodeException {
    if (object == null) {
      return Collections.emptyMap();
    }
    try {
      ObjectParamMetadata metadata = getMetadata(object.getClass());
      Map<String, Object> propertyNameToValue = new HashMap<String, Object>();
      for (PropertyDescriptor pd : metadata.objectProperties) {
        Method method = pd.getReadMethod();
        Object value = method.invoke(object);
        if (value != null && value != object) {
          Param alias = method.getAnnotation(Param.class);
          String name = alias != null ? alias.value() : pd.getName();
          if ("filters".equals(name)) {
            List<SearchCriteria> filters = (List<SearchCriteria>) value;
            if (!CollectionUtils.isEmpty(filters)) {
              for (int i = 0; i < filters.size(); i++) {
                propertyNameToValue.put("filters[" + i + "].key", filters.get(i).getKey());
                propertyNameToValue.put("filters[" + i + "].op", filters.get(i).getOp());
                propertyNameToValue.put("filters[" + i + "].value", filters.get(i).getValue());
              }
            }
          } else {
            propertyNameToValue.put(name, value);
          }
        }
      }
      return propertyNameToValue;
    } catch (IllegalAccessException | IntrospectionException | InvocationTargetException e) {
      throw new EncodeException("Failure encoding object into query map", e);
    }
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
      List<PropertyDescriptor> properties = new ArrayList<PropertyDescriptor>();

      for (PropertyDescriptor pd : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
        boolean isGetterMethod = pd.getReadMethod() != null && !"class".equals(pd.getName());
        if (isGetterMethod) {
          properties.add(pd);
        }
      }

      return new ObjectParamMetadata(properties);
    }
  }
}
