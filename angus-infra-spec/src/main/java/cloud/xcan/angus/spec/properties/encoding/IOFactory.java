package cloud.xcan.angus.spec.properties.encoding;

import java.io.Writer;
import org.apache.commons.configuration2.PropertiesConfiguration.DefaultIOFactory;
import org.apache.commons.configuration2.convert.ListDelimiterHandler;

/**
 * The reason for this is, the strict definition for Java's property files requires them to be in
 * ISO-8859-1 encoding, and all Unicode characters not in it will be encoded with the \UXXXX
 * escapes. So technically speaking everything works as specified.
 * <p>
 * If the library allows it (maybe with a custom writer), you could hack it to write UTF-8 and not
 * perform the escapes.
 */
public class IOFactory extends DefaultIOFactory {

  @Override
  public PropertiesWriter createPropertiesWriter(Writer out, ListDelimiterHandler handler) {
    return new PropertiesWriter(out, handler);
  }

}
