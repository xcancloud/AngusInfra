package cloud.xcan.angus.spec.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.Getter;

@Getter
public class FileSearchUtils {

  public static final int SORT_BY_NAME = 1;
  public static final int SORT_BY_SIZE = 2;
  public static final int SORT_BY_TIME = 3;
  private final String directory;
  private final String prefix;
  private final String suffix;
  private final String regex;
  private final boolean includeSubdirs;
  private final FileFilter filter;
  private final int sortBy;

  private FileSearchUtils(Builder builder) {
    directory = builder.directory;
    prefix = builder.prefix;
    suffix = builder.suffix;
    regex = builder.regex;
    includeSubdirs = builder.includeSubdirs;
    filter = builder.filter;
    sortBy = builder.sortBy;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static Builder newBuilder(FileSearchUtils copy) {
    Builder builder = new Builder();
    builder.directory = copy.getDirectory();
    builder.prefix = copy.getPrefix();
    builder.suffix = copy.getSuffix();
    builder.regex = copy.getRegex();
    builder.includeSubdirs = copy.isIncludeSubdirs();
    builder.filter = copy.getFilter();
    builder.sortBy = copy.getSortBy();
    return builder;
  }

  public List<File> search() {
    File dir = new File(directory);
    if (!dir.exists() || !dir.isDirectory()) {
      throw new IllegalArgumentException("Invalid directory: " + directory);
    }
    List<File> result = new ArrayList<>();
    search(dir, result);
    sort(result);
    return result;
  }

  public void search(File dir, List<File> result) {
    File[] files = dir.listFiles(filter);
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (file.isFile()) {
        if (match(file)) {
          result.add(file);
        }
      } else if (includeSubdirs) {
        search(file, result);
      }
    }
  }

  public boolean match(File file) {
    String fileName = file.getName();
    if (prefix != null && !fileName.startsWith(prefix)) {
      return false;
    }
    if (suffix != null && !fileName.endsWith(suffix)) {
      return false;
    }
    if (regex != null && !fileName.matches(regex)) {
      return false;
    }
    return true;
  }

  public void sort(List<File> files) {
    switch (sortBy) {
      //      case SORT_BY_NAME:
      //        files.sort(Comparator.comparing(File::getName));
      //        break;
      case SORT_BY_SIZE:
        files.sort(Comparator.comparingLong(File::length));
        break;
      case SORT_BY_TIME:
        files.sort(Comparator.comparingLong(File::lastModified));
        break;
      default:
        files.sort(Comparator.comparing(File::getName));
    }
  }

  public static final class Builder {

    private String directory;
    private String prefix;
    private String suffix;
    private String regex;
    private boolean includeSubdirs;
    private FileFilter filter;
    private int sortBy;

    private Builder() {
    }

    public Builder directory(String directory) {
      this.directory = directory;
      return this;
    }

    public Builder prefix(String prefix) {
      this.prefix = prefix;
      return this;
    }

    public Builder suffix(String suffix) {
      this.suffix = suffix;
      return this;
    }

    public Builder regex(String regex) {
      this.regex = regex;
      return this;
    }

    public Builder includeSubdirs(boolean includeSubdirs) {
      this.includeSubdirs = includeSubdirs;
      return this;
    }

    public Builder filter(FileFilter filter) {
      this.filter = filter;
      return this;
    }

    public Builder sortBy(int sortBy) {
      this.sortBy = sortBy;
      return this;
    }

    public FileSearchUtils build0() {
      this.directory = ".";
      this.includeSubdirs = true;
      this.sortBy = SORT_BY_NAME;
      return new FileSearchUtils(this);
    }

    public FileSearchUtils build() {
      return new FileSearchUtils(this);
    }
  }
}
