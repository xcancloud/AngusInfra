package cloud.xcan.angus.sharding;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import cloud.xcan.angus.sharding.annotation.ShardedRepository;
import cloud.xcan.angus.sharding.annotation.ShardedTable;
import cloud.xcan.angus.sharding.context.ShardContext;
import cloud.xcan.angus.sharding.context.ShardInfo;
import cloud.xcan.angus.sharding.strategy.HashShardingStrategy;
import jakarta.persistence.Entity;
import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

class ShardedRepositoryAspectTest {

  private static final int TABLE_COUNT = 16;
  private final HashShardingStrategy strategy = new HashShardingStrategy();
  private final ShardedRepositoryAspect aspect =
      new ShardedRepositoryAspect(strategy, TABLE_COUNT);

  @AfterEach
  void clearContext() {
    ShardContext.clear();
  }

  // ── meta resolution ─────────────────────────────────────────────────────────

  @Test
  void findEntityClass_walksGenericInterfaces() {
    Class<?> entity = ShardedRepositoryAspect.findEntityClass(SampleRepo.class);
    assertThat(entity).isEqualTo(SampleEntity.class);
  }

  @Test
  void findShardedRepoInterface_walksImplementedInterfaces() {
    Class<?> iface = ShardedRepositoryAspect.findShardedRepoInterface(SampleRepoMysql.class);
    assertThat(iface).isEqualTo(SampleRepo.class);
  }

  @Test
  void resolveMeta_returnsEmptyWhenAnnotationMissingShardKey() {
    assertThat(aspect.resolveMeta(NoShardKeyRepo.class)).isEmpty();
  }

  // ── resolveShardValue ───────────────────────────────────────────────────────

  @Test
  void resolveShardValue_savesReadEntityField() throws Exception {
    ShardedRepositoryAspect.EntityShardMeta meta =
        new ShardedRepositoryAspect.EntityShardMeta(SampleEntity.class, "taskId", 0);
    Method m = SampleRepo.class.getMethod("save", Object.class);
    SampleEntity e = new SampleEntity();
    e.taskId = "task-42";

    Long v = aspect.resolveShardValue(m, new Object[]{e}, meta);

    assertThat(v).isEqualTo((long) "task-42".hashCode());
  }

  @Test
  void resolveShardValue_saveAllReadsFirstEntityAndAssertsUniformity() throws Exception {
    ShardedRepositoryAspect.EntityShardMeta meta =
        new ShardedRepositoryAspect.EntityShardMeta(SampleEntity.class, "taskId", 0);
    Method m = SampleRepo.class.getMethod("saveAll", Iterable.class);
    SampleEntity a = new SampleEntity();
    a.taskId = "t-1";
    SampleEntity b = new SampleEntity();
    b.taskId = "t-1";

    Long v = aspect.resolveShardValue(m, new Object[]{List.of(a, b)}, meta);

    assertThat(v).isEqualTo((long) "t-1".hashCode());
  }

  @Test
  void resolveShardValue_saveAllRejectsMixedShardKeys() throws Exception {
    ShardedRepositoryAspect.EntityShardMeta meta =
        new ShardedRepositoryAspect.EntityShardMeta(SampleEntity.class, "taskId", 0);
    Method m = SampleRepo.class.getMethod("saveAll", Iterable.class);
    SampleEntity a = new SampleEntity();
    a.taskId = "t-1";
    SampleEntity b = new SampleEntity();
    b.taskId = "t-2";

    assertThatThrownBy(() -> aspect.resolveShardValue(m, new Object[]{List.of(a, b)}, meta))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Mixed shard keys");
  }

  @Test
  void resolveShardValue_byParamName() throws Exception {
    ShardedRepositoryAspect.EntityShardMeta meta =
        new ShardedRepositoryAspect.EntityShardMeta(SampleEntity.class, "taskId", 0);
    Method m = SampleRepo.class.getMethod("findByTaskId", String.class);

    Long v = aspect.resolveShardValue(m, new Object[]{"task-X"}, meta);

    assertThat(v).isEqualTo((long) "task-X".hashCode());
  }

  @Test
  void resolveShardValue_byAtParam() throws Exception {
    ShardedRepositoryAspect.EntityShardMeta meta =
        new ShardedRepositoryAspect.EntityShardMeta(SampleEntity.class, "taskId", 0);
    Method m = SampleRepo.class.getMethod("queryWithParam", String.class);

    Long v = aspect.resolveShardValue(m, new Object[]{"task-Y"}, meta);

    assertThat(v).isEqualTo((long) "task-Y".hashCode());
  }

  @Test
  void resolveShardValue_returnsNullWhenNoMatch() throws Exception {
    ShardedRepositoryAspect.EntityShardMeta meta =
        new ShardedRepositoryAspect.EntityShardMeta(SampleEntity.class, "taskId", 0);
    Method m = SampleRepo.class.getMethod("findById", Object.class);

    Long v = aspect.resolveShardValue(m, new Object[]{1L}, meta);

    assertThat(v).isNull();
  }

