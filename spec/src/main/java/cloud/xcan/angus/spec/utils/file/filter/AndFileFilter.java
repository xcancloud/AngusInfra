package cloud.xcan.angus.spec.utils.file.filter;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * AND-combines file filters: {@code true} only if every filter accepts the file. Evaluation stops
 * at the first {@code false}. An empty filter list yields {@code false}.
 * <p>
 * This class is not thread-safe if the backing list is modified while {@link #accept(File)} runs on
 * another thread.
 */
public class AndFileFilter implements FileFilter {

  private List<FileFilter> fileFilters;

  public AndFileFilter() {
    this(Collections.emptyList());
  }

  public AndFileFilter(FileFilter... fileFilters) {
    this(Arrays.asList(fileFilters));
  }

  public AndFileFilter(List<FileFilter> fileFilters) {
    Objects.requireNonNull(fileFilters, "fileFilters");
    this.fileFilters = new ArrayList<>(fileFilters);
  }

  public AndFileFilter addFileFilter(FileFilter fileFilter) {
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
      return false;
    }
    for (FileFilter fileFilter : fileFilters) {
      if (!fileFilter.accept(file)) {
        return false;
      }
    }
    return true;
  }
}
