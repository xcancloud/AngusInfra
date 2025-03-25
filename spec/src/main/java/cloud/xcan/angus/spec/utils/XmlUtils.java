package cloud.xcan.angus.spec.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.StringWriter;

public class XmlUtils {

  public static String objectsToXml(Object value, Class<?> clz) throws JAXBException {
    StringWriter writer = new StringWriter();
    JAXBContext context = JAXBContext.newInstance(clz);
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    marshaller.marshal(value, writer);
    return writer.toString();
  }

}
