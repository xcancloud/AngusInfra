package cloud.xcan.angus.job.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JobPropertiesTest {

  @Test
  void defaultsAndSetters() {
    JobProperties p = new JobProperties();
    assertEquals(1_000L, p.getScanIntervalMs());
    assertEquals(300, p.getLockTimeoutSeconds());
    assertEquals(10, p.getExecutorCorePoolSize());
    assertEquals(50, p.getExecutorMaxPoolSize());
    assertEquals(1_000, p.getExecutorQueueCapacity());
    assertEquals(5, p.getSchedulerPoolSize());
    assertEquals(5, p.getRetryBackoffMinutes());
    assertEquals(30, p.getTimeoutThresholdMinutes());
    assertEquals(100, p.getMaxJobsPerScan());

    p.setScanIntervalMs(2_000);
    p.setLockTimeoutSeconds(60);
    p.setExecutorCorePoolSize(2);
    p.setExecutorMaxPoolSize(20);
    p.setExecutorQueueCapacity(500);
    p.setSchedulerPoolSize(3);
    p.setRetryBackoffMinutes(10);
    p.setTimeoutThresholdMinutes(15);
    p.setMaxJobsPerScan(50);

    assertEquals(2_000L, p.getScanIntervalMs());
    assertEquals(60, p.getLockTimeoutSeconds());
    assertEquals(50, p.getMaxJobsPerScan());
  }
}
