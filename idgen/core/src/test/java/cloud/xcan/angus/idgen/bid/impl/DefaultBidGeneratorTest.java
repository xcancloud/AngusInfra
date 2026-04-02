package cloud.xcan.angus.idgen.bid.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.idgen.BidGenerator;
import cloud.xcan.angus.idgen.ConfigIdAssigner;
import cloud.xcan.angus.idgen.DistributedIncrAssigner;
import cloud.xcan.angus.idgen.bid.DateFormat;
import cloud.xcan.angus.idgen.bid.DefaultBidGenerator;
import cloud.xcan.angus.idgen.bid.Format;
import cloud.xcan.angus.idgen.bid.Mode;
import cloud.xcan.angus.idgen.entity.IdConfig;
import cloud.xcan.angus.idgen.exception.IdGenerateException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DefaultBidGenerator Unit Tests")
class DefaultBidGeneratorTest {

  private DefaultBidGenerator bidGenerator;
  private ConfigIdAssigner mockConfigAssigner;
  private DistributedIncrAssigner mockIncrAssigner;
  private final AtomicLong nextAssignMaxId = new AtomicLong(0L);

  @BeforeEach
  void setUp() {
    mockConfigAssigner = mock(ConfigIdAssigner.class);
    mockIncrAssigner = mock(DistributedIncrAssigner.class);
    bidGenerator = new DefaultBidGenerator(mockConfigAssigner, mockIncrAssigner);
    nextAssignMaxId.set(0L);
    when(mockConfigAssigner.assignSegmentByParam(anyLong(), anyString(), anyLong()))
        .thenAnswer(invocation -> nextAssignMaxId.addAndGet(50_000L));
  }

  private IdConfig createTestConfig(String bizKey, Long tenantId, Format format) {
    IdConfig config = new IdConfig();
    config.setBizKey(bizKey);
    config.setTenantId(tenantId);
    config.setFormat(format);
    config.setSeqLength(6);
    config.setStep(1000L);
    config.setMode(Mode.DB);
    return config;
  }

