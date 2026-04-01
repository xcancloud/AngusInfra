package cloud.xcan.angus.core.biz;

import java.util.List;
import java.util.Optional;

public interface BaseRepository<T, ID> {

  Iterable<T> batchUpdate(Iterable<T> entities);

  <S extends T> S save(S entity);

  Iterable<T> batchInsert(Iterable<T> entities);

  Optional<T> findById(ID identity);

  List<T> findAllById(Iterable<ID> ids);

}
