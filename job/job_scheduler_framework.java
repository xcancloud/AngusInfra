// ==================== 1. 核心实体类 ====================

// Job实体
@Entity
@Table(name = "scheduled_job")
public class ScheduledJob {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String jobName;
    private String jobGroup;
    private String cronExpression;
    private String beanName; // Spring Bean名称
    private String methodName;
    
    @Enumerated(EnumType.STRING)
    private JobType jobType; // SIMPLE, MAP_REDUCE, SHARDING
    
    @Enumerated(EnumType.STRING)
    private JobStatus status; // READY, RUNNING, PAUSED, COMPLETED, FAILED
    
    private Integer shardingCount; // 分片数量
    private String shardingParameter; // 分片参数
    
    private Integer retryCount;
    private Integer maxRetryCount;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime lastExecuteTime;
    private LocalDateTime nextExecuteTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // Getters and Setters
}

// Job执行记录
@Entity
@Table(name = "job_execution_log")
public class JobExecutionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long jobId;
    private String jobName;
    private Integer shardingItem; // 分片项
    
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status; // SUCCESS, FAILURE, RUNNING
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long executionTime; // 执行时长(毫秒)
    
    @Column(columnDefinition = "TEXT")
    private String result;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    private String executorNode; // 执行节点
    
    // Getters and Setters
}

// 分布式锁实体
@Entity
@Table(name = "distributed_lock")
public class DistributedLock {
    @Id
    private String lockKey;
    private String lockValue;
    private String owner;
    private LocalDateTime acquireTime;
    private LocalDateTime expireTime;
    
    @Version
    private Long version; // 乐观锁
    
    // Getters and Setters
}

// MapReduce任务分片
@Entity
@Table(name = "job_shard")
public class JobShard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long jobId;
    private Integer shardingItem;
    private String shardingParameter;
    
    @Enumerated(EnumType.STRING)
    private ShardStatus status; // PENDING, RUNNING, COMPLETED, FAILED
    
    @Column(columnDefinition = "TEXT")
    private String mapResult; // Map阶段结果
    
    private String executorNode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // Getters and Setters
}

// ==================== 2. 枚举类型 ====================

public enum JobType {
    SIMPLE,      // 简单任务
    MAP_REDUCE,  // MapReduce任务
    SHARDING     // 分片任务
}

public enum JobStatus {
    READY, RUNNING, PAUSED, COMPLETED, FAILED
}

public enum ExecutionStatus {
    SUCCESS, FAILURE, RUNNING, TIMEOUT
}

public enum ShardStatus {
    PENDING, RUNNING, COMPLETED, FAILED
}

// ==================== 3. JPA Repository ====================

@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {
    List<ScheduledJob> findByStatus(JobStatus status);
    List<ScheduledJob> findByStatusAndNextExecuteTimeBefore(JobStatus status, LocalDateTime time);
    Optional<ScheduledJob> findByJobNameAndJobGroup(String jobName, String jobGroup);
}

@Repository
public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, Long> {
    List<JobExecutionLog> findByJobIdOrderByStartTimeDesc(Long jobId);
    List<JobExecutionLog> findByStatusAndStartTimeAfter(ExecutionStatus status, LocalDateTime time);
}

@Repository
public interface DistributedLockRepository extends JpaRepository<DistributedLock, String> {
    @Query("SELECT l FROM DistributedLock l WHERE l.lockKey = :lockKey AND l.expireTime > :now")
    Optional<DistributedLock> findValidLock(@Param("lockKey") String lockKey, 
                                            @Param("now") LocalDateTime now);
}

@Repository
public interface JobShardRepository extends JpaRepository<JobShard, Long> {
    List<JobShard> findByJobIdAndStatus(Long jobId, ShardStatus status);
    List<JobShard> findByJobId(Long jobId);
}

// ==================== 4. 分布式锁服务 ====================

@Service
@Slf4j
public class DistributedLockService {
    
    @Autowired
    private DistributedLockRepository lockRepository;
    
    private static final int DEFAULT_LOCK_TIMEOUT = 30; // 秒
    
    /**
     * 尝试获取锁
     */
    @Transactional
    public boolean tryLock(String lockKey, String owner) {
        return tryLock(lockKey, owner, DEFAULT_LOCK_TIMEOUT);
    }
    
    @Transactional
    public boolean tryLock(String lockKey, String owner, int timeoutSeconds) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Optional<DistributedLock> existingLock = lockRepository.findValidLock(lockKey, now);
            
