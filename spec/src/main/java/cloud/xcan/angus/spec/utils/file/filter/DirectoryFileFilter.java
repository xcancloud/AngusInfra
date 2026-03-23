package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * Accepts directories only.
 */
public final class DirectoryFileFilter implements FileFilter {

  public static final DirectoryFileFilter INSTANCE = new DirectoryFileFilter();

  public DirectoryFileFilter() {
  }

  @Override
  public boolean accept(File file) {
    return file.isDirectory();
  }
}
