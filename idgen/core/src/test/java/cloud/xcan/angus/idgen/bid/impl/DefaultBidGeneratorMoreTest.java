package cloud.xcan.angus.idgen.bid.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.idgen.BidGenerator;
import cloud.xcan.angus.idgen.bid.ConfigIdAssigner;
import cloud.xcan.angus.idgen.bid.DateFormat;
import cloud.xcan.angus.idgen.bid.DistributedIncrAssigner;
import cloud.xcan.angus.idgen.bid.Format;
import cloud.xcan.angus.idgen.bid.Mode;
import cloud.xcan.angus.idgen.entity.IdConfig;
import cloud.xcan.angus.idgen.exception.IdGenerateException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultBidGeneratorMoreTest {

  @Mock
  private ConfigIdAssigner configIdAssigner;
  @Mock
  private DistributedIncrAssigner incrAssigner;

  private final AtomicLong nextAssignMaxId = new AtomicLong(0L);
  private DefaultBidGenerator generator;

  @BeforeEach
  void setUp() {
    generator = new DefaultBidGenerator(configIdAssigner, incrAssigner);
    nextAssignMaxId.set(0L);
    lenient()
        .when(configIdAssigner.assignSegmentByParam(anyLong(), anyString(), anyLong()))
        .thenAnswer(inv -> nextAssignMaxId.addAndGet(50_000L));
  }

  private static IdConfig createTestConfig(String bizKey, Long tenantId, Format format) {
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
  void redisModeUsesIncrForSegment() {
    IdConfig config = createTestConfig("R1", BidGenerator.GLOBAL_TENANT_ID, Format.SEQ);
    config.setMode(Mode.REDIS);
    when(configIdAssigner.retrieveFromIdConfig("R1", BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);
    when(incrAssigner.incr(contains("R1"), eq(1000L))).thenReturn(20_000L);

    String id = generator.getId("R1");

    assertThat(id).isNotBlank();
    verify(incrAssigner).incr(contains("R1"), eq(1000L));
  }

  @Test
  void redisModeWithoutIncrBeanFails() {
    DefaultBidGenerator g2 = new DefaultBidGenerator(configIdAssigner, null);
    IdConfig config = createTestConfig("R2", BidGenerator.GLOBAL_TENANT_ID, Format.SEQ);
    config.setMode(Mode.REDIS);
    when(configIdAssigner.retrieveFromIdConfig("R2", BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    assertThatThrownBy(() -> g2.getId("R2"))
        .isInstanceOf(IdGenerateException.class)
        .hasMessageContaining("Redis");
  }

  @Test
  void tenantMismatchFails() {
    IdConfig config = createTestConfig("TM", 1L, Format.SEQ);
    config.setTenantId(1L);
    when(configIdAssigner.retrieveFromIdConfig("TM", 2L)).thenReturn(config);

    assertThatThrownBy(() -> generator.getId("TM", 2L))
        .isInstanceOf(IdGenerateException.class)
        .hasMessageContaining("tenantId parameter");
  }

  @Test
  void nullFormatFails() {
    IdConfig config = createTestConfig("NF", BidGenerator.GLOBAL_TENANT_ID, Format.SEQ);
    config.setFormat(null);
    when(configIdAssigner.retrieveFromIdConfig("NF", BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    assertThatThrownBy(() -> generator.getId("NF"))
        .isInstanceOf(IdGenerateException.class)
        .hasMessageContaining("format");
  }

  @Test
  void prefixDateSeqBatchPrefixesDateAndSeq() {
    IdConfig config = createTestConfig("PDS", BidGenerator.GLOBAL_TENANT_ID,
        Format.PREFIX_DATE_SEQ);
    config.setPrefix("P-");
    config.setDateFormat(DateFormat.YYYYMMDD);
    when(configIdAssigner.retrieveFromIdConfig("PDS", BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    List<String> ids = generator.getIds("PDS", 5);

    assertThat(ids).hasSize(5);
    assertThat(ids.get(0)).startsWith("P-");
  }

  @Test
  void dateSeqWithYearOnlyFormat() {
    IdConfig config = createTestConfig("Y1", BidGenerator.GLOBAL_TENANT_ID, Format.DATE_SEQ);
    config.setDateFormat(DateFormat.YYYY);
    config.setSeqLength(4);
    // Default setUp assigns maxId 50_000 so first sequence is 5 digits; keep 4-digit seq formatting.
    when(configIdAssigner.assignSegmentByParam(anyLong(), eq("Y1"),
        eq(BidGenerator.GLOBAL_TENANT_ID)))
        .thenReturn(2000L);
    when(configIdAssigner.retrieveFromIdConfig("Y1", BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    String id = generator.getId("Y1");

    assertThat(id.length()).isGreaterThanOrEqualTo(5);
  }

  @Test
  void prefixDateSeqMissingPrefixFails() {
    IdConfig config = createTestConfig("BAD", BidGenerator.GLOBAL_TENANT_ID,
        Format.PREFIX_DATE_SEQ);
    config.setPrefix("");
    config.setDateFormat(DateFormat.YYYYMMDD);
    when(configIdAssigner.retrieveFromIdConfig("BAD", BidGenerator.GLOBAL_TENANT_ID)).thenReturn(
        config);

    assertThatThrownBy(() -> generator.getId("BAD"))
        .isInstanceOf(IdGenerateException.class)
        .hasMessageContaining("prefix");
  }
}