            if (existingLock.isPresent()) {
                // 锁已被占用
                return false;
            }
            
            // 清理过期锁
            lockRepository.deleteById(lockKey);
            
            // 创建新锁
            DistributedLock lock = new DistributedLock();
            lock.setLockKey(lockKey);
            lock.setLockValue(UUID.randomUUID().toString());
            lock.setOwner(owner);
            lock.setAcquireTime(now);
            lock.setExpireTime(now.plusSeconds(timeoutSeconds));
            
            lockRepository.save(lock);
            log.info("Lock acquired: {} by {}", lockKey, owner);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to acquire lock: {}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 释放锁
     */
    @Transactional
    public void unlock(String lockKey, String owner) {
        try {
            Optional<DistributedLock> lock = lockRepository.findById(lockKey);
            if (lock.isPresent() && lock.get().getOwner().equals(owner)) {
                lockRepository.deleteById(lockKey);
                log.info("Lock released: {} by {}", lockKey, owner);
            }
        } catch (Exception e) {
            log.error("Failed to release lock: {}", lockKey, e);
        }
    }
    
    /**
     * 续期锁
     */
    @Transactional
    public boolean renewLock(String lockKey, String owner, int timeoutSeconds) {
        try {
            Optional<DistributedLock> lock = lockRepository.findById(lockKey);
            if (lock.isPresent() && lock.get().getOwner().equals(owner)) {
                DistributedLock existingLock = lock.get();
                existingLock.setExpireTime(LocalDateTime.now().plusSeconds(timeoutSeconds));
                lockRepository.save(existingLock);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Failed to renew lock: {}", lockKey, e);
            return false;
        }
    }
}

// ==================== 5. Job执行器接口 ====================

/**
 * Job执行器接口
 */
public interface JobExecutor {
    /**
     * 执行任务
     */
    JobExecutionResult execute(JobContext context);
}

/**
 * MapReduce任务执行器
 */
public interface MapReduceJobExecutor extends JobExecutor {
    /**
     * Map阶段
     */
    List<String> map(JobContext context, int shardingItem, String shardingParameter);
    
    /**
     * Reduce阶段
     */
    String reduce(JobContext context, List<String> mapResults);
}

/**
 * 分片任务执行器
 */
public interface ShardingJobExecutor extends JobExecutor {
    /**
     * 执行分片
     */
    JobExecutionResult executeSharding(JobContext context, int shardingItem, String shardingParameter);
}

// ==================== 6. Job上下文 ====================

@Data
@Builder
public class JobContext {
    private Long jobId;
    private String jobName;
    private String jobGroup;
    private JobType jobType;
    private Integer shardingItem;
    private String shardingParameter;
    private Integer totalShardingCount;
    private Map<String, Object> parameters;
    private LocalDateTime executeTime;
}

@Data
@Builder
public class JobExecutionResult {
    private boolean success;
    private String result;
    private String errorMessage;
    private Long executionTime;
    private Map<String, Object> metrics;
}

// ==================== 7. Job调度核心服务 ====================

@Service
@Slf4j
public class JobSchedulerService {
    
    @Autowired
    private ScheduledJobRepository jobRepository;
    
    @Autowired
    private JobExecutionLogRepository executionLogRepository;
    
    @Autowired
    private JobShardRepository shardRepository;
    
    @Autowired
    private DistributedLockService lockService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private String nodeId = UUID.randomUUID().toString();
    
    /**
     * 扫描并执行到期任务
     */
    @Scheduled(fixedDelay = 1000) // 每秒扫描
    public void scanAndExecuteJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledJob> jobs = jobRepository.findByStatusAndNextExecuteTimeBefore(
            JobStatus.READY, now
        );
        
        for (ScheduledJob job : jobs) {
            executeJobWithLock(job);
        }
    }
    
    /**
     * 带分布式锁的任务执行
     */
    private void executeJobWithLock(ScheduledJob job) {
        String lockKey = "job_lock_" + job.getId();
        
        if (lockService.tryLock(lockKey, nodeId, 300)) {
            try {
                executeJob(job);
            } finally {
                lockService.unlock(lockKey, nodeId);
            }
        } else {
            log.debug("Job {} is locked by another node", job.getJobName());
        }
    }
    
