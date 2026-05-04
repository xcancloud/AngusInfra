package cloud.xcan.angus.job.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JobEnumsTest {

  @Test
  void jobTypeValues() {
    assertEquals(JobType.SIMPLE, JobType.valueOf("SIMPLE"));
    assertEquals(JobType.MAP_REDUCE, JobType.valueOf("MAP_REDUCE"));
    assertEquals(JobType.SHARDING, JobType.valueOf("SHARDING"));
  }

  @Test
  void jobStatusValues() {
    assertEquals(JobStatus.READY, JobStatus.valueOf("READY"));
    assertEquals(JobStatus.RUNNING, JobStatus.valueOf("RUNNING"));
    assertEquals(JobStatus.PAUSED, JobStatus.valueOf("PAUSED"));
    assertEquals(JobStatus.COMPLETED, JobStatus.valueOf("COMPLETED"));
    assertEquals(JobStatus.FAILED, JobStatus.valueOf("FAILED"));
  }

  @Test
  void executionStatusValues() {
    assertEquals(ExecutionStatus.SUCCESS, ExecutionStatus.valueOf("SUCCESS"));
    assertEquals(ExecutionStatus.FAILURE, ExecutionStatus.valueOf("FAILURE"));
    assertEquals(ExecutionStatus.RUNNING, ExecutionStatus.valueOf("RUNNING"));
  }

  @Test
  void shardStatusValues() {
    assertEquals(ShardStatus.PENDING, ShardStatus.valueOf("PENDING"));
    assertEquals(ShardStatus.RUNNING, ShardStatus.valueOf("RUNNING"));
    assertEquals(ShardStatus.COMPLETED, ShardStatus.valueOf("COMPLETED"));
    assertEquals(ShardStatus.FAILED, ShardStatus.valueOf("FAILED"));
  }
}
