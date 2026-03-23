package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

/**
 * Accepts files whose {@linkplain File#getName() name} equals the given value (case-insensitive).
 */
public final class NameFileFilter implements FileFilter {

  private final String name;

  public NameFileFilter(String name) {
    this.name = Objects.requireNonNull(name, "name");
  }

  @Override
  public boolean accept(File file) {
    return file.getName().equalsIgnoreCase(name);
  }
}
