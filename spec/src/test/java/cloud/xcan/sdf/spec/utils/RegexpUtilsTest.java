package cloud.xcan.sdf.spec.utils;

import org.junit.Assert;
import org.junit.Test;

public class RegexpUtilsTest {

  @Test
  public void test(){
    Assert.assertTrue(RegexpUtils.getMatcher("123", "\\d+").matches());
    Assert.assertTrue(RegexpUtils.getMatcher("123", "\\d+").matches());
  }

}
