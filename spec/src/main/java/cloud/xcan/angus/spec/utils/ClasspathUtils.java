package cloud.xcan.angus.spec.utils;


import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class ClasspathUtils {

  public static String loadFileFromClasspath(String location) {
    InputStream inputStream = loadFileInputStreamFromClasspath(location);

    if (inputStream != null) {
      try {
        return IOUtils.toString(inputStream);
      } catch (IOException e) {
        throw new RuntimeException("Could not read " + location + " from the classpath", e);
      }
    }

    throw new RuntimeException("Could not find " + location + " on the classpath");
  }

  public static InputStream loadFileInputStreamFromClasspath(String location) {
    String file = FilenameUtils.separatorsToUnix(location);

    InputStream inputStream = ClasspathUtils.class.getResourceAsStream(file);

    if (inputStream == null) {
      inputStream = ClasspathUtils.class.getClassLoader().getResourceAsStream(file);
    }

    if (inputStream == null) {
      inputStream = ClassLoader.getSystemResourceAsStream(file);
    }

    return inputStream;
  }
}
