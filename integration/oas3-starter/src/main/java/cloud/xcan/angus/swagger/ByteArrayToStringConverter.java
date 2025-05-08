package cloud.xcan.angus.swagger;

import java.io.IOException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

/**
 * Resolves the encoding problem of SpringDoc endpoint /v3/api-docs.
 *
 * <a href="https://github.com/springdoc/springdoc-openapi/issues/2475" />
 */
public class ByteArrayToStringConverter extends AbstractHttpMessageConverter<byte[]> {

  public ByteArrayToStringConverter() {
    super(MediaType.APPLICATION_JSON);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return byte[].class.isAssignableFrom(clazz);
  }

  @Override
  protected byte[] readInternal(Class<? extends byte[]> clazz, HttpInputMessage inputMessage)
      throws IOException {
    return inputMessage.getBody().readAllBytes();
  }

  @Override
  protected void writeInternal(byte[] bytes, HttpOutputMessage outputMessage) throws IOException {
    outputMessage.getBody().write(bytes);
  }
}
