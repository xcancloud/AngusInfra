package cloud.xcan.angus.queue.starter.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QueuePropertiesTest {

  @Test
  void nestedSchedulingAndAdminRoundTrip() {
    QueueProperties p = new QueueProperties();
    p.setPartitions(16);
    p.setPollBatch(20);
    p.setLeaseSeconds(45);
    p.setReclaimBatch(300);
    p.setDeadLetterMoveBatch(150);
    p.getScheduling().setPoolSize(8);
    p.getScheduling().setThreadNamePrefix("q-");
    p.getAdmin().setRetentionDays(14);
    p.getAdmin().setPurgeIntervalMs(120_000L);

    assertEquals(16, p.getPartitions());
    assertEquals(20, p.getPollBatch());
    assertEquals(45, p.getLeaseSeconds());
    assertEquals(300, p.getReclaimBatch());
    assertEquals(150, p.getDeadLetterMoveBatch());
    assertEquals(8, p.getScheduling().getPoolSize());
    assertEquals("q-", p.getScheduling().getThreadNamePrefix());
    assertEquals(14, p.getAdmin().getRetentionDays());
    assertEquals(120_000L, p.getAdmin().getPurgeIntervalMs());
  }
}
