package cloud.xcan.angus.spec.properties.encoding;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;
import org.apache.commons.configuration2.convert.ValueTransformer;

public class PropertiesWriter extends PropertiesConfiguration.PropertiesWriter {

  private final ValueTransformer TRANSFORMER = new ValueTransformer() {

    @Override
    public Object transformValue(Object value) {
      //
      return String.valueOf(value);
    }
  };

  public PropertiesWriter(Writer writer, ListDelimiterHandler delHandler) {
    super(writer, delHandler);
  }

  @Override
  public void writeProperty(String key, Object value, boolean forceSingleLine) throws IOException {
    String v;

    if (value instanceof List) {
      v = null;
      List<?> values = (List<?>) value;
      if (forceSingleLine) {
        try {
          v = String.valueOf(getDelimiterHandler()
              .escapeList(values, TRANSFORMER));
        } catch (UnsupportedOperationException uoex) {
          // the handler may not support escaping lists,
          // then the list is written in multiple lines
        }
      }
      if (v == null) {
        writeProperty(key, values);
        return;
      }
    } else {
      v = String.valueOf(getDelimiterHandler().escape(value, TRANSFORMER));
    }

    write(escapeKey(key));
    write(fetchSeparator(key, value));
    write(v);

    writeln(null);
  }
}
