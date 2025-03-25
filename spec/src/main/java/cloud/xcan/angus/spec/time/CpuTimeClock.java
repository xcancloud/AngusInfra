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
package cloud.xcan.angus.spec.time;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * A clock implementation which returns the current thread's CPU time.
 */
public class CpuTimeClock extends Clock {

  private static final ThreadMXBean THREAD_MX_BEAN = ManagementFactory.getThreadMXBean();

  @Override
  public long getTick() {
    return THREAD_MX_BEAN.getCurrentThreadCpuTime();
  }

}
