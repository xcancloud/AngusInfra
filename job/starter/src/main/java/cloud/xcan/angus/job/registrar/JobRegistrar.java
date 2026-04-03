package cloud.xcan.angus.job.registrar;

import cloud.xcan.angus.job.annotation.JobDefinition;
import cloud.xcan.angus.job.entity.ScheduledJob;
import cloud.xcan.angus.job.executor.JobExecutor;
import cloud.xcan.angus.job.jpa.DistributedLockRepository;
import cloud.xcan.angus.job.jpa.ScheduledJobRepository;
import cloud.xcan.angus.job.model.CreateJobRequest;
import cloud.xcan.angus.job.service.JobManagementService;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

/**
 * Scans all {@link JobExecutor} beans annotated with {@link JobDefinition} and registers them into
 * the {@code scheduled_job} table on every application startup.
 *
 * <h3>Idempotency guarantee</h3>
 * <p>If a job with the same {@code name + group} already exists the registration is silently
 * skipped, so that:
 * <ul>
 *   <li>Restarts never overwrite operator changes made via the REST API (e.g. a custom cron).</li>
 *   <li>The same application can be deployed multiple times without duplicate records.</li>
 * </ul>
 *
 * <h3>Ordering</h3>
 * <p>Runs at {@link Order @Order(10)} to execute after standard infrastructure runners
 * (datasource, JPA, etc.) are ready.
 *
 * <h3>Conditional beans</h3>
 * <p>Executors guarded by {@code @Conditional} (e.g. private-edition-only jobs) are simply absent
 * from the {@code jobExecutors} map when their condition evaluates to {@code false}, so no special
 * handling is required here.
 */
@Slf4j
@Order(10)
@RequiredArgsConstructor
public class JobRegistrar implements ApplicationRunner {

  private final JobManagementService jobManagementService;
  private final ScheduledJobRepository jobRepository;
  private final DistributedLockRepository lockRepository;

  /**
   * All {@link JobExecutor} beans present in the application context, keyed by Spring bean name.
   * Spring auto-collects them via the {@code Map<String, JobExecutor>} injection point.
   */
  private final Map<String, JobExecutor> jobExecutors;

