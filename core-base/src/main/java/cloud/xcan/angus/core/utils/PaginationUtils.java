package cloud.xcan.angus.core.utils;

import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class PaginationUtils {

  public static <T> Page<T> paginate(List<T> list, Pageable pageable) {
    int pageSize = pageable.getPageSize();
    int currentPage = pageable.getPageNumber();
    int start = currentPage * pageSize;

    if (start >= list.size()) {
      return new PageImpl<>(Collections.emptyList(), pageable, list.size());
    }

    int end = Math.min(start + pageSize, list.size());
    List<T> pageContent = list.subList(start, end);
    return new PageImpl<>(pageContent, pageable, list.size());
  }

}
