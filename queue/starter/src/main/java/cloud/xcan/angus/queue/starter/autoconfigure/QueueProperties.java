package cloud.xcan.angus.queue.starter.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "angus.queue")
public class QueueProperties {

  private int partitions = 8;
  private int pollBatch = 100;
  private int ackBatch = 200;
  private int leaseSeconds = 30;
  private int reclaimBatch = 500;
  private int deadLetterMoveBatch = 200;

  private final Scheduling scheduling = new Scheduling();
  private Admin admin = new Admin();

  public int getPartitions() {
    return partitions;
  }

  public void setPartitions(int partitions) {
    this.partitions = partitions;
  }

  public int getPollBatch() {
    return pollBatch;
  }

  public void setPollBatch(int pollBatch) {
    this.pollBatch = pollBatch;
  }

  public int getAckBatch() {
    return ackBatch;
  }

  public void setAckBatch(int ackBatch) {
    this.ackBatch = ackBatch;
  }

  public int getLeaseSeconds() {
    return leaseSeconds;
  }

  public void setLeaseSeconds(int leaseSeconds) {
    this.leaseSeconds = leaseSeconds;
  }

  public int getReclaimBatch() {
    return reclaimBatch;
  }

  public void setReclaimBatch(int reclaimBatch) {
    this.reclaimBatch = reclaimBatch;
  }

  public int getDeadLetterMoveBatch() {
    return deadLetterMoveBatch;
  }

  public void setDeadLetterMoveBatch(int deadLetterMoveBatch) {
    this.deadLetterMoveBatch = deadLetterMoveBatch;
  }

  public Scheduling getScheduling() {
    return scheduling;
  }

  public Admin getAdmin() {
    return admin;
  }

  public void setAdmin(Admin admin) {
    this.admin = admin;
  }

  public static class Scheduling {

    private int poolSize = 4;
    private String threadNamePrefix = "queue-scheduler-";

    public int getPoolSize() {
      return poolSize;
    }

    public void setPoolSize(int poolSize) {
      this.poolSize = poolSize;
    }

    public String getThreadNamePrefix() {
      return threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
      this.threadNamePrefix = threadNamePrefix;
    }
  }

  public static class Admin {

    private int retentionDays = 7; // days to keep soft-deleted DLQ
    private long purgeIntervalMs = 600_000; // 10 minutes by default

    public int getRetentionDays() {
      return retentionDays;
    }

    public void setRetentionDays(int retentionDays) {
      this.retentionDays = retentionDays;
    }

    public long getPurgeIntervalMs() {
      return purgeIntervalMs;
    }

    public void setPurgeIntervalMs(long purgeIntervalMs) {
      this.purgeIntervalMs = purgeIntervalMs;
    }
  }
}
