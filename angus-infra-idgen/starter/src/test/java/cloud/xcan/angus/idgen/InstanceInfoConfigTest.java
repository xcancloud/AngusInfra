package cloud.xcan.angus.idgen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.api.pojo.instance.InstanceType;
import cloud.xcan.angus.idgen.autoconfigure.InstanceInfoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.test.util.ReflectionTestUtils;

class InstanceInfoConfigTest {

  @Test
  void hostFallsBackToNetUtilsWhenAddressNull() {
    InstanceInfoConfig cfg = new InstanceInfoConfig();
    ServerProperties sp = mock(ServerProperties.class);
    when(sp.getAddress()).thenReturn(null);
    when(sp.getPort()).thenReturn(8080);
    cfg.setServerProperties(sp);
    ReflectionTestUtils.setField(cfg, "env", InstanceType.HOST);

    assertThat(cfg.getHost()).isNotBlank();
    assertThat(cfg.getPort()).isEqualTo("8080");
    assertThat(cfg.getEnv()).isEqualTo(InstanceType.HOST);
  }
}
