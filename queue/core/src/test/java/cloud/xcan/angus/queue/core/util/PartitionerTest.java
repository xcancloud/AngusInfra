package cloud.xcan.angus.queue.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PartitionerTest {

  @Test
  void sameTopicAndKeyMapsToSamePartition() {
    int p1 = Partitioner.partition("orders", "user-1", 8);
    int p2 = Partitioner.partition("orders", "user-1", 8);
    assertEquals(p1, p2);
    assertTrue(p1 >= 0 && p1 < 8);
  }

  @Test
  void nullKeyTreatedAsEmptyString() {
    int withNull = Partitioner.partition("t", null, 4);
    int withEmpty = Partitioner.partition("t", "", 4);
    assertEquals(withEmpty, withNull);
  }

  @Test
  void numPartitionsMinimumOne() {
    int p = Partitioner.partition("t", "k", 0);
    assertEquals(0, p);
  }
}
