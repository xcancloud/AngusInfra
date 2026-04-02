package cloud.xcan.angus.sharding.autoconfigure.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import cloud.xcan.angus.sharding.annotation.Sharding;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultShardKeyResolverTest {

  private DefaultShardKeyResolver resolver;
  private Sharding stubSharding;

  @BeforeEach
  void setUp() throws Exception {
    resolver = new DefaultShardKeyResolver();
    // Use a real @Sharding annotation obtained via reflection
    stubSharding = AnnotationHolder.class
        .getMethod("annotated")
        .getAnnotation(Sharding.class);
  }

  // ── Empty / null args ────────────────────────────────────────────────────

  @Test
  void nullArgs_returnsNull() {
    assertThat(resolver.resolve(null, null, stubSharding, "tenantId")).isNull();
  }

  @Test
  void emptyArgs_returnsNull() {
    assertThat(resolver.resolve(new Object[0], null, stubSharding, "tenantId")).isNull();
  }

  // ── First Long fallback ──────────────────────────────────────────────────

  @Test
  void firstLongArgUsedAsFallback() {
    Object[] args = {42L};
    assertThat(resolver.resolve(args, null, stubSharding, "nonExistentField")).isEqualTo(42L);
  }

  @Test
  void firstLongArgAmongMixed() {
    Object[] args = {"hello", 99L, "world"};
    assertThat(resolver.resolve(args, null, stubSharding, "")).isEqualTo(99L);
  }

  // ── Named field extraction ────────────────────────────────────────────────

  @Test
  void namedFieldOnPlainObject() {
    Object[] args = {new DomainEntity(500L)};
    assertThat(resolver.resolve(args, null, stubSharding, "tenantId")).isEqualTo(500L);
  }

  @Test
  void namedFieldOnFirstIterableElement() {
    Object[] args = {List.of(new DomainEntity(200L), new DomainEntity(300L))};
    assertThat(resolver.resolve(args, null, stubSharding, "tenantId")).isEqualTo(200L);
  }

  @Test
  void namedFieldIntIsCoercedToLong() {
    Object[] args = {new IntEntity(7)};
    assertThat(resolver.resolve(args, null, stubSharding, "count")).isEqualTo(7L);
  }

  @Test
  void namedFieldInheritedFromSuperclass() {
    Object[] args = {new ChildEntity(888L)};
    assertThat(resolver.resolve(args, null, stubSharding, "tenantId")).isEqualTo(888L);
  }

  @Test
  void nonExistentFieldFallsBackToFirstLong() {
    Object[] args = {new DomainEntity(1L), 77L};
    // "badField" doesn't exist; falls back to the domain entity (no Long), then 77L
    assertThat(resolver.resolve(args, null, stubSharding, "badField")).isEqualTo(77L);
  }

  // ── Parameter name matching ───────────────────────────────────────────────

  @Test
  void paramNameMatchingPicksUpLong() {
    Object[] args = {"text", 55L};
    String[] paramNames = {"name", "tenantId"};
    assertThat(resolver.resolve(args, paramNames, stubSharding, "tenantId")).isEqualTo(55L);
  }

  @Test
  void paramNameMatchingIgnoredException_whenNotNumeric() {
    Object[] args = {"hello"};
    String[] paramNames = {"tenantId"};
    // "hello" is not numeric → should fall back to null (no Long found)
    assertThat(resolver.resolve(args, paramNames, stubSharding, "tenantId")).isNull();
  }

  // ── Edge cases ────────────────────────────────────────────────────────────

  @Test
  void nullArgSkipped() {
    Object[] args = {null, 33L};
    assertThat(resolver.resolve(args, null, stubSharding, "tenantId")).isEqualTo(33L);
  }

  @Test
  void emptyIterableDoesNotCrash() {
    Object[] args = {List.of()};
    assertThat(resolver.resolve(args, null, stubSharding, "tenantId")).isNull();
  }

  @Test
  void order_is100() {
    assertThat(resolver.getOrder()).isEqualTo(100);
  }

  // ── Helper types ─────────────────────────────────────────────────────────

  static class DomainEntity {

    private final long tenantId;

    DomainEntity(long tenantId) {
      this.tenantId = tenantId;
    }
  }

  static class SuperEntity {

    protected long tenantId;

    SuperEntity(long tenantId) {
      this.tenantId = tenantId;
    }
  }

  static class ChildEntity extends SuperEntity {

    ChildEntity(long tenantId) {
      super(tenantId);
    }
  }

  static class IntEntity {

    private final int count;

    IntEntity(int count) {
      this.count = count;
    }
  }

  /**
   * Dummy class for obtaining a @Sharding annotation instance via reflection.
   */
  static class AnnotationHolder {

    @Sharding(shardKey = "tenantId")
    public void annotated() {
    }
  }
}
