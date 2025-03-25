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

import java.util.concurrent.TimeUnit;

public class ManualClock extends Clock {

  private final long initialTicksInNanos;
  long ticksInNanos;

  public ManualClock(long initialTicksInNanos) {
    this.initialTicksInNanos = initialTicksInNanos;
    this.ticksInNanos = initialTicksInNanos;
  }

  public ManualClock() {
    this(0L);
  }

  public synchronized void addNanos(long nanos) {
    ticksInNanos += nanos;
  }

  public synchronized void addSeconds(long seconds) {
    ticksInNanos += TimeUnit.SECONDS.toNanos(seconds);
  }

  public synchronized void addMillis(long millis) {
    ticksInNanos += TimeUnit.MILLISECONDS.toNanos(millis);
  }

  public synchronized void addHours(long hours) {
    ticksInNanos += TimeUnit.HOURS.toNanos(hours);
  }

  @Override
  public synchronized long getTick() {
    return ticksInNanos;
  }

  @Override
  public synchronized long getTime() {
    return TimeUnit.NANOSECONDS.toMillis(ticksInNanos - initialTicksInNanos);
  }

}
