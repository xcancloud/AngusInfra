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
package cloud.xcan.sdf.spec.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import org.junit.Test;

public class ClockTest {

  @Test
  public void userTimeClock() {
    final Clock.UserTimeClock clock = new Clock.UserTimeClock();

    assertThat((double) clock.getTime())
        .isEqualTo(System.currentTimeMillis(),
            offset(100.0));

    assertThat((double) clock.getTick())
        .isEqualTo(System.nanoTime(),
            offset(1000000.0));
  }

  @Test
  public void defaultsToUserTime() {
    assertThat(Clock.defaultClock())
        .isInstanceOf(Clock.UserTimeClock.class);
  }
}
