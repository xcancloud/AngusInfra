package cloud.xcan.angus.idgen.uid.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class BufferPaddingExecutorTest {

  private BufferPaddingExecutor executor;

  @AfterEach
  void tearDown() {
    if (executor != null) {
      executor.shutdown();
    }
  }

  @Test
  void paddingFillsRingBufferWithoutSchedule() {
    RingBuffer ringBuffer = new RingBuffer(8, 50);
    BufferedUidProvider provider = second -> List.of(second * 1000, second * 1000 + 1);
    executor = new BufferPaddingExecutor(ringBuffer, provider, false);
    ringBuffer.setBufferPaddingExecutor(executor);

    executor.paddingBuffer();

    assertThat(ringBuffer.getTail()).isGreaterThan(ringBuffer.getCursor());
    assertThat(executor.isRunning()).isFalse();
  }

  @Test
  void setScheduleIntervalRejectsNonPositive() {
    RingBuffer ringBuffer = new RingBuffer(8, 50);
    executor = new BufferPaddingExecutor(ringBuffer, s -> List.of(1L), false);
    assertThatThrownBy(() -> executor.setScheduleInterval(0))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void startScheduleDoesNotThrow() {
    RingBuffer ringBuffer = new RingBuffer(8, 50);
    executor = new BufferPaddingExecutor(ringBuffer, s -> List.of(1L), true);
    executor.setScheduleInterval(3600L);
    executor.start();
    assertThat(executor).isNotNull();
  }
}
