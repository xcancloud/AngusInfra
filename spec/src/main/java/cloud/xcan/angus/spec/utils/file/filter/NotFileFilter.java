package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

/**
 * Logical negation of another {@link FileFilter}.
 */
public final class NotFileFilter implements FileFilter {

  private final FileFilter filter;

  public NotFileFilter(FileFilter filter) {
    this.filter = Objects.requireNonNull(filter, "filter");
  }

  @Override
  public boolean accept(File file) {
    return !filter.accept(file);
  }
}
