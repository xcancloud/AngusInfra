package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NettySslUtilsTest {

  @Test
  void createSslContext_rejectsEmptyKeyStoreResource() {
    assertThrows(IllegalArgumentException.class,
        () -> NettySslUtils.createSslContext("pw", "", "JKS", "storePw", "classpath:dummy.jks",
            "JKS", "trustPw"));
  }

  @Test
  void createSslContext_rejectsEmptyTrustStoreResource() {
    assertThrows(IllegalArgumentException.class,
        () -> NettySslUtils.createSslContext("pw", "classpath:dummy.jks", "JKS", "storePw",
            "", "JKS", "trustPw"));
  }
}
