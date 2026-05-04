package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class PaginationUtilsTest {

  @Test
  void paginate_firstPage() {
    List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
    Pageable p = PageRequest.of(0, 2);
    Page<Integer> page = PaginationUtils.paginate(data, p);
    assertEquals(5, page.getTotalElements());
    assertEquals(2, page.getContent().size());
    assertEquals(List.of(1, 2), page.getContent());
  }

  @Test
  void paginate_beyondEnd_returnsEmptyContent() {
    List<String> data = List.of("a");
    Pageable p = PageRequest.of(5, 10);
    Page<String> page = PaginationUtils.paginate(data, p);
    assertTrue(page.getContent().isEmpty());
    assertEquals(1, page.getTotalElements());
  }

  @Test
  void paginate_nullList_treatedAsEmpty() {
    Pageable p = PageRequest.of(0, 10);
    Page<Object> page = PaginationUtils.paginate(null, p);
    assertTrue(page.getContent().isEmpty());
    assertEquals(0, page.getTotalElements());
  }

  @Test
  void paginate_nullPageable_throws() {
    assertThrows(NullPointerException.class,
        () -> PaginationUtils.paginate(Collections.singletonList(1), null));
  }
}
