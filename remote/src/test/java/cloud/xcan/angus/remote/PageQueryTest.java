package cloud.xcan.angus.remote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

@DisplayName("PageQuery Unit Tests")
class PageQueryTest {

  private TestPageQuery pageQuery;

  @BeforeEach
  void setUp() {
    pageQuery = new TestPageQuery();
  }

  @Test
  @DisplayName("should create PageRequest with default values")
  void testDefaultPageRequest() {
    PageRequest pageRequest = pageQuery.tranPage();

    assertThat(pageRequest).isNotNull();
    assertThat(pageRequest.getPageNumber()).isZero();
    assertThat(pageRequest.getPageSize()).isEqualTo(10);
  }

  @Test
  @DisplayName("should create PageRequest with custom pagination")
  void testCustomPagination() {
    pageQuery.setPageNo(3);
    pageQuery.setPageSize(20);

    PageRequest pageRequest = pageQuery.tranPage();

    assertThat(pageRequest.getPageNumber()).isEqualTo(2); // 0-indexed
    assertThat(pageRequest.getPageSize()).isEqualTo(20);
  }

  @Test
  @DisplayName("should apply sort correctly")
  void testSortApplication() {
    pageQuery.setOrderBy("id");
    pageQuery.setOrderSort(OrderSort.ASC);

    PageRequest pageRequest = pageQuery.tranPage();

    assertThat(pageRequest.getSort()).isNotEmpty();
  }

  @Test
  @DisplayName("should reject SQL injection attempts in orderBy field")
  void testSQLInjectionPrevention() {
    // Attempt SQL injection
    pageQuery.setOrderBy("id; DROP TABLE users; --");

    assertThatThrownBy(pageQuery::tranPage).isInstanceOf(
        IllegalArgumentException.class).hasMessageContaining("Invalid orderBy field");
  }

  @Test
  @DisplayName("should reject orderBy with special characters")
  void testSpecialCharacterRejection() {
    pageQuery.setOrderBy("id'; OR '1'='1");

    assertThatThrownBy(pageQuery::tranPage).isInstanceOf(
        IllegalArgumentException.class);
  }

  @Test
  @DisplayName("should reject orderBy with parentheses")
  void testParenthesesRejection() {
    pageQuery.setOrderBy("id(123)");

    assertThatThrownBy(pageQuery::tranPage).isInstanceOf(
        IllegalArgumentException.class);
  }

  @Test
  @DisplayName("should allow valid orderBy fields with underscores and dots")
  void testValidFieldNames() {
    pageQuery.setOrderBy("created_at");
    assertThat(pageQuery.tranPage()).isNotNull();

    pageQuery.setOrderBy("user.created_at");
    assertThat(pageQuery.tranPage()).isNotNull();
  }

  @Test
  @DisplayName("should validate against whitelist when provided")
  void testWhitelistValidation() {
    TestPageQueryWithWhitelist restrictedQuery = new TestPageQueryWithWhitelist();
    
    // Valid field
    restrictedQuery.setOrderBy("id");
    assertThat(restrictedQuery.tranPage()).isNotNull();

    // Invalid field not in whitelist
    restrictedQuery.setOrderBy("invalid_field");
    assertThatThrownBy(restrictedQuery::tranPage).isInstanceOf(
        IllegalArgumentException.class).hasMessageContaining("Invalid orderBy field");
  }

  @Test
  @DisplayName("should accept numeric page numbers within valid range")
  void testPageNumberValidation() {
    pageQuery.setPageNo(1);
    assertThat(pageQuery.tranPage().getPageNumber()).isZero();

    pageQuery.setPageNo(100);
    assertThat(pageQuery.tranPage().getPageNumber()).isEqualTo(99);
  }

  @Test
  @DisplayName("should handle null orderSort with default value")
  void testDefaultOrderSort() {
    pageQuery.setOrderBy("id");
    pageQuery.setOrderSort(null);

    PageRequest pageRequest = pageQuery.tranPage();
    assertThat(pageRequest.getSort()).isNotEmpty();
  }

  /**
   * Concrete implementation of PageQuery for testing
   */
  private static class TestPageQuery extends PageQuery {

    @Override
    public String getDefaultOrderBy() {
      return "id";
    }
  }

  /**
   * Concrete implementation with whitelist validation
   */
  private static class TestPageQueryWithWhitelist extends PageQuery {

    @Override
    public String getDefaultOrderBy() {
      return "id";
    }

    @Override
    protected Set<String> getAllowedOrderByFields() {
      Set<String> allowed = new HashSet<>();
      allowed.add("id");
      allowed.add("created_at");
      allowed.add("modified_at");
      allowed.add("name");
      return allowed;
    }
  }
}
