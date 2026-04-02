package cloud.xcan.angus.idgen.jpa;

import cloud.xcan.angus.idgen.entity.IdConfig;
import cloud.xcan.angus.idgen.entity.IdConfigRepository;
import org.springframework.transaction.annotation.Transactional;

public class SpringIdConfigPersistenceAdapter implements IdConfigRepository {

  private final SpringDataIdConfigRepository repository;

  public SpringIdConfigPersistenceAdapter(SpringDataIdConfigRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public IdConfig save(IdConfig idConfig) {
    return repository.save(idConfig);
  }

  @Override
  @Transactional(readOnly = true)
  public IdConfig findByBizKeyAndTenantId(String bizKey, Long tenantId) {
    return repository.findByBizKeyAndTenantId(bizKey, tenantId);
  }

  @Override
  @Transactional
  public int incrementByBizKeyAndTenantId(Long step, String bizKey, Long tenantId) {
    return repository.incrementByBizKeyAndTenantId(step, bizKey, tenantId);
  }

  @Override
  @Transactional(readOnly = true)
  public long findMaxIdByBizKeyAndTenantId(String bizKey, Long tenantId) {
    return repository.findMaxIdByBizKeyAndTenantId(bizKey, tenantId);
  }
}
