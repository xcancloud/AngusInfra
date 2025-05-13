package cloud.xcan.angus.web.supplier;

import static cloud.xcan.angus.web.JacksonAutoConfigurer.PRIMARY_OBJECT_MAPPER_BEAN_NAME;

import cloud.xcan.angus.core.spring.SpringContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import io.hypersistence.utils.hibernate.type.util.JsonConfiguration;
import io.hypersistence.utils.hibernate.type.util.ObjectMapperSupplier;

/**
 * Extend the {@link JsonType} field serialization of the ObjectMapper object.
 *
 * @see JsonConfiguration
 */
public class JsonTypeObjectMapperSupplier implements ObjectMapperSupplier {

  @Override
  public ObjectMapper get() {
    return SpringContextHolder.getBean(PRIMARY_OBJECT_MAPPER_BEAN_NAME, ObjectMapper.class);
  }
}
