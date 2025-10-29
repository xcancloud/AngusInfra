package cloud.xcan.angus.queue.core.util;

public final class Partitioner {

  private Partitioner() {
  }

  /**
   * Deterministically map (topic, key) to a partition in [0, numPartitions). Ensures messages with
   * the same key go to the same partition to preserve order.
   */
  public static int partition(String topic, String key, int numPartitions) {
    String base = topic + "#" + (key == null ? "" : key);
    int h = base.hashCode();
    return Math.floorMod(h, Math.max(1, numPartitions));
  }
}

