package cloud.xcan.angus.core.jpa.repository;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
public interface ReadOnlyRepository<T, ID> extends Repository<T, ID> {

  Optional<T> findById(ID id);

  Collection<T> findByIdIn(Collection<ID> id);

  Page<T> findAll(Example<T> example, Pageable pageable);

}
