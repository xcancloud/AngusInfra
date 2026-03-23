package cloud.xcan.angus.idgen.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NetUtilsTest {

  @Test
  void localAddressIsResolved() {
    assertThat(NetUtils.localAddress).isNotNull();
    assertThat(NetUtils.getLocalAddress()).isNotBlank();
  }
}