    /**
     * 执行任务
     */
    @Transactional
    public void executeJob(ScheduledJob job) {
        log.info("Executing job: {}", job.getJobName());
        
        // 更新任务状态
        job.setStatus(JobStatus.RUNNING);
        job.setLastExecuteTime(LocalDateTime.now());
        jobRepository.save(job);
        
        try {
            switch (job.getJobType()) {
                case SIMPLE:
                    executeSimpleJob(job);
                    break;
                case MAP_REDUCE:
                    executeMapReduceJob(job);
                    break;
                case SHARDING:
                    executeShardingJob(job);
                    break;
            }
            
            // 更新下次执行时间
            updateNextExecuteTime(job);
            job.setStatus(JobStatus.READY);
            job.setRetryCount(0);
            
        } catch (Exception e) {
            log.error("Job execution failed: {}", job.getJobName(), e);
            handleJobFailure(job, e);
        } finally {
            jobRepository.save(job);
        }
    }
    
    /**
     * 执行简单任务
     */
    private void executeSimpleJob(ScheduledJob job) {
        long startTime = System.currentTimeMillis();
        JobExecutionLog executionLog = createExecutionLog(job, null);
        
        try {
            JobExecutor executor = getJobExecutor(job);
            JobContext context = buildJobContext(job, null);
            
            JobExecutionResult result = executor.execute(context);
            
            executionLog.setStatus(result.isSuccess() ? 
                ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE);
            executionLog.setResult(result.getResult());
            executionLog.setErrorMessage(result.getErrorMessage());
            
        } catch (Exception e) {
            executionLog.setStatus(ExecutionStatus.FAILURE);
            executionLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            executionLog.setEndTime(LocalDateTime.now());
            executionLog.setExecutionTime(System.currentTimeMillis() - startTime);
            executionLogRepository.save(executionLog);
        }
    }
    
