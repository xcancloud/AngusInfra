package cloud.xcan.angus.job.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.executor.JobExecutorRegistry;
import cloud.xcan.angus.job.model.JobExecutionResult;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultJobExecutorRegistryTest {

  @Test
  @DisplayName("getExecutor returns registered executor by bean name")
  void getExecutor_found() {
    JobExecutor dummy = ctx -> JobExecutionResult.builder().success(true).build();
    JobExecutorRegistry registry = new DefaultJobExecutorRegistry(Map.of("myJob", dummy));

    assertThat(registry.getExecutor("myJob")).isSameAs(dummy);
  }

  @Test
  @DisplayName("getExecutor throws IllegalArgumentException for unknown name (security guard)")
  void getExecutor_notFound() {
    JobExecutorRegistry registry = new DefaultJobExecutorRegistry(Map.of());

    assertThatThrownBy(() -> registry.getExecutor("arbitrary.Bean"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("arbitrary.Bean");
  }

  @Test
  @DisplayName("registry only exposes executors declared at construction time")
  void registry_isImmutable() {
    Map<String, JobExecutor> initialMap = new java.util.HashMap<>();
    initialMap.put("safeExecutor", ctx -> JobExecutionResult.builder().success(true).build());
    DefaultJobExecutorRegistry registry = new DefaultJobExecutorRegistry(initialMap);

    // Modifying the original map after construction must not affect the registry.
    initialMap.put("injectedExecutor", ctx -> JobExecutionResult.builder().success(true).build());

    assertThatThrownBy(() -> registry.getExecutor("injectedExecutor"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
