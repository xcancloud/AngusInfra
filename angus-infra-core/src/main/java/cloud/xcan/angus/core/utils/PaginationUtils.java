package cloud.xcan.angus.core.utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public final class PaginationUtils {

  private PaginationUtils() {
  }

  /**
   * In-memory pagination. Treats {@code null} list as empty.
   */
  public static <T> Page<T> paginate(List<T> list, Pageable pageable) {
    Objects.requireNonNull(pageable, "pageable");
    List<T> data = list == null ? Collections.emptyList() : list;
    int pageSize = pageable.getPageSize();
    int currentPage = pageable.getPageNumber();
    int start = currentPage * pageSize;

    if (start >= data.size()) {
      return new PageImpl<>(Collections.emptyList(), pageable, data.size());
    }

    int end = Math.min(start + pageSize, data.size());
    List<T> pageContent = data.subList(start, end);
    return new PageImpl<>(pageContent, pageable, data.size());
  }

}
