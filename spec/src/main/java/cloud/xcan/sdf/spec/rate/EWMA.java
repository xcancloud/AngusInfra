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


import static java.lang.Math.exp;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * An exponentially-weighted moving average.
 *
 * @see <a href="https://static.helpsystems.com/teamquest/pdfs/tq-neil-gunther-pdf-content/ldavg1.pdf">UNIX
 * Load Average Part 1: How It Works</a>
 * @see <a href="https://static.helpsystems.com/teamquest/pdfs/tq-neil-gunther-pdf-content/ldavg2.pdf">UNIX
 * Load Average Part 2: Not Your Average Average</a>
 * @see <a href="http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average">EMA</a>
 */
public class EWMA {

  private static final int INTERVAL = 5;
  private static final double SECONDS_PER_MINUTE = 60.0;
  private static final int ONE_MINUTE = 1;
  private static final int FIVE_MINUTES = 5;
  private static final int FIFTEEN_MINUTES = 15;
  private static final double M1_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / ONE_MINUTE);
  private static final double M5_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / FIVE_MINUTES);
  private static final double M15_ALPHA = 1 - exp(-INTERVAL / SECONDS_PER_MINUTE / FIFTEEN_MINUTES);
  private final LongAdder uncounted = new LongAdder();
  private final double alpha, interval;
  private volatile boolean initialized = false;
  private volatile double rate = 0.0;

  /**
   * Create a new EWMA with a specific smoothing constant.
   *
   * @param alpha        the smoothing constant
   * @param interval     the expected tick interval
   * @param intervalUnit the time unit of the tick interval
   */
  public EWMA(double alpha, long interval, TimeUnit intervalUnit) {
    this.interval = intervalUnit.toNanos(interval);
    this.alpha = alpha;
  }

  /**
   * Creates a new EWMA which is equivalent to the UNIX one minute load average and which expects to
   * be ticked every 5 seconds.
   *
   * @return a one-minute EWMA
   */
  public static EWMA oneMinuteEwma() {
    return new EWMA(M1_ALPHA, INTERVAL, TimeUnit.SECONDS);
  }

  /**
   * Creates a new EWMA which is equivalent to the UNIX five minute load average and which expects
   * to be ticked every 5 seconds.
   *
   * @return a five-minute EWMA
   */
  public static EWMA fiveMinuteEwma() {
    return new EWMA(M5_ALPHA, INTERVAL, TimeUnit.SECONDS);
  }

  /**
   * Creates a new EWMA which is equivalent to the UNIX fifteen minute load average and which
   * expects to be ticked every 5 seconds.
   *
   * @return a fifteen-minute EWMA
   */
  public static EWMA fifteenMinuteEwma() {
    return new EWMA(M15_ALPHA, INTERVAL, TimeUnit.SECONDS);
  }

  /**
   * Update the moving average with a new value.
   *
   * @param n the new value
   */
  public void update(long n) {
    uncounted.add(n);
  }

  /**
   * Mark the passage of time and decay the current rate accordingly.
   */
  public void tick() {
    final long count = uncounted.sumThenReset();
    final double instantRate = count / interval;
    if (initialized) {
      final double oldRate = this.rate;
      rate = oldRate + (alpha * (instantRate - oldRate));
    } else {
      rate = instantRate;
      initialized = true;
    }
  }

  /**
   * Returns the rate in the given units of time.
   *
   * @param rateUnit the unit of time
   * @return the rate
   */
  public double getRate(TimeUnit rateUnit) {
    return rate * (double) rateUnit.toNanos(1);
  }
}
