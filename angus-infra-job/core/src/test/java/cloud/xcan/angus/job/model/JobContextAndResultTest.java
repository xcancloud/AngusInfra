package cloud.xcan.angus.job.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import cloud.xcan.angus.job.enums.JobType;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JobContextAndResultTest {

  @Test
  void jobContextBuilder() {
    LocalDateTime t = LocalDateTime.now();
    JobContext ctx = JobContext.builder()
        .jobId(1L)
        .jobName("n")
        .jobGroup("g")
        .jobType(JobType.SIMPLE)
        .shardingItem(0)
        .shardingParameter("p")
        .totalShardingCount(2)
        .parameters(Map.of("k", 1))
        .executeTime(t)
        .build();

    assertEquals(1L, ctx.getJobId());
    assertEquals("n", ctx.getJobName());
    assertEquals(JobType.SIMPLE, ctx.getJobType());
    assertEquals(0, ctx.getShardingItem());
    assertEquals("p", ctx.getShardingParameter());
    assertEquals(2, ctx.getTotalShardingCount());
    assertEquals(1, ctx.getParameters().get("k"));
    assertEquals(t, ctx.getExecuteTime());
  }

  @Test
  void jobExecutionResultBuilder() {
    JobExecutionResult r = JobExecutionResult.builder()
        .success(false)
        .result(null)
        .errorMessage("e")
        .executionTime(10L)
        .metrics(Map.of("rows", 5))
        .build();

    assertEquals(false, r.isSuccess());
    assertNull(r.getResult());
    assertEquals("e", r.getErrorMessage());
    assertEquals(10L, r.getExecutionTime());
    assertEquals(5, r.getMetrics().get("rows"));
  }
}
