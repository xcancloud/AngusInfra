package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter accepts files that are directories.
 */
public class DirectoryFileFilter implements FileFilter {

  @Override
  public boolean accept(File file) {
    return file.isDirectory();
  }

}
