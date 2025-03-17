/*
 * Copyright (c) 2021   XCan Company
 *
 *        http://www.xcan.cloud
 *
 * The product is based on the open source project io.dropwizard.metrics
 * modified or rewritten by the XCan team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * On the basis of Apache License 2.0, other terms need to comply with
 * XCBL License restriction requirements. Detail XCBL license at:
 *
 * http://www.xcan.cloud/licenses/XCBL-1.0
 */
package cloud.xcan.sdf.spec.rate;

import static org.assertj.core.api.Assertions.offset;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class EWMATest {

  @Test
  public void aOneMinuteEWMAWithAValueOfThree() {
    final EWMA ewma = EWMA.oneMinuteEwma();
    ewma.update(3);
    ewma.tick();

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.6, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.22072766, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.08120117, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.02987224, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.01098938, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00404277, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00148725, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00054713, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00020128, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00007405, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00002724, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00001002, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00000369, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00000136, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00000050, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.00000018, offset(0.000001));
  }

  @Test
  public void aFiveMinuteEWMAWithAValueOfThree() {
    final EWMA ewma = EWMA.fiveMinuteEwma();
    ewma.update(3);
    ewma.tick();

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.6, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.49123845, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.40219203, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.32928698, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.26959738, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.22072766, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.18071653, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.14795818, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.12113791, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.09917933, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.08120117, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.06648190, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.05443077, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.04456415, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.03648604, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.02987224, offset(0.000001));
  }

  @Test
  public void aFifteenMinuteEWMAWithAValueOfThree() {
    final EWMA ewma = EWMA.fifteenMinuteEwma();
    ewma.update(3);
    ewma.tick();

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.6, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.56130419, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.52510399, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.49123845, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.45955700, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.42991879, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.40219203, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.37625345, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.35198773, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.32928698, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.30805027, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.28818318, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.26959738, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.25221023, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.23594443, offset(0.000001));

    elapseMinute(ewma);

    assertThat(ewma.getRate(TimeUnit.SECONDS)).isEqualTo(0.22072766, offset(0.000001));
  }


  private void elapseMinute(EWMA ewma) {
    for (int i = 1; i <= 12; i++) {
      ewma.tick();
    }
  }
}
