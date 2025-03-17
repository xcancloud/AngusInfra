package cloud.xcan.sdf.idgen.bid.impl;

import cloud.xcan.sdf.idgen.BidGenerator;
import cloud.xcan.sdf.idgen.bid.DistributedIncrAssigner;
import cloud.xcan.sdf.idgen.bid.Format;
import cloud.xcan.sdf.idgen.bid.Mode;
import cloud.xcan.sdf.idgen.entity.IdConfig;
import cloud.xcan.sdf.idgen.exception.IdGenerateException;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractBidGenerator implements BidGenerator {

  protected void checkTenantIdParam(Long tenantId) {
    if (tenantId == null || tenantId < -1) {
      throw new IdGenerateException("The tenantId is invalid, tenantId cannot be less than -1");
    }
  }

  protected void checkBizKeyParam(String bizKey) {
    if (StringUtils.isEmpty(bizKey)) {
      throw new IdGenerateException("The bizKey is empty");
    }
  }

  protected void checkBatchNumParam(int batchNum) {
    if (batchNum > MAX_BATCH_NUM || batchNum < 1) {
      throw new IdGenerateException(
          "The batchNum is invalid, the valid range of batchNum is 1-" + MAX_BATCH_NUM);
    }
  }

  protected void checkRedisInstanceParam(IdConfig idConfig, DistributedIncrAssigner incrAssigner) {
    if (Mode.REDIS.equals(idConfig.getMode()) && Objects.isNull(incrAssigner)) {
      throw new IdGenerateException("RedisTemplate instance bean is not initialized");
    }
  }

  protected void checkIdConfig(IdConfig idConfig, Long tenantId) {
    if (Objects.isNull(idConfig.getFormat())) {
      throw new IdGenerateException("The id format is not configured");
    }
    if (!tenantId.equals(idConfig.getTenantId())) {
      throw new IdGenerateException("The tenantId parameter value is wrong");
    }
    if (Objects.isNull(idConfig.getSeqLength()) || idConfig.getSeqLength() > MAX_SQE_LENGTH) {
      throw new IdGenerateException(
          "The seqLength is invalid, the valid range of seqLength is 1-" + MAX_SQE_LENGTH);
    }
    if (Objects.isNull(idConfig.getStep()) || idConfig.getStep() > MAX_STEP) {
      throw new IdGenerateException(
          "The step is invalid, the valid range of step is 1-" + MAX_STEP);
    }
    if (Format.PREFIX_SEQ.equals(idConfig.getFormat()) && StringUtils
        .isEmpty(idConfig.getPrefix())) {
      throw new IdGenerateException("The id prefix is not configured");
    }
    if (Format.DATE_SEQ.equals(idConfig.getFormat()) && Objects.isNull(idConfig.getDateFormat())) {
      throw new IdGenerateException("The id dateFormat is not configured");
    }
    if (Format.PREFIX_DATE_SEQ.equals(idConfig.getFormat()) && (
        Objects.isNull(idConfig.getDateFormat()) || StringUtils.isEmpty(idConfig.getPrefix()))) {
      throw new IdGenerateException("The id prefix or dateFormat is not configured");
    }
  }

}
