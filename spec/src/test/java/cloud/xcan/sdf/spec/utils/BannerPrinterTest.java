package cloud.xcan.sdf.spec.utils;

import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class BannerPrinterTest {

  @Test
  public void testLoadDefaultResource() {
    BannerPrinter printer = new BannerPrinter();
    Assert.assertNotNull(printer);
    Assert.assertNotNull(printer.getBanner());
    Assert.assertTrue(new File(printer.getBanner().getFile()).exists());
  }

}
