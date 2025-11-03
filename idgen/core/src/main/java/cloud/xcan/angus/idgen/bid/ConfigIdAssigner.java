package cloud.xcan.angus.idgen.bid;

import cloud.xcan.angus.idgen.entity.IdConfig;

public interface ConfigIdAssigner {

  IdConfig save(IdConfig idConfigDB);

  IdConfig retrieveFromIdConfig(String bizKey, Long globalTenantId);

  long assignSegmentByParam(Long step, String bizKey, Long tenantId);

  IdConfig saveAndAssignSegment(IdConfig idConfigDB);
}
