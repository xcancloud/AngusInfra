package cloud.xcan.angus.job.service;

import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.executor.JobExecutorRegistry;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Spring-managed implementation of {@link JobExecutorRegistry}.
 *
 * <p>Spring automatically injects all beans that implement {@link JobExecutor}
 * into the {@code Map<String, JobExecutor>} constructor parameter, keyed by their Spring bean name.
 *  This means the registry only ever contains executors that were explicitly declared as beans —
 * external callers cannot reference arbitrary ApplicationContext beans by fabricating a name
 * (CWE-470 mitigation).
 *
 * <p>The map is wrapped in an unmodifiable view so that post-construction
 * modifications are impossible.
 */
@Slf4j
@Component
public class DefaultJobExecutorRegistry implements JobExecutorRegistry {

  private final Map<String, JobExecutor> executors;

  public DefaultJobExecutorRegistry(Map<String, JobExecutor> executors) {
    this.executors = Collections.unmodifiableMap(executors);
    log.info("JobExecutorRegistry initialised with {} executor(s): {}",
        executors.size(), executors.keySet());
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if no executor is registered under {@code beanName}
   */
  @Override
  public JobExecutor getExecutor(String beanName) {
    JobExecutor executor = executors.get(beanName);
    if (executor == null) {
      throw new IllegalArgumentException(
          "No JobExecutor registered with name '" + beanName + "'. "
              + "Registered executors: " + executors.keySet());
    }
    return executor;
  }
}
