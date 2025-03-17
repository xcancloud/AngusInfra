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

/**
 * A triple of moving averages (one-, five-, and fifteen-minute moving average) as needed by {@link
 * Meter}.
 * <p>
 * Included implementations are:
 * <ul>
 * <li>{@link ExponentialMovingAverages} exponential decaying average similar to the {@code top}
 * Unix command.
 * <li>{@link SlidingTimeWindowMovingAverages} simple (unweighted) moving average
 * </ul>
 */
public interface MovingAverages {

  /**
   * Tick the internal clock of the MovingAverages implementation if needed (according to the
   * internal ticking interval)
   */
  void tickIfNecessary();

  /**
   * Update all three moving averages with n events having occurred since the last update.
   */
  void update(long n);

  /**
   * Returns the one-minute moving average rate
   *
   * @return the one-minute moving average rate
   */
  double getM1Rate();

  /**
   * Returns the five-minute moving average rate
   *
   * @return the five-minute moving average rate
   */
  double getM5Rate();

  /**
   * Returns the fifteen-minute moving average rate
   *
   * @return the fifteen-minute moving average rate
   */
  double getM15Rate();
}
