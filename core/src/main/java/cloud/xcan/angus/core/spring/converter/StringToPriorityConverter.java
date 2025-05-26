package cloud.xcan.angus.core.spring.converter;

import cloud.xcan.angus.api.enums.Priority;
import org.springframework.core.convert.converter.Converter;

/**
 * Fix: Failed to convert property value of type 'java.lang.String' to required type
 * 'cloud.xcan.angus.api.enums.Priority' for property 'Priority'.
 * <p>
 * FireFox auto write `Priority: u=0` in header, and conflicts with execution, use cases, and task list queries.
 */
public class StringToPriorityConverter implements Converter<String, Priority> {

  @Override
  public Priority convert(String source) {
    try {
      return Priority.valueOf(source.toUpperCase());
    } catch (Exception e) {
      return null;
    }
  }
}
