package cloud.xcan.sdf.spec.rate;

/**
 * A triple of moving current (current rate moving average and current max rate) as needed by {@link
 * Rater}.
 */
public interface MovingCurrent {

  /**
   * Tick the internal clock of the MovingCurrent implementation if needed (according to the
   * internal ticking interval)
   */
  void tickIfNecessary();

  /**
   * Update all three moving averages with n events having occurred since the last update.
   */
  void update(long n);

  /**
   * Returns the one-second moving current rate
   *
   * @return the one-second moving current rate
   */
  double getS1Rate();

  /**
   * Returns the one-second moving current max rate
   *
   * @return the one-second moving current max rate
   */
  double getMaxS1Rate();
}
