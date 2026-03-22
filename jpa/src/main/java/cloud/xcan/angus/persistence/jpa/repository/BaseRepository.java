package cloud.xcan.angus.persistence.jpa.repository;

import org.springframework.data.repository.NoRepositoryBean;

/**
 * Persistence-layer marker for Spring Data JPA repositories; extends the shared contract in core.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends cloud.xcan.angus.core.jpa.repository.BaseRepository<T, ID> {
}
