package cloud.xcan.sdf.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter accepts any file ending in extension. The case of the filename is ignored.
 */
public class ExtensionFileFilter implements FileFilter {

  private String extension;

  public ExtensionFileFilter(String extension) {
    this.extension = extension;
  }

  @Override
  public boolean accept(File file) {
    // perform a case insensitive check.
    return file.getName().toUpperCase().endsWith(extension.toUpperCase());
  }

}
