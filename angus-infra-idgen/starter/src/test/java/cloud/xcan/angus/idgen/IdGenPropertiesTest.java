package cloud.xcan.angus.idgen;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.idgen.autoconfigure.IdGenProperties;
import org.junit.jupiter.api.Test;

class IdGenPropertiesTest {

  @Test
  void nestedDefaultsAndOverrides() {
    IdGenProperties p = new IdGenProperties();
    assertThat(p.isEnabled()).isTrue();
    assertThat(p.getUid().getTimeBits()).isEqualTo(32);
    assertThat(p.getCached().getBoostPower()).isEqualTo(2);
    assertThat(p.getBid().getInitialMapCapacity()).isEqualTo(512);

    p.setEnabled(false);
    p.getUid().setTimeBits(30);
    p.getCached().setRejectionPolicy("EXCEPTION");
    p.getBid().setMaxBatchNum(500);

    assertThat(p.isEnabled()).isFalse();
    assertThat(p.getUid().getTimeBits()).isEqualTo(30);
    assertThat(p.getCached().getRejectionPolicy()).isEqualTo("EXCEPTION");
    assertThat(p.getBid().getMaxBatchNum()).isEqualTo(500);
  }
}
