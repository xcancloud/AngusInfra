package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * Accepts files that {@linkplain File#isHidden() are hidden}.
 */
public final class HiddenFilter implements FileFilter {

  public static final HiddenFilter INSTANCE = new HiddenFilter();

  public HiddenFilter() {
  }

  @Override
  public boolean accept(File file) {
    return file.isHidden();
  }
}
