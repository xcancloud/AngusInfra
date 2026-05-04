package cloud.xcan.angus.idgen.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.idgen.entity.IdConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpringIdConfigPersistenceAdapterTest {

  @Mock
  private SpringDataIdConfigRepository repository;

  private SpringIdConfigPersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new SpringIdConfigPersistenceAdapter(repository);
  }

  @Test
  void saveDelegatesToSpringDataRepository() {
    IdConfig config = new IdConfig();
    when(repository.save(config)).thenReturn(config);

    assertThat(adapter.save(config)).isSameAs(config);
    verify(repository).save(config);
  }

  @Test
  void retrieveDelegatesToSpringDataRepository() {
    IdConfig config = new IdConfig();
    when(repository.findByBizKeyAndTenantId("biz", 1L)).thenReturn(config);

    assertThat(adapter.findByBizKeyAndTenantId("biz", 1L)).isSameAs(config);
    verify(repository).findByBizKeyAndTenantId("biz", 1L);
  }

  @Test
  void incrementDelegatesToSpringDataRepository() {
    when(repository.incrementByBizKeyAndTenantId(10L, "biz", 1L)).thenReturn(1);

    assertThat(adapter.incrementByBizKeyAndTenantId(10L, "biz", 1L)).isEqualTo(1);
    verify(repository).incrementByBizKeyAndTenantId(10L, "biz", 1L);
  }

  @Test
  void findMaxIdDelegatesToSpringDataRepository() {
    when(repository.findMaxIdByBizKeyAndTenantId("biz", 1L)).thenReturn(123L);

    assertThat(adapter.findMaxIdByBizKeyAndTenantId("biz", 1L)).isEqualTo(123L);
    verify(repository).findMaxIdByBizKeyAndTenantId("biz", 1L);
  }
}