    /**
     * 执行MapReduce任务
     */
    private void executeMapReduceJob(ScheduledJob job) {
        log.info("Executing MapReduce job: {}", job.getJobName());
        
        // 1. 创建分片
        List<JobShard> shards = createShards(job);
        
        // 2. 执行Map阶段（并行）
        ExecutorService executor = Executors.newFixedThreadPool(job.getShardingCount());
        List<Future<String>> futures = new ArrayList<>();
        
        for (JobShard shard : shards) {
            Future<String> future = executor.submit(() -> executeMapPhase(job, shard));
            futures.add(future);
        }
        
        // 3. 收集Map结果
        List<String> mapResults = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                mapResults.add(future.get(5, TimeUnit.MINUTES));
            } catch (Exception e) {
                log.error("Map phase failed", e);
                throw new RuntimeException("Map phase failed", e);
            }
        }
        executor.shutdown();
        
        // 4. 执行Reduce阶段
        executeReducePhase(job, mapResults);
    }
    
    /**
     * 执行Map阶段
     */
    private String executeMapPhase(ScheduledJob job, JobShard shard) {
        shard.setStatus(ShardStatus.RUNNING);
        shard.setExecutorNode(nodeId);
        shard.setStartTime(LocalDateTime.now());
        shardRepository.save(shard);
        
        try {
            MapReduceJobExecutor executor = (MapReduceJobExecutor) getJobExecutor(job);
            JobContext context = buildJobContext(job, shard.getShardingItem());
            
            List<String> mapResult = executor.map(context, 
                shard.getShardingItem(), shard.getShardingParameter());
            
            String result = String.join(",", mapResult);
            shard.setMapResult(result);
            shard.setStatus(ShardStatus.COMPLETED);
            
            return result;
            
        } catch (Exception e) {
            shard.setStatus(ShardStatus.FAILED);
            throw e;
        } finally {
            shard.setEndTime(LocalDateTime.now());
            shardRepository.save(shard);
        }
    }
    
    /**
     * 执行Reduce阶段
     */
    private void executeReducePhase(ScheduledJob job, List<String> mapResults) {
        long startTime = System.currentTimeMillis();
        JobExecutionLog executionLog = createExecutionLog(job, -1); // -1表示Reduce阶段
        
        try {
            MapReduceJobExecutor executor = (MapReduceJobExecutor) getJobExecutor(job);
            JobContext context = buildJobContext(job, null);
            
            String result = executor.reduce(context, mapResults);
            
            executionLog.setStatus(ExecutionStatus.SUCCESS);
            executionLog.setResult(result);
            
        } catch (Exception e) {
            executionLog.setStatus(ExecutionStatus.FAILURE);
            executionLog.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            executionLog.setEndTime(LocalDateTime.now());
            executionLog.setExecutionTime(System.currentTimeMillis() - startTime);
            executionLogRepository.save(executionLog);
        }
    }
    
    /**
     * 执行分片任务
     */
    private void executeShardingJob(ScheduledJob job) {
        List<JobShard> shards = createShards(job);
        
        ExecutorService executor = Executors.newFixedThreadPool(job.getShardingCount());
        List<Future<Void>> futures = new ArrayList<>();
        
        for (JobShard shard : shards) {
            Future<Void> future = executor.submit(() -> {
                executeShardingItem(job, shard);
                return null;
            });
            futures.add(future);
        }
        
        // 等待所有分片完成
        for (Future<Void> future : futures) {
            try {
                future.get(10, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.error("Sharding execution failed", e);
            }
        }
        executor.shutdown();
    }
    
    /**
     * 执行单个分片
     */
    private void executeShardingItem(ScheduledJob job, JobShard shard) {
        long startTime = System.currentTimeMillis();
        JobExecutionLog executionLog = createExecutionLog(job, shard.getShardingItem());
        
        shard.setStatus(ShardStatus.RUNNING);
        shard.setExecutorNode(nodeId);
        shard.setStartTime(LocalDateTime.now());
        shardRepository.save(shard);
        
        try {
            ShardingJobExecutor executor = (ShardingJobExecutor) getJobExecutor(job);
            JobContext context = buildJobContext(job, shard.getShardingItem());
            
            JobExecutionResult result = executor.executeSharding(context, 
                shard.getShardingItem(), shard.getShardingParameter());
            
            shard.setStatus(result.isSuccess() ? 
                ShardStatus.COMPLETED : ShardStatus.FAILED);
            
            executionLog.setStatus(result.isSuccess() ? 
                ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE);
            executionLog.setResult(result.getResult());
            executionLog.setErrorMessage(result.getErrorMessage());
            
        } catch (Exception e) {
            shard.setStatus(ShardStatus.FAILED);
            executionLog.setStatus(ExecutionStatus.FAILURE);
            executionLog.setErrorMessage(e.getMessage());
        } finally {
            shard.setEndTime(LocalDateTime.now());
            shardRepository.save(shard);
            
            executionLog.setEndTime(LocalDateTime.now());
            executionLog.setExecutionTime(System.currentTimeMillis() - startTime);
            executionLogRepository.save(executionLog);
        }
    }
    
    /**
     * 创建分片
     */
    private List<JobShard> createShards(ScheduledJob job) {
        // 清理旧分片
        List<JobShard> oldShards = shardRepository.findByJobId(job.getId());
        shardRepository.deleteAll(oldShards);
        
        List<JobShard> shards = new ArrayList<>();
        String[] parameters = job.getShardingParameter() != null ? 
            job.getShardingParameter().split(",") : new String[job.getShardingCount()];
        
        for (int i = 0; i < job.getShardingCount(); i++) {
            JobShard shard = new JobShard();
            shard.setJobId(job.getId());
            shard.setShardingItem(i);
            shard.setShardingParameter(i < parameters.length ? parameters[i] : "");
            shard.setStatus(ShardStatus.PENDING);
            shards.add(shard);
        }
        
        return shardRepository.saveAll(shards);
    }
    
    /**
     * 获取Job执行器
     */
    private JobExecutor getJobExecutor(ScheduledJob job) {
        try {
            return (JobExecutor) applicationContext.getBean(job.getBeanName());
        } catch (Exception e) {
            throw new RuntimeException("Job executor not found: " + job.getBeanName(), e);
        }
    }
    
    /**
     * 构建Job上下文
     */
    private JobContext buildJobContext(ScheduledJob job, Integer shardingItem) {
        return JobContext.builder()
            .jobId(job.getId())
            .jobName(job.getJobName())
            .jobGroup(job.getJobGroup())
            .jobType(job.getJobType())
            .shardingItem(shardingItem)
            .totalShardingCount(job.getShardingCount())
            .executeTime(LocalDateTime.now())
            .build();
    }
    
    /**
     * 创建执行日志
     */
    private JobExecutionLog createExecutionLog(ScheduledJob job, Integer shardingItem) {
        JobExecutionLog log = new JobExecutionLog();
        log.setJobId(job.getId());
        log.setJobName(job.getJobName());
        log.setShardingItem(shardingItem);
        log.setStatus(ExecutionStatus.RUNNING);
        log.setStartTime(LocalDateTime.now());
        log.setExecutorNode(nodeId);
        return log;
    }
    
    /**
     * 更新下次执行时间
     */
    private void updateNextExecuteTime(ScheduledJob job) {
        try {
            CronExpression cron = new CronExpression(job.getCronExpression());
            Date nextTime = cron.getNextValidTimeAfter(new Date());
            job.setNextExecuteTime(LocalDateTime.ofInstant(
                nextTime.toInstant(), ZoneId.systemDefault()));
        } catch (Exception e) {
            log.error("Failed to calculate next execute time", e);
        }
    }
    
    /**
     * 处理任务失败
     */
    private void handleJobFailure(ScheduledJob job, Exception e) {
        job.setRetryCount(job.getRetryCount() + 1);
        
        if (job.getRetryCount() >= job.getMaxRetryCount()) {
            job.setStatus(JobStatus.FAILED);
            log.error("Job failed after {} retries: {}", job.getMaxRetryCount(), job.getJobName());
        } else {
            job.setStatus(JobStatus.READY);
            // 延迟重试
            job.setNextExecuteTime(LocalDateTime.now().plusMinutes(5));
        }
    }
}