  @Override
  public void run(ApplicationArguments args) {
    String nodeId = buildNodeId();

    // -----------------------------------------------------------------------
    // 启动阶段第一步：清理当前节点遗留的分布式锁
    //
    // 若节点在持有 job_lock 时被强制关闭（kill -9、OOM、电源故障等），这些锁会
    // 遗留在数据库中，expireTime 设得较远（如 5 分钟）。当该节点重启时，由于 nodeId
    // 使用 hostname-based 生成，重启后仍然相同，因此可以识别并删除所有由该节点
    // 加的锁，这样其他节点就可以立即获取这些 job 并重新调度。
    // -----------------------------------------------------------------------
    int locksCleaned = lockRepository.deleteByOwner(nodeId);
    if (locksCleaned > 0) {
      log.warn("Cleaned {} stale distributed lock(s) held by this node [{}] on startup",
          locksCleaned, nodeId);
    }

    // -----------------------------------------------------------------------
    // 启动阶段第二步：清理遗留的 RUNNING 脏数据（仅限当前节点）
    //
    // 正常情况下任务执行完毕后状态会恢复为 READY 或 FAILED。若当前节点在执行任务时
    // 被强制终止（kill -9、OOM、强制重启等），正在执行的任务状态会永久滞留为 RUNNING，
    // 导致调度器永远无法再次触发这些任务（调度器只轮询 READY 状态）。
    //
    // 此处只重置**该节点之前执行但未完成的 RUNNING job**（通过 JobExecutionLog
    // 判断），避免误伤其他节点正在执行的 job。
    // -----------------------------------------------------------------------
    int reset = jobRepository.resetStaleRunningJobs(nodeId);
    if (reset > 0) {
      log.warn("Reset {} stale RUNNING job(s) to READY on startup (caused by previous crash or forced shutdown)",
          reset);
    }

    int registered = 0;
    int skipped = 0;
    int failed = 0;

    for (Map.Entry<String, JobExecutor> entry : jobExecutors.entrySet()) {
      String beanName = entry.getKey();
      JobExecutor executor = entry.getValue();

      // Unwrap CGLIB / JDK proxy to reach the real class annotation
      Class<?> targetClass = resolveTargetClass(executor);
      JobDefinition def = targetClass.getAnnotation(JobDefinition.class);
      if (def == null) {
        continue;
      }

      Optional<ScheduledJob> existing =
          jobRepository.findByJobNameAndJobGroup(def.name(), def.group());

      if (existing.isPresent()) {
        log.debug("Job already registered, skipping: name='{}', group='{}'",
            def.name(), def.group());
        skipped++;
        continue;
      }

      try {
        CreateJobRequest req = buildRequest(def, beanName);
        ScheduledJob job = jobManagementService.createJob(req);

        if (def.initialDelaySeconds() > 0) {
          job.setNextExecuteTime(LocalDateTime.now().plusSeconds(def.initialDelaySeconds()));
          jobRepository.save(job);
        }

        log.info("Registered job: name='{}', group='{}', cron='{}', bean='{}'",
            def.name(), def.group(), def.cron(), beanName);
        registered++;

      } catch (Exception e) {
        log.error("Failed to register job: name='{}', group='{}', bean='{}'",
            def.name(), def.group(), beanName, e);
        failed++;
      }
    }

    log.info("Job registration complete — registered: {}, skipped (already exist): {}, failed: {}",
        registered, skipped, failed);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private CreateJobRequest buildRequest(JobDefinition def, String beanName) {
    CreateJobRequest req = new CreateJobRequest();
    req.setJobName(def.name());
    req.setJobGroup(def.group());
    req.setCronExpression(def.cron());
    req.setBeanName(beanName);
    req.setJobType(def.type());
    req.setShardingCount(def.shardingCount());
    req.setShardingParameter(def.shardingParameter().isEmpty() ? null : def.shardingParameter());
    req.setMaxRetryCount(def.maxRetryCount());
    req.setDescription(def.description());
    // 0 表示使用全局默认值（7天），-1 表示永久保留；均原样持久化，由清理 job 按规则处理
    req.setLogRetentionDays(def.logRetentionDays());
    return req;
  }

  /**
   * Resolves the real target class from a potentially proxied Spring bean.
   *
   * <p>Spring AOP (CGLIB / JDK dynamic proxy) wraps beans in proxy objects at runtime. Annotations
   * placed on the original class are not visible on the proxy class itself, so we need to unwrap
   * one level to reach the user-defined class.
   */
  private static Class<?> resolveTargetClass(Object bean) {
    try {
      // Spring AOP utility — works for both CGLIB and interface-based proxies.
      return org.springframework.aop.support.AopUtils.getTargetClass(bean);
    } catch (Exception e) {
      return bean.getClass();
    }
  }

  /**
   * 生成稳定的节点标识，格式为 {@code hostname|local-ip}。
   *
   * <p>稳定性保证：只要物理节点不变，重启后生成的 nodeId 完全相同，
   * 这允许应用启动时只删除该节点遗留的分布式锁（包括未过期的）。
   *
   * @return 节点标识字符串，格式 {@code hostname|192.168.1.100}
   */
  private String buildNodeId() {
    try {
      String hostname = java.net.InetAddress.getLocalHost().getHostName();
      String ip = java.net.InetAddress.getLocalHost().getHostAddress();
      return hostname + "|" + ip;
    } catch (java.net.UnknownHostException e) {
      // Fallback if hostname resolution fails
      log.warn("Failed to resolve hostname, using localhost fallback", e);
      return "localhost|127.0.0.1";
    }
  }
}
