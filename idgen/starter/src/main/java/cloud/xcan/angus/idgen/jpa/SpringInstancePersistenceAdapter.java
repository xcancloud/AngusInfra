package cloud.xcan.angus.idgen.jpa;

import cloud.xcan.angus.idgen.entity.Instance;
import cloud.xcan.angus.idgen.entity.InstanceRepository;
import org.springframework.transaction.annotation.Transactional;

public class SpringInstancePersistenceAdapter implements InstanceRepository {

  private final SpringDataInstanceRepository repository;

  public SpringInstancePersistenceAdapter(SpringDataInstanceRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public Instance save(Instance instance) {
    return repository.save(instance);
  }

  @Override
  @Transactional(readOnly = true)
  public Instance findByHostAndPort(String host, String port) {
    return repository.findByHostAndPort(host, port);
  }

  @Override
  @Transactional
  public int incrementId(String pk, Long id) {
    return repository.incrementId(pk, id);
  }
}