// ==================== 8. Job管理服务 ====================

@Service
@Slf4j
public class JobManagementService {
    
    @Autowired
    private ScheduledJobRepository jobRepository;
    
    @Autowired
    private JobExecutionLogRepository executionLogRepository;
    
    @Autowired
    private JobShardRepository shardRepository;
    
    /**
     * 创建任务
     */
    @Transactional
    public ScheduledJob createJob(ScheduledJob job) {
        job.setStatus(JobStatus.READY);
        job.setCreateTime(LocalDateTime.now());
        job.setUpdateTime(LocalDateTime.now());
        job.setRetryCount(0);
        
        // 计算首次执行时间
        try {
            CronExpression cron = new CronExpression(job.getCronExpression());
            Date nextTime = cron.getNextValidTimeAfter(new Date());
            job.setNextExecuteTime(LocalDateTime.ofInstant(
                nextTime.toInstant(), ZoneId.systemDefault()));
        } catch (Exception e) {
            throw new RuntimeException("Invalid cron expression", e);
        }
        
        return jobRepository.save(job);
    }
    
    /**
     * 暂停任务
     */
    @Transactional
    public void pauseJob(Long jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.PAUSED);
        job.setUpdateTime(LocalDateTime.now());
        jobRepository.save(job);
    }
    
    /**
     * 恢复任务
     */
    @Transactional
    public void resumeJob(Long jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setStatus(JobStatus.READY);
        job.setUpdateTime(LocalDateTime.now());
        jobRepository.save(job);
    }
    
    /**
     * 删除任务
     */
    @Transactional
    public void deleteJob(Long jobId) {
        jobRepository.deleteById(jobId);
        // 清理相关数据
        List<JobShard> shards = shardRepository.findByJobId(jobId);
        shardRepository.deleteAll(shards);
    }
    
    /**
     * 立即执行任务
     */
    @Transactional
    public void triggerJob(Long jobId) {
        ScheduledJob job = jobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));
        job.setNextExecuteTime(LocalDateTime.now());
        jobRepository.save(job);
    }
    
    /**
     * 获取任务执行历史
     */
    public List<JobExecutionLog> getJobExecutionHistory(Long jobId) {
        return executionLogRepository.findByJobIdOrderByStartTimeDesc(jobId);
    }
    
    /**
     * 获取任务统计信息
     */
    public Map<String, Object> getJobStatistics(Long jobId) {
        List<JobExecutionLog> logs = executionLogRepository.findByJobIdOrderByStartTimeDesc(jobId);
        
        long successCount = logs.stream()
            .filter(log -> log.getStatus() == ExecutionStatus.SUCCESS)
            .count();
        
        long failureCount = logs.stream()
            .filter(log -> log.getStatus() == ExecutionStatus.FAILURE)
            .count();
        
        double avgExecutionTime = logs.stream()
            .filter(log -> log.getExecutionTime() != null)
            .mapToLong(JobExecutionLog::getExecutionTime)
            .average()
            .orElse(0.0);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExecutions", logs.size());
        stats.put("successCount", successCount);
        stats.put("failureCount", failureCount);
        stats.put("successRate", logs.isEmpty() ? 0 : (double) successCount / logs.size() * 100);
        stats.put("avgExecutionTime", avgExecutionTime);
        
        return stats;
    }
}

// ==================== 9. 示例Job实现 ====================

/**
 * 简单任务示例
 */
@Component("simpleJobExample")
@Slf4j
public class SimpleJobExample implements JobExecutor {
    
