package cloud.xcan.angus.idgen.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class NamingThreadFactoryTest {

  @Test
  void namedPrefixAndDaemon() {
    NamingThreadFactory f = new NamingThreadFactory("worker", true);
    Thread t = f.newThread(() -> {
    });
    assertThat(t.isDaemon()).isTrue();
    assertThat(t.getName()).startsWith("worker-");
  }

  @Test
  void blankPrefixUsesInvokerClass() {
    NamingThreadFactory f = new NamingThreadFactory();
    Thread t = f.newThread(() -> {
    });
    assertThat(t.getName()).contains("-");
  }

  @Test
  void customUncaughtHandler() throws InterruptedException {
    AtomicReference<Throwable> ref = new AtomicReference<>();
    NamingThreadFactory f = new NamingThreadFactory("x", false, (th, e) -> ref.set(e));
    Thread t = f.newThread(() -> {
      throw new IllegalStateException("boom");
    });
    t.start();
    t.join();
    assertThat(ref.get()).isInstanceOf(IllegalStateException.class);
  }
}