  @Test
  void resolveShardValue_longShardKeyKeptAsLong() throws Exception {
    ShardedRepositoryAspect.EntityShardMeta meta =
        new ShardedRepositoryAspect.EntityShardMeta(NodeEntity.class, "nodeId", 0);
    Method m = NodeRepo.class.getMethod("findByNodeId", Long.class);

    Long v = aspect.resolveShardValue(m, new Object[]{42L}, meta);

    assertThat(v).isEqualTo(42L);
  }

  // ── full around() flow ──────────────────────────────────────────────────────

  @Test
  void around_setsAndClearsContext() throws Throwable {
    SampleRepoMysql proxy = mock(SampleRepoMysql.class);
    Method m = SampleRepo.class.getMethod("findByTaskId", String.class);
    ProceedingJoinPoint pjp = mockPjp(proxy, m, new Object[]{"task-Z"});
    given(pjp.proceed()).willAnswer(inv -> {
      assertThat(ShardContext.get()).isNotNull();
      assertThat(ShardContext.get().getDataSourceKey()).isEqualTo("meterShard");
      // shardKey slot carries computed tableIndex
      int expected = strategy.computeTableIndex("task-Z".hashCode(), TABLE_COUNT);
      assertThat(ShardContext.get().getShardKey()).isEqualTo((long) expected);
      assertThat(ShardContext.get().hasTableIndex()).isFalse();
      return null;
    });

    aspect.around(pjp);

    assertThat(ShardContext.get()).isNull();
  }

  @Test
  void around_throwsWhenUnresolvedAndFailOnUnresolvedTrue() throws Throwable {
    SampleRepoMysql proxy = mock(SampleRepoMysql.class);
    Method m = SampleRepo.class.getMethod("findById", Object.class);
    ProceedingJoinPoint pjp = mockPjp(proxy, m, new Object[]{1L});

    assertThatThrownBy(() -> aspect.around(pjp))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("could not resolve shard key");
  }

  @Test
  void around_honorsExistingOuterContext() throws Throwable {
    ShardInfo outer = new ShardInfo(7, "preset", -1);
    ShardContext.set(outer);
    SampleRepoMysql proxy = mock(SampleRepoMysql.class);
    Method m = SampleRepo.class.getMethod("findById", Object.class);
    ProceedingJoinPoint pjp = mockPjp(proxy, m, new Object[]{1L});
    given(pjp.proceed()).willAnswer(inv -> {
      assertThat(ShardContext.get()).isSameAs(outer);
      return null;
    });

    aspect.around(pjp);

    // Outer context is preserved (aspect must not clear caller-set context).
    assertThat(ShardContext.get()).isSameAs(outer);
  }

  @Test
  void around_skipsForUnannotatedRepository() throws Throwable {
    PlainRepo proxy = mock(PlainRepo.class);
    Method m = PlainRepo.class.getMethod("findById", Object.class);
    ProceedingJoinPoint pjp = mockPjp(proxy, m, new Object[]{1L});
    given(pjp.proceed()).willReturn("ok");

    Object result = aspect.around(pjp);

    assertThat(result).isEqualTo("ok");
    assertThat(ShardContext.get()).isNull();
  }

  // ── helpers ─────────────────────────────────────────────────────────────────

  private static ProceedingJoinPoint mockPjp(Object target, Method method, Object[] args) {
    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    MethodSignature sig = mock(MethodSignature.class);
    given(pjp.getThis()).willReturn(target);
    given(pjp.getTarget()).willReturn(target);
    given(pjp.getArgs()).willReturn(args);
    given(pjp.getSignature()).willReturn((Signature) sig);
    given(sig.getMethod()).willReturn(method);
    return pjp;
  }

  // ── fixtures ────────────────────────────────────────────────────────────────

  @ShardedTable(shardKey = "taskId")
  @Entity
  static class SampleEntity {
    @jakarta.persistence.Id
    Long id;
    String taskId;
  }

  @ShardedTable(shardKey = "nodeId")
  @Entity
  static class NodeEntity {
    @jakarta.persistence.Id
    Long id;
    Long nodeId;
  }

  @ShardedTable
  @Entity
  static class NoShardKeyEntity {
    @jakarta.persistence.Id
    Long id;
  }

  @ShardedRepository(dataSourceKey = "meterShard")
  interface SampleRepo extends JpaRepository<SampleEntity, Long> {
    List<SampleEntity> findByTaskId(String taskId);

    List<SampleEntity> queryWithParam(@Param("taskId") String anything);
  }

  interface SampleRepoMysql extends SampleRepo {
  }

  @ShardedRepository(dataSourceKey = "meterShard")
  interface NodeRepo extends JpaRepository<NodeEntity, Long> {
    NodeEntity findByNodeId(Long nodeId);
  }

  @ShardedRepository(dataSourceKey = "meterShard")
  interface NoShardKeyRepo extends JpaRepository<NoShardKeyEntity, Long> {
  }

  interface PlainRepo extends JpaRepository<SampleEntity, Long> {
  }
}