    @Override
    public JobExecutionResult execute(JobContext context) {
        log.info("Executing simple job: {}", context.getJobName());
        
        try {
            // 模拟业务逻辑
            Thread.sleep(1000);
            
            return JobExecutionResult.builder()
                .success(true)
                .result("Simple job completed successfully")
                .executionTime(1000L)
                .build();
                
        } catch (Exception e) {
            return JobExecutionResult.builder()
                .success(false)
                .errorMessage(e.getMessage())
                .build();
        }
    }
}

/**
 * MapReduce任务示例：统计用户数据
 */
@Component("userDataMapReduceJob")
@Slf4j
public class UserDataMapReduceJob implements MapReduceJobExecutor {
    
    @Override
    public JobExecutionResult execute(JobContext context) {
        // MapReduce框架会自动调用map和reduce
        return null;
    }
    
    @Override
    public List<String> map(JobContext context, int shardingItem, String shardingParameter) {
        log.info("Map phase - Shard: {}, Parameter: {}", shardingItem, shardingParameter);
        
        // 模拟处理分片数据
        List<String> results = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            results.add("user_" + (shardingItem * 100 + i) + ":active");
        }
        
        return results;
    }
    
    @Override
    public String reduce(JobContext context, List<String> mapResults) {
        log.info("Reduce phase - Aggregating {} map results", mapResults.size());
        
        // 汇总所有Map结果
        long totalUsers = mapResults.stream()
            .flatMap(result -> Arrays.stream(result.split(",")))
            .count();
        
        return "Total users processed: " + totalUsers;
    }
}

/**
 * 分片任务示例：批量发送邮件
 */
@Component("emailShardingJob")
@Slf4j
public class EmailShardingJob implements ShardingJobExecutor {
    
    @Override
    public JobExecutionResult execute(JobContext context) {
        // 框架会自动调用executeSharding
        return null;
    }
    
    @Override
    public JobExecutionResult executeSharding(JobContext context, 
                                             int shardingItem, 
                                             String shardingParameter) {
        log.info("Processing shard {} with parameter: {}", shardingItem, shardingParameter);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 模拟发送邮件
            int emailCount = 0;
            for (int i = 0; i < 50; i++) {
                String email = String.format("user_%d_%d@example.com", shardingItem, i);
                // 发送邮件逻辑
                Thread.sleep(10);
                emailCount++;
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            return JobExecutionResult.builder()
                .success(true)
                .result(String.format("Shard %d: Sent %d emails", shardingItem, emailCount))
                .executionTime(executionTime)
                .build();
                
        } catch (Exception e) {
            return JobExecutionResult.builder()
                .success(false)
                .errorMessage(e.getMessage())
                .executionTime(System.currentTimeMillis() - startTime)
                .build();
        }
    }
}

// ==================== 10. REST API Controller ====================

@RestController
@RequestMapping("/api/jobs")
@Slf4j
public class JobController {
    
    @Autowired
    private JobManagementService jobManagementService;
    
    @Autowired
    private ScheduledJobRepository jobRepository;
    
    @Autowired
    private JobSchedulerService schedulerService;
    
