package cloud.xcan.angus.idgen.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.idgen.entity.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SpringInstancePersistenceAdapterTest {

  @Mock
  private SpringDataInstanceRepository repository;

  private SpringInstancePersistenceAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new SpringInstancePersistenceAdapter(repository);
  }

  @Test
  void saveDelegatesToSpringDataRepository() {
    Instance instance = new Instance();
    when(repository.save(instance)).thenReturn(instance);

    assertThat(adapter.save(instance)).isSameAs(instance);
    verify(repository).save(instance);
  }

  @Test
  void findByHostAndPortDelegatesToSpringDataRepository() {
    Instance instance = new Instance();
    when(repository.findByHostAndPort("h", "p")).thenReturn(instance);

    assertThat(adapter.findByHostAndPort("h", "p")).isSameAs(instance);
    verify(repository).findByHostAndPort("h", "p");
  }

  @Test
  void incrementIdDelegatesToSpringDataRepository() {
    when(repository.incrementId("pk", 5L)).thenReturn(1);

    assertThat(adapter.incrementId("pk", 5L)).isEqualTo(1);
    verify(repository).incrementId("pk", 5L);
  }
}
