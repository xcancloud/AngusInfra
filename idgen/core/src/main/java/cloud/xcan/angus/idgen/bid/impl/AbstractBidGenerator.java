package cloud.xcan.angus.idgen.bid.impl;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;

import cloud.xcan.angus.idgen.BidGenerator;
import cloud.xcan.angus.idgen.bid.DistributedIncrAssigner;
import cloud.xcan.angus.idgen.bid.Format;
import cloud.xcan.angus.idgen.bid.Mode;
import cloud.xcan.angus.idgen.entity.IdConfig;
import cloud.xcan.angus.idgen.exception.IdGenerateException;

public abstract class AbstractBidGenerator implements BidGenerator {

  protected void checkTenantIdParam(Long tenantId) {
    if (tenantId == null || tenantId < -1) {
      throw new IdGenerateException("The tenantId is invalid, tenantId cannot be less than -1");
    }
  }

  protected void checkBizKeyParam(String bizKey) {
    if (isEmpty(bizKey)) {
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
    if (Mode.REDIS.equals(idConfig.getMode()) && isNull(incrAssigner)) {
      throw new IdGenerateException("RedisTemplate instance bean is not initialized");
    }
  }

  protected void checkIdConfig(IdConfig idConfig, Long tenantId) {
    if (isNull(idConfig.getFormat())) {
      throw new IdGenerateException("The id format is not configured");
    }
    if (!tenantId.equals(idConfig.getTenantId())) {
      throw new IdGenerateException("The tenantId parameter value is wrong");
    }
    if (isNull(idConfig.getSeqLength()) || idConfig.getSeqLength() > MAX_SQE_LENGTH) {
      throw new IdGenerateException(
          "The seqLength is invalid, the valid range of seqLength is 1-" + MAX_SQE_LENGTH);
    }
    if (isNull(idConfig.getStep()) || idConfig.getStep() > MAX_STEP) {
      throw new IdGenerateException(
          "The step is invalid, the valid range of step is 1-" + MAX_STEP);
    }
    if (Format.PREFIX_SEQ.equals(idConfig.getFormat()) && isEmpty(idConfig.getPrefix())) {
      throw new IdGenerateException("The id prefix is not configured");
    }
    if (Format.DATE_SEQ.equals(idConfig.getFormat()) && isNull(idConfig.getDateFormat())) {
      throw new IdGenerateException("The id dateFormat is not configured");
    }
    if (Format.PREFIX_DATE_SEQ.equals(idConfig.getFormat())
        && (isNull(idConfig.getDateFormat()) || isEmpty(idConfig.getPrefix()))) {
      throw new IdGenerateException("The id prefix or dateFormat is not configured");
    }
  }

}