    /**
     * 创建任务
     */
    @PostMapping
    public ResponseEntity<ScheduledJob> createJob(@RequestBody ScheduledJob job) {
        try {
            ScheduledJob created = jobManagementService.createJob(job);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            log.error("Failed to create job", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取所有任务
     */
    @GetMapping
    public ResponseEntity<List<ScheduledJob>> getAllJobs() {
        return ResponseEntity.ok(jobRepository.findAll());
    }
    
    /**
     * 获取任务详情
     */
    @GetMapping("/{jobId}")
    public ResponseEntity<ScheduledJob> getJob(@PathVariable Long jobId) {
        return jobRepository.findById(jobId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新任务
     */
    @PutMapping("/{jobId}")
    public ResponseEntity<ScheduledJob> updateJob(@PathVariable Long jobId, 
                                                   @RequestBody ScheduledJob job) {
        return jobRepository.findById(jobId)
            .map(existing -> {
                existing.setJobName(job.getJobName());
                existing.setCronExpression(job.getCronExpression());
                existing.setBeanName(job.getBeanName());
                existing.setMethodName(job.getMethodName());
                existing.setDescription(job.getDescription());
                existing.setUpdateTime(LocalDateTime.now());
                return ResponseEntity.ok(jobRepository.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 暂停任务
     */
    @PostMapping("/{jobId}/pause")
    public ResponseEntity<Void> pauseJob(@PathVariable Long jobId) {
        try {
            jobManagementService.pauseJob(jobId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to pause job", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 恢复任务
     */
    @PostMapping("/{jobId}/resume")
    public ResponseEntity<Void> resumeJob(@PathVariable Long jobId) {
        try {
            jobManagementService.resumeJob(jobId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to resume job", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 立即执行任务
     */
    @PostMapping("/{jobId}/trigger")
    public ResponseEntity<Void> triggerJob(@PathVariable Long jobId) {
        try {
            jobManagementService.triggerJob(jobId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to trigger job", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除任务
     */
    @DeleteMapping("/{jobId}")
    public ResponseEntity<Void> deleteJob(@PathVariable Long jobId) {
        try {
            jobManagementService.deleteJob(jobId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Failed to delete job", e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取任务执行历史
     */
    @GetMapping("/{jobId}/executions")
    public ResponseEntity<List<JobExecutionLog>> getExecutionHistory(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobManagementService.getJobExecutionHistory(jobId));
    }
    
    /**
     * 获取任务统计信息
     */
    @GetMapping("/{jobId}/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable Long jobId) {
        return ResponseEntity.ok(jobManagementService.getJobStatistics(jobId));
    }
}

// ==================== 11. 配置类 ====================

@Configuration
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.example.scheduler.repository")
@EntityScan(basePackages = "com.example.scheduler.entity")
public class SchedulerConfiguration {
    
    /**
     * 配置任务执行线程池
     */
    @Bean(name = "jobExecutorPool")
    public ThreadPoolTaskExecutor jobExecutorPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("job-executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * 配置调度器线程池
     */
    @Bean(name = "schedulerPool")
    public ThreadPoolTaskScheduler schedulerPool() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(20);
        scheduler.setThreadNamePrefix("scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}

// ==================== 12. 健康监控 ====================

@Component
@Slf4j
public class JobHealthMonitor {
    
    @Autowired
    private JobExecutionLogRepository executionLogRepository;
    
    @Autowired
    private DistributedLockRepository lockRepository;
    
    /**
     * 清理过期锁
     */
    @Scheduled(fixedDelay = 60000) // 每分钟执行
    public void cleanExpiredLocks() {
        try {
            List<DistributedLock> allLocks = lockRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            
            List<DistributedLock> expiredLocks = allLocks.stream()
                .filter(lock -> lock.getExpireTime().isBefore(now))
                .collect(Collectors.toList());
            
            if (!expiredLocks.isEmpty()) {
                lockRepository.deleteAll(expiredLocks);
                log.info("Cleaned {} expired locks", expiredLocks.size());
            }
        } catch (Exception e) {
            log.error("Failed to clean expired locks", e);
        }
    }
    
    /**
     * 监控超时任务
     */
    @Scheduled(fixedDelay = 30000) // 每30秒执行
    public void monitorTimeoutJobs() {
        try {
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(30);
            List<JobExecutionLog> runningJobs = executionLogRepository
                .findByStatusAndStartTimeAfter(ExecutionStatus.RUNNING, timeoutThreshold);
            
            if (!runningJobs.isEmpty()) {
                log.warn("Found {} potentially timeout jobs", runningJobs.size());
                
                // 可以在这里添加告警逻辑
                for (JobExecutionLog job : runningJobs) {
                    Duration duration = Duration.between(job.getStartTime(), LocalDateTime.now());
                    if (duration.toMinutes() > 30) {
                        log.warn("Job {} has been running for {} minutes", 
                            job.getJobName(), duration.toMinutes());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to monitor timeout jobs", e);
        }
    }
    
    /**
     * 生成健康报告
     */
    @Scheduled(cron = "0 0 * * * *") // 每小时执行
    public void generateHealthReport() {
        try {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            
            List<JobExecutionLog> recentExecutions = executionLogRepository
                .findAll()
                .stream()
                .filter(log -> log.getStartTime().isAfter(oneHourAgo))
                .collect(Collectors.toList());
            
            long totalExecutions = recentExecutions.size();
            long successCount = recentExecutions.stream()
                .filter(log -> log.getStatus() == ExecutionStatus.SUCCESS)
                .count();
            long failureCount = recentExecutions.stream()
                .filter(log -> log.getStatus() == ExecutionStatus.FAILURE)
                .count();
            
            double successRate = totalExecutions > 0 ? 
                (double) successCount / totalExecutions * 100 : 0;
            
            log.info("=== Job Scheduler Health Report (Last Hour) ===");
            log.info("Total Executions: {}", totalExecutions);
            log.info("Success Count: {}", successCount);
            log.info("Failure Count: {}", failureCount);
            log.info("Success Rate: {:.2f}%", successRate);
            log.info("===============================================");
            
        } catch (Exception e) {
            log.error("Failed to generate health report", e);
        }
    }
}

// ==================== 13. 异常处理 ====================

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", e.getMessage());
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", e.getMessage());
        error.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(error);
    }
}

// ==================== 14. 数据库初始化脚本 ====================

/*
-- SQL Schema

CREATE TABLE scheduled_job (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name VARCHAR(255) NOT NULL,
    job_group VARCHAR(255) NOT NULL,
    cron_expression VARCHAR(255) NOT NULL,
    bean_name VARCHAR(255) NOT NULL,
    method_name VARCHAR(255),
    job_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    sharding_count INT DEFAULT 1,
    sharding_parameter TEXT,
    retry_count INT DEFAULT 0,
    max_retry_count INT DEFAULT 3,
    description TEXT,
    last_execute_time DATETIME,
    next_execute_time DATETIME,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    UNIQUE KEY uk_job_name_group (job_name, job_group),
    INDEX idx_status (status),
    INDEX idx_next_execute_time (next_execute_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE job_execution_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    job_name VARCHAR(255) NOT NULL,
    sharding_item INT,
    status VARCHAR(50) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    execution_time BIGINT,
    result TEXT,
    error_message TEXT,
    executor_node VARCHAR(255),
    INDEX idx_job_id (job_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE distributed_lock (
    lock_key VARCHAR(255) PRIMARY KEY,
    lock_value VARCHAR(255) NOT NULL,
    owner VARCHAR(255) NOT NULL,
    acquire_time DATETIME NOT NULL,
    expire_time DATETIME NOT NULL,
    version BIGINT DEFAULT 0,
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE job_shard (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    sharding_item INT NOT NULL,
    sharding_parameter VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    map_result TEXT,
    executor_node VARCHAR(255),
    start_time DATETIME,
    end_time DATETIME,
    INDEX idx_job_id (job_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
*/

// ==================== 15. application.yml 配置示例 ====================

/*
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/job_scheduler?useUnicode=true&characterEncoding=utf8
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        
scheduler:
  enabled: true
  scan-interval: 1000
  executor-pool-size: 10
  max-retry-count: 3
  lock-timeout: 300
*/

// ==================== 16. Maven依赖 pom.xml ====================

/*
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- MySQL Driver -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- Quartz for Cron Expression -->
    <dependency>
        <groupId>org.quartz-scheduler</groupId>
        <artifactId>quartz</artifactId>
        <version>2.3.2</version>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- SLF4J Logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
    </dependency>
</dependencies>
*/

// ==================== 17. 使用示例 ====================

/*
// 创建简单任务
ScheduledJob simpleJob = new ScheduledJob();
simpleJob.setJobName("DailyReportJob");
simpleJob.setJobGroup("Reports");
simpleJob.setCronExpression("0 0 9 * * ?"); // 每天9点
simpleJob.setJobType(JobType.SIMPLE);
simpleJob.setBeanName("simpleJobExample");
simpleJob.setDescription("生成每日报表");
simpleJob.setMaxRetryCount(3);

jobManagementService.createJob(simpleJob);

// 创建MapReduce任务
ScheduledJob mapReduceJob = new ScheduledJob();
mapReduceJob.setJobName("UserDataAnalysis");
mapReduceJob.setJobGroup("Analytics");
mapReduceJob.setCronExpression("0 0 2 * * ?"); // 每天凌晨2点
mapReduceJob.setJobType(JobType.MAP_REDUCE);
mapReduceJob.setBeanName("userDataMapReduceJob");
mapReduceJob.setShardingCount(10); // 10个分片
mapReduceJob.setDescription("分析用户数据");

jobManagementService.createJob(mapReduceJob);

// 创建分片任务
ScheduledJob shardingJob = new ScheduledJob();
shardingJob.setJobName("EmailNotification");
shardingJob.setJobGroup("Notification");
shardingJob.setCronExpression("0 */30 * * * ?"); // 每30分钟
shardingJob.setJobType(JobType.SHARDING);
shardingJob.setBeanName("emailShardingJob");
shardingJob.setShardingCount(5); // 5个分片
shardingJob.setShardingParameter("region1,region2,region3,region4,region5");
shardingJob.setDescription("批量发送邮件通知");

jobManagementService.createJob(shardingJob);
*/