  @Test
  @DisplayName("should generate sequential IDs for business key")
  void testSimpleIdGeneration() {
    IdConfig config = createTestConfig("ORDER", BidGenerator.GLOBAL_TENANT_ID, Format.SEQ);
    when(
        mockConfigAssigner.retrieveFromIdConfig("ORDER", BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    String id1 = bidGenerator.getId("ORDER");
    String id2 = bidGenerator.getId("ORDER");
    String id3 = bidGenerator.getId("ORDER");

    assertThat(id1).isNotNull();
    assertThat(id2).isNotNull();
    assertThat(id3).isNotNull();
    assertThat(id1).isNotEqualTo(id2);
    assertThat(id2).isNotEqualTo(id3);
  }

  @Test
  @DisplayName("should multitenancy multi-tenant ID generation")
  void testMultiTenantGeneration() {
    IdConfig config1 = createTestConfig("ORDER", 1L, Format.SEQ);
    IdConfig config2 = createTestConfig("ORDER", 2L, Format.SEQ);

    when(mockConfigAssigner.retrieveFromIdConfig("ORDER", 1L)).thenReturn(config1);
    when(mockConfigAssigner.retrieveFromIdConfig("ORDER", 2L)).thenReturn(config2);

    String idTenant1 = bidGenerator.getId("ORDER", 1L);
    String idTenant2 = bidGenerator.getId("ORDER", 2L);

    assertThat(idTenant1).isNotEqualTo(idTenant2);
  }

  @Test
  @DisplayName("should batch generate IDs correctly")
  void testBatchGeneration() {
    IdConfig config = createTestConfig("USER", BidGenerator.GLOBAL_TENANT_ID, Format.SEQ);
    when(mockConfigAssigner.retrieveFromIdConfig("USER", BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    List<String> ids = bidGenerator.getIds("USER", 100);

    assertThat(ids).hasSize(100);
    assertThat(new HashSet<>(ids)).hasSize(100); // All unique
  }

  @Test
  @DisplayName("should multitenancy prefix format IDs")
  void testPrefixFormatGeneration() {
    IdConfig config = createTestConfig("PRODUCT", BidGenerator.GLOBAL_TENANT_ID,
        Format.PREFIX_SEQ);
    config.setPrefix("PROD_");
    when(mockConfigAssigner.retrieveFromIdConfig("PRODUCT",
        BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    String id = bidGenerator.getId("PRODUCT");

    assertThat(id).startsWith("PROD_");
  }

  @Test
  @DisplayName("should multitenancy date format IDs")
  void testDateFormatGeneration() {
    IdConfig config = createTestConfig("INVOICE", BidGenerator.GLOBAL_TENANT_ID,
        Format.DATE_SEQ);
    config.setDateFormat(DateFormat.YYYYMMDD);
    when(mockConfigAssigner.retrieveFromIdConfig("INVOICE",
        BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    String id = bidGenerator.getId("INVOICE");

    assertThat(id).isNotNull().hasSize(14); // 8-digit date + 6-digit sequence
  }

  @Test
  @DisplayName("should throw exception for invalid bizKey")
  void testInvalidBizKey() {
    assertThatThrownBy(() -> bidGenerator.getId("")).isInstanceOf(
        IdGenerateException.class).hasMessageContaining("bizKey");
  }

  @Test
  @DisplayName("should throw exception for invalid batchNum")
  void testInvalidBatchNum() {
    assertThatThrownBy(() -> bidGenerator.getIds("ORDER", 0)).isInstanceOf(
        IdGenerateException.class);
    assertThatThrownBy(
        () -> bidGenerator.getIds("ORDER", BidGenerator.MAX_BATCH_NUM + 1)).isInstanceOf(
        IdGenerateException.class);
  }

  @Test
  @DisplayName("should throw exception for invalid tenantId")
  void testInvalidTenantId() {
    IdConfig config = createTestConfig("ORDER", -2L, Format.SEQ);
    when(mockConfigAssigner.retrieveFromIdConfig("ORDER", -2L)).thenReturn(config);

    assertThatThrownBy(() -> bidGenerator.getId("ORDER", -2L)).isInstanceOf(
        IdGenerateException.class).hasMessageContaining("tenantId");
  }

  @Test
  @DisplayName("should handle concurrent generation for same bizKey")
  void testConcurrentSameBizKey() throws Exception {
    IdConfig config = createTestConfig("TRADE", BidGenerator.GLOBAL_TENANT_ID, Format.SEQ);
    when(
        mockConfigAssigner.retrieveFromIdConfig("TRADE", BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    int threadCount = 50;
    int idsPerThread = 100;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);
    Set<String> generatedIds = new HashSet<>();
    Object lock = new Object();
    AtomicInteger duplicates = new AtomicInteger(0);

    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < idsPerThread; j++) {
            String id = bidGenerator.getId("TRADE");
            synchronized (lock) {
              if (!generatedIds.add(id)) {
                duplicates.incrementAndGet();
              }
            }
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    assertThat(duplicates.get()).isZero();
    assertThat(generatedIds).hasSize(threadCount * idsPerThread);
  }

  @Test
  @DisplayName("should multitenancy concurrent generation for different tenants")
  void testConcurrentMultiTenant() throws Exception {
    IdConfig config1 = createTestConfig("ORDER", 1L, Format.SEQ);
    IdConfig config2 = createTestConfig("ORDER", 2L, Format.SEQ);
    IdConfig config3 = createTestConfig("ORDER", 3L, Format.SEQ);

    when(mockConfigAssigner.retrieveFromIdConfig("ORDER", 1L)).thenReturn(config1);
    when(mockConfigAssigner.retrieveFromIdConfig("ORDER", 2L)).thenReturn(config2);
    when(mockConfigAssigner.retrieveFromIdConfig("ORDER", 3L)).thenReturn(config3);

    ExecutorService executor = Executors.newFixedThreadPool(30);
    CountDownLatch latch = new CountDownLatch(30);

    for (int i = 0; i < 10; i++) {
      executor.submit(() -> {
        try {
          for (int j = 0; j < 100; j++) {
            bidGenerator.getId("ORDER", 1L);
          }
        } finally {
          latch.countDown();
        }
      });
      executor.submit(() -> {
        try {
          for (int j = 0; j < 100; j++) {
            bidGenerator.getId("ORDER", 2L);
          }
        } finally {
          latch.countDown();
        }
      });
      executor.submit(() -> {
        try {
          for (int j = 0; j < 100; j++) {
            bidGenerator.getId("ORDER", 3L);
          }
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    // If no exceptions thrown, test passes
  }
}
