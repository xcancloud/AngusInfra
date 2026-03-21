package cloud.xcan.angus.job.executor;

/**
 * Registry that maintains the allowlist of available {@link JobExecutor} beans.
 *
 * <p>Spring automatically populates a {@code Map<String, JobExecutor>} injection
 * point with all beans that implement {@code JobExecutor}, keyed by their bean
 * name.  This registry exposes a safe lookup that throws a clear
 * {@link IllegalArgumentException} when an unknown name is requested, preventing
 * external callers from directing the framework toward arbitrary Spring beans
 * (CWE-470).
 *
 * <p>The map is immutable after construction; only executors registered in the
 * application context at startup are reachable.
 */
public interface JobExecutorRegistry {

  /**
   * Returns the executor registered under the given name.
   *
   * @param beanName the Spring bean name of the executor
   * @return the corresponding executor — never {@code null}
   * @throws IllegalArgumentException if no executor exists for the given name
   */
  JobExecutor getExecutor(String beanName);
}
