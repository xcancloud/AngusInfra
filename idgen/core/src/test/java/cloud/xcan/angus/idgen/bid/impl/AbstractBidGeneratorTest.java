package cloud.xcan.angus.idgen.bid.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cloud.xcan.angus.idgen.BidGenerator;
import cloud.xcan.angus.idgen.bid.DateFormat;
import cloud.xcan.angus.idgen.bid.DistributedIncrAssigner;
import cloud.xcan.angus.idgen.bid.Format;
import cloud.xcan.angus.idgen.bid.Mode;
import cloud.xcan.angus.idgen.entity.IdConfig;
import cloud.xcan.angus.idgen.exception.IdGenerateException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AbstractBidGeneratorTest {

  private static final class Stub extends AbstractBidGenerator {

    @Override
    public String getId(String bizKey) {
      return null;
    }

    @Override
    public String getId(String bizKey, Long tenantId) {
      return null;
    }

    @Override
    public List<String> getIds(String bizKey, int batchNum) {
      return Collections.emptyList();
    }

    @Override
    public List<String> getIds(String bizKey, int batchNum, Long tenantId) {
      return Collections.emptyList();
    }
  }

  private final Stub gen = new Stub();

  @Test
  void checkBizKeyRejectsEmpty() {
    assertThatThrownBy(() -> gen.checkBizKeyParam(""))
        .isInstanceOf(IdGenerateException.class)
        .hasMessageContaining("bizKey");
  }

  @Test
  void checkTenantIdRejectsInvalid() {
    assertThatThrownBy(() -> gen.checkTenantIdParam(null))
        .isInstanceOf(IdGenerateException.class);
    assertThatThrownBy(() -> gen.checkTenantIdParam(-2L))
        .isInstanceOf(IdGenerateException.class);
  }

  @Test
  void checkBatchNumRejectsOutOfRange() {
    assertThatThrownBy(() -> gen.checkBatchNumParam(0))
        .isInstanceOf(IdGenerateException.class);
    assertThatThrownBy(() -> gen.checkBatchNumParam(BidGenerator.MAX_BATCH_NUM + 1))
        .isInstanceOf(IdGenerateException.class);
  }

  @Test
  void checkRedisRequiresIncrWhenRedisMode() {
    IdConfig cfg = baseConfig();
    cfg.setMode(Mode.REDIS);
    assertThatThrownBy(() -> gen.checkRedisInstanceParam(cfg, null))
        .isInstanceOf(IdGenerateException.class)
        .hasMessageContaining("RedisTemplate");
    assertThatCode(
        () -> gen.checkRedisInstanceParam(cfg, Mockito.mock(DistributedIncrAssigner.class)))
        .doesNotThrowAnyException();
  }

  @Test
  void checkRedisSkippedForDbMode() {
    IdConfig cfg = baseConfig();
    cfg.setMode(Mode.DB);
    assertThatCode(() -> gen.checkRedisInstanceParam(cfg, null)).doesNotThrowAnyException();
  }

  @Test
  void checkIdConfigRejectsNullFormat() {
    IdConfig cfg = baseConfig();
    cfg.setFormat(null);
    assertThatThrownBy(() -> gen.checkIdConfig(cfg, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("format");
  }

  @Test
  void checkIdConfigRejectsTenantMismatch() {
    IdConfig cfg = baseConfig();
    cfg.setTenantId(2L);
    assertThatThrownBy(() -> gen.checkIdConfig(cfg, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("tenantId");
  }

  @Test
  void checkIdConfigRejectsInvalidSeqLength() {
    IdConfig nullLen = baseConfig();
    nullLen.setSeqLength(null);
    assertThatThrownBy(() -> gen.checkIdConfig(nullLen, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("seqLength");

    IdConfig tooLong = baseConfig();
    tooLong.setSeqLength(BidGenerator.MAX_SQE_LENGTH + 1);
    assertThatThrownBy(() -> gen.checkIdConfig(tooLong, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("seqLength");
  }

  @Test
  void checkIdConfigRejectsInvalidStep() {
    IdConfig nullStep = baseConfig();
    nullStep.setStep(null);
    assertThatThrownBy(() -> gen.checkIdConfig(nullStep, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("step");

    IdConfig tooBig = baseConfig();
    tooBig.setStep(BidGenerator.MAX_STEP + 1);
    assertThatThrownBy(() -> gen.checkIdConfig(tooBig, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("step");
  }

  @Test
  void checkIdConfigPrefixSeqRequiresPrefix() {
    IdConfig cfg = baseConfig();
    cfg.setFormat(Format.PREFIX_SEQ);
    cfg.setPrefix("");
    assertThatThrownBy(() -> gen.checkIdConfig(cfg, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("prefix");
  }

  @Test
  void checkIdConfigDateSeqRequiresDateFormat() {
    IdConfig cfg = baseConfig();
    cfg.setFormat(Format.DATE_SEQ);
    cfg.setDateFormat(null);
    assertThatThrownBy(() -> gen.checkIdConfig(cfg, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("dateFormat");
  }

  @Test
  void checkIdConfigPrefixDateSeqRequiresPrefixAndDateFormat() {
    IdConfig missingDate = baseConfig();
    missingDate.setFormat(Format.PREFIX_DATE_SEQ);
    missingDate.setPrefix("p");
    missingDate.setDateFormat(null);
    assertThatThrownBy(() -> gen.checkIdConfig(missingDate, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("prefix or dateFormat");

    IdConfig missingPrefix = baseConfig();
    missingPrefix.setFormat(Format.PREFIX_DATE_SEQ);
    missingPrefix.setPrefix("");
    missingPrefix.setDateFormat(DateFormat.YYYYMMDD);
    assertThatThrownBy(() -> gen.checkIdConfig(missingPrefix, BidGenerator.GLOBAL_TENANT_ID))
        .hasMessageContaining("prefix or dateFormat");
  }

  private static IdConfig baseConfig() {
    IdConfig cfg = new IdConfig();
    cfg.setTenantId(BidGenerator.GLOBAL_TENANT_ID);
    cfg.setFormat(Format.SEQ);
    cfg.setSeqLength(6);
    cfg.setStep(1000L);
    cfg.setMode(Mode.DB);
    return cfg;
  }
}
