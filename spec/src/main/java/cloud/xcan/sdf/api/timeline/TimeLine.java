package cloud.xcan.sdf.api.timeline;


public final class TimeLine {

  private final TimePoint root = new TimePoint("Root");

  private final String name;
  private int count;
  private TimePoint current = root;

  public TimeLine() {
    name = "TimeLine";
  }

  public TimeLine(String name) {
    this.name = name;
  }

  public void begin(String name) {
    addTimePoint(name);
  }

  public void begin() {
    addTimePoint("Begin");
  }

  public void addTimePoint(String name) {
    current = current.next = new TimePoint(name);
    count++;
  }

  public void addTimePoints(Object[] points) {
    if (points != null) {
      for (int i = 0; i < points.length; i++) {
        current = current.next = new TimePoint((String) points[i],
            ((Number) points[++i]).longValue());
        count++;
      }
    }
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

  public void clean() {
    root.next = null;
  }

  public Object[] getTimePoints() {
    Object[] arrays = new Object[2 * count];
    int i = 0;
    TimePoint next = root;
    while ((next = next.next) != null) {
      arrays[i++] = next.name;
      arrays[i++] = next.time;
    }
    return arrays;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(name);
    if (root.next != null) {
      sb.append('[').append(current.time - root.next.time).append(']').append("(ms)");
    }
    sb.append('{');
    TimePoint next = root;
    while ((next = next.next) != null) {
      sb.append(next.toString());
    }
    sb.append('}');
    return sb.toString();
  }

  private static class TimePoint {

    private final String name;
    private final long time;
    private transient TimePoint next;

    public TimePoint(String name) {
      this.name = name;
      this.time = System.currentTimeMillis();
    }

    public TimePoint(String name, long time) {
      this.name = name;
      this.time = time;
    }

    public void setNext(TimePoint next) {
      this.next = next;
    }

    @Override
    public String toString() {
      if (next == null) {
        return name;
      }
      return name + " --（" + (next.time - time) + "ms) --> ";
    }
  }
}
