package cloud.xcan.angus.core.jpa.repository;

import java.util.Collection;

public interface NameJoinRepository<T, ID> {

  Collection<T> findByIdIn(Collection<ID> id);

}
