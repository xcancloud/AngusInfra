package cloud.xcan.angus.spec.unit;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class DataSizeTest {

  @Test
  public void testParse() {
    DataSize dataSize = DataSize.parse("2GB");
    Assert.assertNotNull(dataSize);

    Assert.assertEquals(dataSize.getUnit().getMessage(), "GB");
    Assert.assertEquals(dataSize.getValue().intValue(), 2);

    dataSize = DataSize.parse("25.5 GB");
    Assert.assertNotNull(dataSize);

    Assert.assertEquals(dataSize.getUnit().getMessage(), "GB");
    Assert.assertEquals(dataSize.getValue(), 25.5D, 1);

  }

}
