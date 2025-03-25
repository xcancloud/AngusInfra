package cloud.xcan.angus.spec.thread;

import static cloud.xcan.angus.spec.experimental.Assert.assertHasText;

/**
 * {@link InheritableThreadLocal} subclass that exposes a specified name as {@link #toString()}
 * result (allowing for introspection).
 *
 * @see NamedThreadLocal
 */
public class NamedInheritableThreadLocal<T> extends InheritableThreadLocal<T> {

  private final String name;

  public NamedInheritableThreadLocal(String name) {
    assertHasText(name, "Name must not be empty");
    this.name = name;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
