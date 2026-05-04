package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * OR-combines file filters: {@code true} only if any filter accepts the file. Evaluation stops at
 * the first {@code true}. An empty filter list yields {@code true}.
 * <p>
 * This class is not thread-safe if the backing list is modified while {@link #accept(File)} runs on
 * another thread.
 */
public class OrFileFilter implements FileFilter {

  private List<FileFilter> fileFilters;

  public OrFileFilter() {
    this(Collections.emptyList());
  }

  public OrFileFilter(FileFilter... fileFilters) {
    this(Arrays.asList(fileFilters));
  }

  public OrFileFilter(List<FileFilter> fileFilters) {
    Objects.requireNonNull(fileFilters, "fileFilters");
    this.fileFilters = new ArrayList<>(fileFilters);
  }

  public OrFileFilter addFileFilter(FileFilter fileFilter) {
    fileFilters.add(Objects.requireNonNull(fileFilter, "fileFilter"));
    return this;
  }

  public List<FileFilter> getFileFilters() {
    return Collections.unmodifiableList(fileFilters);
  }

  public boolean removeFileFilter(FileFilter fileFilter) {
    return fileFilters.remove(fileFilter);
  }

  public void setFileFilters(List<FileFilter> filters) {
    Objects.requireNonNull(filters, "fileFilters");
    List<FileFilter> copy = new ArrayList<>();
    for (FileFilter f : filters) {
      copy.add(Objects.requireNonNull(f, "fileFilter"));
    }
    this.fileFilters = copy;
  }

  @Override
  public boolean accept(File file) {
    if (fileFilters.isEmpty()) {
      return true;
    }
    for (FileFilter fileFilter : fileFilters) {
      if (fileFilter.accept(file)) {
        return true;
      }
    }
    return false;
  }
}
