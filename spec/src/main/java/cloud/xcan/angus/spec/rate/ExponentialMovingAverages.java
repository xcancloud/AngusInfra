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
package cloud.xcan.angus.spec.rate;

import cloud.xcan.angus.spec.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A triple (one, five and fifteen minutes) of exponentially-weighted moving average rates as needed
 * by {@link Meter}.
 * <p>
 * The rates have the same exponential decay factor as the fifteen-minute load average in the
 * {@code top} Unix command.
 */
public class ExponentialMovingAverages implements MovingAverages {

  public static final long TICK_INTERVAL = TimeUnit.SECONDS.toNanos(5);

  private final EWMA m1Rate = EWMA.oneMinuteEwma();
  private final EWMA m5Rate = EWMA.fiveMinuteEwma();
  private final EWMA m15Rate = EWMA.fifteenMinuteEwma();

  private final AtomicLong lastTick;
  private final Clock clock;

  /**
   * Creates a new {@link ExponentialMovingAverages}.
   */
  public ExponentialMovingAverages() {
    this(Clock.defaultClock());
  }

  /**
   * Creates a new {@link ExponentialMovingAverages}.
   */
  public ExponentialMovingAverages(Clock clock) {
    this.clock = clock;
    this.lastTick = new AtomicLong(this.clock.getTick());
  }

  @Override
  public void update(long n) {
    m1Rate.update(n);
    m5Rate.update(n);
    m15Rate.update(n);
  }

  @Override
  public void tickIfNecessary() {
    final long oldTick = lastTick.get();
    final long newTick = clock.getTick();
    final long age = newTick - oldTick;
    if (age > TICK_INTERVAL) {
      final long newIntervalStartTick = newTick - age % TICK_INTERVAL;
      if (lastTick.compareAndSet(oldTick, newIntervalStartTick)) {
        final long requiredTicks = age / TICK_INTERVAL;
        for (long i = 0; i < requiredTicks; i++) {
          m1Rate.tick();
          m5Rate.tick();
          m15Rate.tick();
        }
      }
    }
  }

  @Override
  public double getM1Rate() {
    return m1Rate.getRate(TimeUnit.SECONDS);
  }

  @Override
  public double getM5Rate() {
    return m5Rate.getRate(TimeUnit.SECONDS);
  }

  @Override
  public double getM15Rate() {
    return m15Rate.getRate(TimeUnit.SECONDS);
  }
}
