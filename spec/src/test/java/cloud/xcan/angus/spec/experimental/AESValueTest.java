package cloud.xcan.angus.spec.experimental;

import java.util.Objects;
import org.junit.jupiter.api.Test;

public class AESValueTest {

  @Test
  public void testValue() {
    AESValue value = new AESValue("123");
    String encryptValue = value.encrypt("password").getValue();
    Assert.assertTrue(Objects.equals("{AES}tsyQ2Bo2kwHbfPV0JTSh/w==", encryptValue), "Error");
    String decryptValue = value.decrypt("password");
    Assert.assertTrue(Objects.equals("123", decryptValue), "Error");
  }

}
