package cloud.xcan.angus.idgen.uid.buffer;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class RejectedPutBufferPoliciesTest {

  @Test
  void discardPolicyDoesNotThrow() {
    RingBuffer rb = mock(RingBuffer.class);
    when(rb.getBufferSize()).thenReturn(8);
    when(rb.getTail()).thenReturn(1L);
    when(rb.getCursor()).thenReturn(0L);
    assertThatCode(() -> new RejectedPutBufferPolicies.DiscardPolicy().rejectPutBuffer(rb, 1L))
        .doesNotThrowAnyException();
  }

  @Test
  void exceptionPolicyThrows() {
    RingBuffer rb = mock(RingBuffer.class);
    when(rb.getBufferSize()).thenReturn(8);
    when(rb.getTail()).thenReturn(2L);
    when(rb.getCursor()).thenReturn(1L);
    assertThatThrownBy(
        () -> new RejectedPutBufferPolicies.ExceptionPolicy().rejectPutBuffer(rb, 9L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("RingBuffer is full");
  }

  @Test
  void blockPolicyReturnsWhenSpaceAvailable() {
    RingBuffer rb = mock(RingBuffer.class);
    when(rb.getBufferSize()).thenReturn(16);
    when(rb.getTail()).thenReturn(5L);
    when(rb.getCursor()).thenReturn(5L);
    assertThatCode(() -> new RejectedPutBufferPolicies.BlockPolicy().rejectPutBuffer(rb, 1L))
        .doesNotThrowAnyException();
  }
}
