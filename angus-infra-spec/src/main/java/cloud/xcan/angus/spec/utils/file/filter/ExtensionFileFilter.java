package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.Locale;
import java.util.Objects;

/**
 * Accepts files whose name ends with the given extension, case-insensitive (using
 * {@link Locale#ROOT}).
 */
public class ExtensionFileFilter implements FileFilter {

  private final String suffixLower;

  public ExtensionFileFilter(String extension) {
    Objects.requireNonNull(extension, "extension");
    this.suffixLower = extension.toLowerCase(Locale.ROOT);
  }

  @Override
  public boolean accept(File file) {
    return file.getName().toLowerCase(Locale.ROOT).endsWith(suffixLower);
  }
}
