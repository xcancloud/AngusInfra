package cloud.xcan.angus.core.biz;

import java.util.Collection;

public interface NameJoinRepository<T, ID> {

  Collection<T> findByIdIn(Collection<ID> id);

}
