package cloud.xcan.angus.idgen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.idgen.autoconfigure.DisposableConfigIdAssigner;
import cloud.xcan.angus.idgen.entity.IdConfig;
import cloud.xcan.angus.idgen.entity.IdConfigRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DisposableConfigIdAssignerTest {

  @Mock
  private IdConfigRepository idConfigRepo;

  @InjectMocks
  private DisposableConfigIdAssigner assigner;

  @Test
  void retrieveDelegates() {
    IdConfig cfg = new IdConfig();
    when(idConfigRepo.findByBizKeyAndTenantId("b", 1L)).thenReturn(cfg);
    assertThat(assigner.retrieveFromIdConfig("b", 1L)).isSameAs(cfg);
  }

  @Test
  void saveDelegates() {
    IdConfig cfg = new IdConfig();
    when(idConfigRepo.save(cfg)).thenReturn(cfg);
    assertThat(assigner.save(cfg)).isSameAs(cfg);
  }

  @Test
  void assignSegmentReturnsMaxWhenIncrementOk() {
    when(idConfigRepo.incrementByBizKeyAndTenantId(10L, "bk", 2L)).thenReturn(1);
    when(idConfigRepo.findMaxIdByBizKeyAndTenantId("bk", 2L)).thenReturn(99L);

    assertThat(assigner.assignSegmentByParam(10L, "bk", 2L)).isEqualTo(99L);
  }

  @Test
  void assignSegmentThrowsWhenIncrementFails() {
    when(idConfigRepo.incrementByBizKeyAndTenantId(anyLong(), any(), anyLong())).thenReturn(0);
    assertThatThrownBy(() -> assigner.assignSegmentByParam(1L, "bk", 2L))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void saveAndAssignSegmentDelegates() {
    IdConfig cfg = new IdConfig();
    when(idConfigRepo.save(cfg)).thenReturn(cfg);
    assertThat(assigner.saveAndAssignSegment(cfg)).isSameAs(cfg);
    verify(idConfigRepo).save(cfg);
  }
}
