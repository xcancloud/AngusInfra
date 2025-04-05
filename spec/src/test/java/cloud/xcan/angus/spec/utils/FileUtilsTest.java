package cloud.xcan.angus.spec.utils;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class FileUtilsTest {

  @Test
  public void testReadFileWithNum() throws IOException {
    URL resource = ClassLoader.getSystemResource("readfile.log");
    List<String> rows = FileUtils.readLines(resource.getPath(), 0, 2);
    Assert.assertEquals(rows, List.of("row1", "row2"));
  }

  @Test
  public void testReadFile() throws IOException {
    URL resource = ClassLoader.getSystemResource("readfile.log");
    List<String> rows = FileUtils.readLines(resource.getPath(), 3);
    Assert.assertEquals(rows, List.of("row4", "row5"));
  }

}
