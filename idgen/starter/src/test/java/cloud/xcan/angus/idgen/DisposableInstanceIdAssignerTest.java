package cloud.xcan.angus.idgen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.api.pojo.instance.InstanceType;
import cloud.xcan.angus.idgen.entity.InstanceRepository;
import cloud.xcan.angus.idgen.entity.Instance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DisposableInstanceIdAssignerTest {

  @Mock
  private InstanceRepository instanceRepo;

  @InjectMocks
  private DisposableInstanceIdAssigner assigner;

  @Test
  void assignByParamCreatesNewInstance() {
    when(instanceRepo.findByHostAndPort("h1", "p1")).thenReturn(null);
    when(instanceRepo.save(any(Instance.class))).thenAnswer(inv -> {
      Instance i = inv.getArgument(0);
      i.setId(42L);
      return i;
    });

    Long id = assigner.assignInstanceIdByParam("h1", "p1", InstanceType.HOST);

    assertThat(id).isEqualTo(42L);
    verify(instanceRepo).save(any(Instance.class));
  }

  @Test
  void assignByParamIncrementsExisting() {
    Instance existing = new Instance().setPk("pk1").setId(7L).setHost("h2").setPort("p2");
    when(instanceRepo.findByHostAndPort("h2", "p2")).thenReturn(existing);
    when(instanceRepo.incrementId("pk1", 7L)).thenReturn(1);

    Long id = assigner.assignInstanceIdByParam("h2", "p2", InstanceType.HOST);

    assertThat(id).isEqualTo(8L);
  }

  @Test
  void assignByParamReturnsNullWhenIncrementFails() {
    Instance existing = new Instance().setPk("pk1").setId(3L);
    when(instanceRepo.findByHostAndPort("h3", "p3")).thenReturn(existing);
    when(instanceRepo.incrementId(eq("pk1"), eq(3L))).thenReturn(0);

    assertThat(assigner.assignInstanceIdByParam("h3", "p3", InstanceType.HOST)).isNull();
  }
}
