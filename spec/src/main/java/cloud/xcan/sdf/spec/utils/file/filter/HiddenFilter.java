package cloud.xcan.sdf.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * Filter that only accepts hidden files.
 */
public class HiddenFilter implements FileFilter {

  @Override
  public boolean accept(File file) {
    return file.isHidden();
  }

}
