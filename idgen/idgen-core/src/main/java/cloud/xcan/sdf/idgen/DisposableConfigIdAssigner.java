package cloud.xcan.sdf.idgen;

import cloud.xcan.sdf.idgen.bid.ConfigIdAssigner;
import cloud.xcan.sdf.idgen.dao.IdConfigRepo;
import cloud.xcan.sdf.idgen.entity.IdConfig;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents an implementation of {@link ConfigIdAssigner}, the worker pk will be discarded after
 * assigned to the BidGenerator
 *
 * @author liuxiaolong
 */
public class DisposableConfigIdAssigner implements ConfigIdAssigner {

  private final IdConfigRepo idConfigRepository;

  public DisposableConfigIdAssigner(IdConfigRepo idConfigRepository) {
    this.idConfigRepository = idConfigRepository;
  }

  @Transactional
  @Override
  public IdConfig save(IdConfig idConfigDB) {
    return idConfigRepository.save(idConfigDB);
  }

  @Transactional
  @Override
  public IdConfig retrieveFromIdConfig(String bizKey, Long globalTenantId) {
    return idConfigRepository.findByBizKeyAndTenantId(bizKey, globalTenantId);
  }

  @Transactional
  @Override
  public long assignSegmentByParam(Long step, String bizKey, Long tenantId) {
    int res = idConfigRepository.incrementByBizKeyAndTenantId(step, bizKey, tenantId);
    if (res == 1) {
      return idConfigRepository.findMaxIdByBizKeyAndTenantId(bizKey, tenantId);
    }
    throw new IllegalStateException("incrementByBizKeyAndTenantId error");
  }

  @Transactional
  @Override
  public IdConfig saveAndAssignSegment(IdConfig idConfigDB) {
    return idConfigRepository.save(idConfigDB);
  }

}
