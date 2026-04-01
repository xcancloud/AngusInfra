package cloud.xcan.angus.sharding.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import cloud.xcan.angus.sharding.annotation.Sharding;
import cloud.xcan.angus.sharding.config.ShardingProperties;
import cloud.xcan.angus.sharding.context.ShardInfo;
import cloud.xcan.angus.sharding.resolver.ShardKeyResolver;
import cloud.xcan.angus.sharding.strategy.ModuloShardingStrategy;
import cloud.xcan.angus.sharding.strategy.ShardingStrategy;
import java.lang.reflect.Method;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ShardingAspectTest {

  private ShardingProperties properties;
  private ShardingStrategy strategy;
  private ShardingAspect aspect;
  private Sharding shardingAnnotation;

  @BeforeEach
  void setUp() throws Exception {
    properties = new ShardingProperties();
    properties.setShardDbCount(4);
    properties.setShardTableCount(8);
    strategy = new ModuloShardingStrategy();

    // DefaultShardKeyResolver as resolver
    cloud.xcan.angus.sharding.autoconfigure.resolver.DefaultShardKeyResolver defaultResolver =
        new cloud.xcan.angus.sharding.autoconfigure.resolver.DefaultShardKeyResolver();
    aspect = new ShardingAspect(properties, strategy, List.of(defaultResolver));

    shardingAnnotation = AnnotationSource.class.getMethod("method").getAnnotation(Sharding.class);
  }

  // ── resolveShardInfo tests ────────────────────────────────────────────────

  @Test
  void resolveShardInfo_routesToCorrectDbShard() throws Exception {
    ProceedingJoinPoint pjp = mockPjpWithArgs(new Object[]{4L}, new String[]{"tenantId"});
    Sharding sharding = AnnotationSource.class.getMethod("withShardKey")
        .getAnnotation(Sharding.class);

    ShardInfo info = aspect.resolveShardInfo(pjp, sharding);

    // 4 % 4 = 0 → shard0DataSource
    assertThat(info.getDataSourceKey()).isEqualTo("shard0DataSource");
    assertThat(info.getShardKey()).isEqualTo(4L);
    assertThat(info.hasTableIndex()).isFalse();
  }

  @Test
  void resolveShardInfo_withSecondaryIndex() throws Exception {
    properties.setEnableTableSecondaryIndex(true);
    ProceedingJoinPoint pjp = mockPjpWithArgs(new Object[]{5L}, new String[]{"tenantId"});
    Sharding sharding = AnnotationSource.class.getMethod("withShardKey")
        .getAnnotation(Sharding.class);

    ShardInfo info = aspect.resolveShardInfo(pjp, sharding);

    // 5 % 4 = 1 → shard1DataSource
    assertThat(info.getDataSourceKey()).isEqualTo("shard1DataSource");
    assertThat(info.hasTableIndex()).isTrue();
    // 5 % 8 = 5
    assertThat(info.getTableIndex()).isEqualTo(5L);
  }

  @Test
  void resolveShardInfo_separateTableKey() throws Exception {
    properties.setEnableTableSecondaryIndex(true);
    // args: shardKey=3L, tableKey=7L
    Entity entity = new Entity(3L, 7L);
    ProceedingJoinPoint pjp = mockPjpWithArgs(new Object[]{entity}, new String[]{"entity"});
    Sharding sharding = AnnotationSource.class.getMethod("withSeparateKeys")
        .getAnnotation(Sharding.class);

    ShardInfo info = aspect.resolveShardInfo(pjp, sharding);

    // dbIndex = 3 % 4 = 3
    assertThat(info.getDataSourceKey()).isEqualTo("shard3DataSource");
    // tableIndex = 7 % 8 = 7
    assertThat(info.getTableIndex()).isEqualTo(7L);
  }

  @Test
  void resolveShardInfo_noArgsDefaultsToZero() throws Exception {
    ProceedingJoinPoint pjp = mockPjpWithArgs(new Object[0], new String[0]);
    Sharding sharding = AnnotationSource.class.getMethod("method").getAnnotation(Sharding.class);
    ShardInfo info = aspect.resolveShardInfo(pjp, sharding);
    assertThat(info.getShardKey()).isEqualTo(0L);
    assertThat(info.getDataSourceKey()).isEqualTo("shard0DataSource");
  }

  // ── custom resolver priority ──────────────────────────────────────────────

  @Test
  void customResolverTakesPriorityByOrder() throws Exception {
    ShardKeyResolver highPriorityResolver = mock(ShardKeyResolver.class);
    given(highPriorityResolver.getOrder()).willReturn(1);
    given(highPriorityResolver.resolve(any(), any(), any(), any())).willReturn(3L);

    ShardingAspect aspectWithCustom = new ShardingAspect(properties, strategy,
        List.of(
            new cloud.xcan.angus.sharding.autoconfigure.resolver.DefaultShardKeyResolver(),
            highPriorityResolver
        ));

    ProceedingJoinPoint pjp = mockPjpWithArgs(new Object[]{99L}, new String[]{"id"});
    Sharding sharding = AnnotationSource.class.getMethod("method").getAnnotation(Sharding.class);

    ShardInfo info = aspectWithCustom.resolveShardInfo(pjp, sharding);
    // High-priority resolver returns 3, so dbIndex = 3 % 4 = 3
    assertThat(info.getDataSourceKey()).isEqualTo("shard3DataSource");
  }

  @Test
  void nullResolverListIsHandledGracefully() {
    ShardingAspect aspectNoResolvers = new ShardingAspect(properties, strategy, null);
    ProceedingJoinPoint pjp = mockPjpWithArgs(new Object[]{}, new String[]{});
    Sharding sharding = mock(Sharding.class);
    given(sharding.shardKey()).willReturn("");
    given(sharding.tableKey()).willReturn("");
    ShardInfo info = aspectNoResolvers.resolveShardInfo(pjp, sharding);
    assertThat(info.getShardKey()).isEqualTo(0L);
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private ProceedingJoinPoint mockPjpWithArgs(Object[] args, String[] paramNames) {
    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    MethodSignature sig = mock(MethodSignature.class);
    given(pjp.getArgs()).willReturn(args);
    given(pjp.getSignature()).willReturn(sig);
    given(sig.getParameterNames()).willReturn(paramNames);
    given(sig.getDeclaringTypeName()).willReturn("TestRepo");
    given(sig.getName()).willReturn("testMethod");
    return pjp;
  }

  static class Entity {

    private final long shardKey;
    private final long tableKey;

    Entity(long shardKey, long tableKey) {
      this.shardKey = shardKey;
      this.tableKey = tableKey;
    }
  }

  static class AnnotationSource {

    @Sharding
    public void method() {
    }

    @Sharding(shardKey = "tenantId")
    public void withShardKey() {
    }

    @Sharding(shardKey = "shardKey", tableKey = "tableKey")
    public void withSeparateKeys() {
    }
  }
}
