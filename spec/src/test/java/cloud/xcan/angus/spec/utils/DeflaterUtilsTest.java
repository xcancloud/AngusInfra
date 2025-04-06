package cloud.xcan.angus.spec.utils;

import java.io.IOException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class DeflaterUtilsTest {

  @Test
  public void compressAndDecompressString() throws IOException {
    String data = "123哈哈";
    byte[] byes = DeflaterUtils.compress(data);
    Assert.assertEquals(data, DeflaterUtils.decompress(byes));
  }

}
