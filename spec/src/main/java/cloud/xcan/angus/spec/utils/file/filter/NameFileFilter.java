package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter accepts any file with this name. The case of the filename is ignored.
 */
public class NameFileFilter implements FileFilter {

  private String name;

  public NameFileFilter(String name) {
    this.name = name;
  }

  @Override
  public boolean accept(File file) {
    // perform a case insensitive check.
    return file.getName().equalsIgnoreCase(name);
  }

}
