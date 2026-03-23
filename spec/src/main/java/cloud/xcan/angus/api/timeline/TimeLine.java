package cloud.xcan.angus.api.timeline;

import java.util.Objects;

public final class TimeLine {

  private final TimePoint root = new TimePoint("Root");

  private final String name;
  private int count;
  private TimePoint current = root;

  public TimeLine() {
    name = "TimeLine";
  }

  public TimeLine(String name) {
    this.name = name == null ? "TimeLine" : name;
  }

  public void begin(String name) {
    addTimePoint(name);
  }

  public void begin() {
    addTimePoint("Begin");
  }

  public void addTimePoint(String name) {
    Objects.requireNonNull(name, "name");
    current = current.next = new TimePoint(name);
    count++;
  }

  /**
   * Appends pre-recorded points. {@code points} must be a flat sequence of {@code [name, timeMs, ...]}
   * with even length; each {@code time} must be a {@link Number} (epoch millis), matching
   * {@link TimePoint#TimePoint(String, long)}.
   */
  public void addTimePoints(Object[] points) {
    if (points == null || points.length == 0) {
      return;
    }
    if ((points.length & 1) != 0) {
      throw new IllegalArgumentException(
          "points must have even length [name, time, ...], was " + points.length);
    }
    for (int i = 0; i < points.length; i += 2) {
      Object nameObj = points[i];
      Object timeObj = points[i + 1];
      if (!(nameObj instanceof String)) {
        throw new IllegalArgumentException(
            "Expected String name at index " + i + ", got " + typeName(nameObj));
      }
      if (!(timeObj instanceof Number)) {
        throw new IllegalArgumentException(
            "Expected Number time at index " + (i + 1) + ", got " + typeName(timeObj));
      }
      current = current.next = new TimePoint((String) nameObj, ((Number) timeObj).longValue());
      count++;
    }
  }

  private static String typeName(Object o) {
    return o == null ? "null" : o.getClass().getName();
  }

  public TimeLine end(String name) {
    addTimePoint(name);
    return this;
  }

  public TimeLine end() {
    addTimePoint("End");
    return this;
  }

  public TimeLine successEnd() {
    addTimePoint("Success-End");
    return this;
  }

  public TimeLine failureEnd() {
    addTimePoint("Failure-End");
    return this;
  }

  public TimeLine timeoutEnd() {
    addTimePoint("Timeout-End");
    return this;
  }

  /** Clears all points and resets the cursor; safe to reuse this instance. */
  public void clean() {
    root.next = null;
    current = root;
    count = 0;
  }

  public Object[] getTimePoints() {
    Object[] arrays = new Object[count * 2];
    int i = 0;
    for (TimePoint p = root.next; p != null; p = p.next) {
      arrays[i++] = p.name;
      arrays[i++] = p.time;
    }
    return arrays;
  }

  /**
   * Total span in the bracket is from the first recorded point to the last (same as before:
   * excludes idle time between {@link TimeLine} construction and the first {@link #begin}).
   */
  @Override
  public String toString() {
    int est = name.length() + 32 + Math.max(0, count * 48);
    StringBuilder sb = new StringBuilder(est);
    sb.append(name);
    if (root.next != null) {
      sb.append('[').append(current.time - root.next.time).append(']').append("(ms)");
    }
    sb.append('{');
    for (TimePoint p = root.next; p != null; p = p.next) {
      sb.append(p.toString());
    }
    sb.append('}');
    return sb.toString();
  }

  private static class TimePoint {

    private final String name;
    private final long time;
    private TimePoint next;

    TimePoint(String name) {
      this.name = name;
      this.time = System.currentTimeMillis();
    }

    TimePoint(String name, long time) {
      this.name = name;
      this.time = time;
    }

    @Override
    public String toString() {
      if (next == null) {
        return name;
      }
      return name + " --(" + (next.time - time) + "ms)--> ";
    }
  }
}
