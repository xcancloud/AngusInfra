package cloud.xcan.angus.core.tester.infra.metricsds.domain.sample;


import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.infra.metricsds.Sharding;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ExecSampleErrorRepo extends BaseRepository<ExecSampleError, Long> {

  @Sharding
  @Override
  Page<ExecSampleError> findAll(Specification<ExecSampleError> spec, Pageable pageable);

  @Override
  @Sharding
  List<ExecSampleError> findAllByFilters(Set<SearchCriteria> filters);

}
