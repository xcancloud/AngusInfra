package cloud.xcan.sdf.idgen;

import java.util.List;

public interface BidGenerator {

  Long GLOBAL_TENANT_ID = -1L;

  Long MAX_STEP = 1000000L;

  Integer MAX_SQE_LENGTH = 40;

  /**
   * In order to obtain the smallest number of segments in batches, the maximum number of batch IDs
   * must be less than the segment step size, that is, one segment is allowed at most once in a
   * batch.
   *
   * @see BidGenerator#MAX_BATCH_NUM < IdConfig#getStep()
   */
  int MAX_BATCH_NUM = 10000;

  String getId(String bizKey);

  String getId(String bizKey, Long tenantId);

  List<String> getIds(String bizKey, int batchNum);

  List<String> getIds(String bizKey, int batchNum, Long tenantId);

}
