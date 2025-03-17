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

/**
 * An abstraction for how time passes. It is passed to {@link Timer} to track timing.
 */
public abstract class Clock {

  /**
   * The default clock to use.
   *
   * @return the default {@link Clock} instance
   * @see UserTimeClock
   */
  public static Clock defaultClock() {
    return UserTimeClockHolder.DEFAULT;
  }

  /**
   * Returns the current time tick.
   *
   * @return time tick in nanoseconds
   */
  public abstract long getTick();

  /**
   * Returns the current time in milliseconds.
   *
   * @return time in milliseconds
   */
  public long getTime() {
    return System.currentTimeMillis();
  }

  /**
   * A clock implementation which returns the current time in epoch nanoseconds.
   */
  public static class UserTimeClock extends Clock {

    @Override
    public long getTick() {
      return System.nanoTime();
    }
  }

  private static class UserTimeClockHolder {

    private static final Clock DEFAULT = new UserTimeClock();
  }
}
