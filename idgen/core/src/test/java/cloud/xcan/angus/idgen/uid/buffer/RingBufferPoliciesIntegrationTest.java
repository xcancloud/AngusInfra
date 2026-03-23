package cloud.xcan.angus.idgen.uid.buffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RingBufferPoliciesIntegrationTest {

  @Test
  void exceptionPutHandlerWhenFull() {
    RingBuffer rb = new RingBuffer(4, 50);
    while (rb.put(1L)) {
      // fill
    }
    rb.setRejectedPutHandler(new RejectedPutBufferPolicies.ExceptionPolicy());
    assertThatThrownBy(() -> rb.put(2L)).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void toStringContainsMetrics() {
    RingBuffer rb = new RingBuffer(16, 50);
    assertThat(rb.toString()).contains("bufferSize=16");
  }

  @Test
  void takeTriggersAsyncPaddingWhenExecutorPresent() {
    RingBuffer rb = new RingBuffer(32, 90);
    BufferPaddingExecutor ex = Mockito.mock(BufferPaddingExecutor.class);
    rb.setBufferPaddingExecutor(ex);
    assertThat(rb.put(100L)).isTrue();
    rb.take();
    Mockito.verify(ex, Mockito.atLeastOnce()).asyncPadding();
  }
}
