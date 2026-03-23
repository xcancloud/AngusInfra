package cloud.xcan.angus.idgen.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IdGenerateExceptionTest {

  @Test
  void constructors() {
    assertThat(new IdGenerateException()).hasMessage(null);
    assertThat(new IdGenerateException("m")).hasMessage("m");
    assertThat(new IdGenerateException("x %s", "y")).hasMessage("x y");
    Throwable cause = new RuntimeException("c");
    assertThat(new IdGenerateException("m", cause).getCause()).isSameAs(cause);
    assertThat(new IdGenerateException(cause).getCause()).isSameAs(cause);
  }
}
