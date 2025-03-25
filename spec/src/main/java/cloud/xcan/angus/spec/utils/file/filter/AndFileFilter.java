package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This filter providing conditional AND logic across a list of file filters. This filter returns
 * {@code true} if all filters in the list return {@code true}. Otherwise, it returns {@code false}.
 * Checking of the file filter list stops when the first filter returns {@code false}.
 */
public class AndFileFilter implements FileFilter {

  /**
   * The list of file filters.
   */
  private List<FileFilter> fileFilters;

  public AndFileFilter() {
    this(new ArrayList<>());
  }

  public AndFileFilter(FileFilter... fileFilters) {
    this(Arrays.asList(fileFilters));
  }

  public AndFileFilter(List<FileFilter> fileFilters) {
    this.fileFilters = new ArrayList<>(fileFilters);
  }

  public AndFileFilter addFileFilter(FileFilter fileFilter) {
    fileFilters.add(fileFilter);
    return this;
  }

  public List<FileFilter> getFileFilters() {
    return Collections.unmodifiableList(fileFilters);
  }

  public boolean removeFileFilter(FileFilter fileFilter) {
    return fileFilters.remove(fileFilter);
  }

  public void setFileFilters(List<FileFilter> fileFilters) {
    this.fileFilters = new ArrayList<>(fileFilters);
  }

  @Override
  public boolean accept(File file) {
    if (this.fileFilters.isEmpty()) {
      return false;
    }
    for (FileFilter fileFilter : this.fileFilters) {
      if (!fileFilter.accept(file)) {
        return false;
      }
    }
    return true;
  }

}
