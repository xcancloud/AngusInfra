package cloud.xcan.angus.domain;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends BaseRepository<User, Long> {

  User findByUsername(String username);
}
