package cloud.xcan.angus.persistence.jpa.criteria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cloud.xcan.angus.remote.InfoScope;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CriteriaUtilsTest {

  private static SearchCriteria c(String key, Object value, SearchOperation op) {
    return new SearchCriteria(key, value, op);
  }

  @Test
  void containsKey_emptyOrBlank() {
    assertThat(CriteriaUtils.containsKey(null, "k")).isFalse();
    assertThat(CriteriaUtils.containsKey(List.of(), "k")).isFalse();
    assertThat(CriteriaUtils.containsKey(List.of(c("a", "v", SearchOperation.EQUAL)), ""))
        .isFalse();
  }

  @Test
  void containsKey_and_containsAndRemove() {
    List<SearchCriteria> list = new ArrayList<>();
    list.add(c("a", "1", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.containsKey(list, "a")).isTrue();
    assertThat(CriteriaUtils.containsKey(list, "b")).isFalse();

    assertThat(CriteriaUtils.containsAndRemove(list, "a")).isTrue();
    assertThat(list).isEmpty();
    assertThat(CriteriaUtils.containsAndRemove(list, "x")).isFalse();
  }

  @Test
  void contains_withOp_and_containsAndRemove() {
    List<SearchCriteria> list = new ArrayList<>();
    list.add(c("x", "v", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.contains(list, "x", SearchOperation.EQUAL)).isTrue();
    assertThat(CriteriaUtils.contains(list, "x", SearchOperation.IN)).isFalse();
    assertThat(CriteriaUtils.contains(null, "x", SearchOperation.EQUAL)).isFalse();
    assertThat(CriteriaUtils.contains(list, "x", null)).isFalse();

    assertThat(CriteriaUtils.containsAndRemove(list, "x", SearchOperation.EQUAL)).isTrue();
    assertThat(list).isEmpty();
  }

  @Test
  void find_findAndRemove_findWithOp() {
    List<SearchCriteria> list = new ArrayList<>();
    list.add(c("k", "1", SearchOperation.EQUAL));
    list.add(c("k", "2", SearchOperation.IN));

    assertThat(CriteriaUtils.find(list, "k")).hasSize(2);
    assertThat(CriteriaUtils.findAndRemove(new ArrayList<>(), "k")).isEmpty();

    List<SearchCriteria> copy = new ArrayList<>(list);
    assertThat(CriteriaUtils.findAndRemove(copy, "k")).hasSize(2);
    assertThat(copy).isEmpty();

    list = new ArrayList<>();
    list.add(c("k", "a", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.find(list, "k", SearchOperation.EQUAL)).hasSize(1);
    assertThat(CriteriaUtils.find(list, "k", SearchOperation.IN)).isEmpty();

    copy = new ArrayList<>(list);
    assertThat(CriteriaUtils.findAndRemove(copy, "k", SearchOperation.EQUAL)).hasSize(1);
    assertThat(copy).isEmpty();
  }

  @Test
  void findFirst_variants() {
    List<SearchCriteria> list = new ArrayList<>();
    assertThat(CriteriaUtils.findFirst(list, "k")).isNull();

    list.add(c("k", "1", SearchOperation.EQUAL));
    list.add(c("k", "2", SearchOperation.IN));
    assertThat(CriteriaUtils.findFirst(list, "k").getValue()).isEqualTo("1");

    List<SearchCriteria> copy = new ArrayList<>(list);
    assertThat(CriteriaUtils.findFirstAndRemove(copy, "k").getValue()).isEqualTo("1");
    assertThat(copy).isEmpty();

    copy = new ArrayList<>(list);
    assertThat(CriteriaUtils.findFirst(copy, "k", SearchOperation.IN).getValue()).isEqualTo("2");
    copy = new ArrayList<>(list);
    assertThat(CriteriaUtils.findFirstAndRemove(copy, "k", SearchOperation.IN).getValue())
        .isEqualTo("2");
    assertThat(copy).hasSize(1);
  }

  @Test
  void findValue_and_removeVariants() {
    List<SearchCriteria> list = new ArrayList<>();
    list.add(c("n", "a", SearchOperation.EQUAL));
    list.add(c("n", null, SearchOperation.EQUAL));
    assertThat(CriteriaUtils.findValue(list, "n")).containsExactly("a");

    List<SearchCriteria> copy = new ArrayList<>(list);
    assertThat(CriteriaUtils.findValueAndRemove(copy, "n")).containsExactly("a");
    assertThat(copy).isEmpty();

    list = new ArrayList<>();
    list.add(c("n", "x", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.findValue(list, "n", SearchOperation.EQUAL)).containsExactly("x");
    copy = new ArrayList<>(list);
    assertThat(CriteriaUtils.findValueAndRemove(copy, "n", SearchOperation.EQUAL))
        .containsExactly("x");
    assertThat(copy).isEmpty();
  }

  @Test
  void findFirstValue_variants() {
    List<SearchCriteria> list = new ArrayList<>();
    list.add(c("n", "v", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.findFirstValue(list, "n")).isEqualTo("v");

    List<SearchCriteria> copy = new ArrayList<>(list);
    assertThat(CriteriaUtils.findFirstValueAndRemove(copy, "n")).isEqualTo("v");
    assertThat(copy).isEmpty();

    assertThat(CriteriaUtils.findFirstValue(list, "n", SearchOperation.EQUAL)).isEqualTo("v");
    copy = new ArrayList<>(list);
    assertThat(CriteriaUtils.findFirstValueAndRemove(copy, "n", SearchOperation.EQUAL))
        .isEqualTo("v");
    assertThat(copy).isEmpty();
  }

  @Test
  void findFirstValueAndRemove_withQuotes() {
    List<SearchCriteria> list = new ArrayList<>();
    list.add(c("n", "\"quoted\"", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.findFirstValueAndRemove(list, "n", SearchOperation.EQUAL))
        .isEqualTo("quoted");
  }

  @Test
  void getFilterInValue_collection_array_string() {
    Set<SearchCriteria> filters = new HashSet<>();
    filters.add(c("id", "a,b", SearchOperation.IN));
    assertThat(CriteriaUtils.getFilterInValue(filters, "id")).containsExactly("a", "b");

    Set<SearchCriteria> copy = new HashSet<>(filters);
    assertThat(CriteriaUtils.getFilterInValueAndRemove(copy, "id")).containsExactly("a", "b");

    filters = new HashSet<>();
    filters.add(c("id", List.of("x", "y"), SearchOperation.IN));
    assertThat(CriteriaUtils.getFilterInValue(filters, "id")).containsExactly("x", "y");

    filters = new HashSet<>();
    filters.add(c("id", new String[]{"p", "q"}, SearchOperation.IN));
    assertThat(CriteriaUtils.getFilterInValue(filters, "id")).containsExactly("p", "q");
  }

  @Test
  void getFilterInFirstValue_variants() {
    Set<SearchCriteria> filters = new HashSet<>();
    assertThat(CriteriaUtils.getFilterInFirstValue(filters, "id")).isNull();

    filters.add(c("id", List.of("a", "b"), SearchOperation.IN));
    assertThat(CriteriaUtils.getFilterInFirstValue(filters, "id")).contains("a");

    filters = new HashSet<>();
    filters.add(c("id", new Long[]{1L, 2L}, SearchOperation.IN));
    assertThat(CriteriaUtils.getFilterInFirstValue(filters, "id")).isNotEmpty();

    filters = new HashSet<>();
    filters.add(c("id", "[\"x\",\"y\"]", SearchOperation.IN));
    assertThat(CriteriaUtils.getFilterInFirstValue(filters, "id")).isNotNull();
  }

  @Test
  void findAllIdInAndEqualValues() {
    Set<SearchCriteria> filters = new LinkedHashSet<>();
    filters.add(c("id", "1", SearchOperation.EQUAL));
    filters.add(c("id", "2,3", SearchOperation.IN));
    assertThat(CriteriaUtils.findAllIdInAndEqualValues(filters, "id", false))
        .contains("1", "2", "3");

    filters = new LinkedHashSet<>();
    filters.add(c("id", "9", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.findAllIdInAndEqualValues(filters, "id", true)).containsExactly("9");
  }

  @Test
  void findAdminInCriteria() {
    Set<SearchCriteria> filters = new HashSet<>();
    assertThat(CriteriaUtils.findAdminInCriteria(filters, false)).isFalse();
    filters.add(c("admin", "true", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.findAdminInCriteria(filters, false)).isTrue();
    assertThat(filters).hasSize(1);
    filters.add(c("admin", "true", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.findAdminInCriteria(filters, true)).isTrue();
  }

  @Test
  void findInfoScope_defaultAndCustom() {
    Set<SearchCriteria> filters = new HashSet<>();
    assertThat(CriteriaUtils.findInfoScope(filters)).isEqualTo(InfoScope.DETAIL);
    assertThat(CriteriaUtils.findInfoScope(filters, InfoScope.BASIC)).isEqualTo(InfoScope.BASIC);

    filters.add(c(SearchCriteria.INFO_SCOPE_KEY, "BASIC", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.findInfoScope(filters)).isEqualTo(InfoScope.BASIC);
  }

  @Test
  void getFilterMatchValue_variants() {
    List<SearchCriteria> list = new ArrayList<>();
    list.add(c("name", "x", SearchOperation.MATCH));
    list.add(c("name", "y", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.getFilterMatchValue(list, "name")).containsExactly("x");
    assertThat(CriteriaUtils.getFilterMatchFirstValue(list, "name")).isEqualTo("x");

    list = new ArrayList<>();
    list.add(c("name", "z", SearchOperation.MATCH));
    assertThat(CriteriaUtils.getFilterMatchFirstValueAndRemove(list, "name")).isEqualTo("z");
    assertThat(list).isEmpty();
  }

  @Test
  void findMatchAndEqualValue_priority() {
    Set<SearchCriteria> filters = new LinkedHashSet<>();
    filters.add(c("f", "m", SearchOperation.MATCH));
    filters.add(c("f", "e", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.findMatchAndEqualValue(filters, "f", false)).isEqualTo("e");

    filters = new LinkedHashSet<>();
    filters.add(c("f", "m", SearchOperation.MATCH_END));
    assertThat(CriteriaUtils.findMatchAndEqualValue(filters, "f", false)).isEqualTo("m");
  }

  @Test
  void assembleGrantPermissionCondition_and_hasPermission() {
    Set<SearchCriteria> criteria = new HashSet<>();
    criteria.add(c("hasPermission", "READ", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.assembleGrantPermissionCondition(criteria, "u", "READ"))
        .contains("u.auths");
    assertThat(CriteriaUtils.assembleGrantPermissionCondition(criteria, "u", "OTHER")).isEmpty();

    assertThat(CriteriaUtils.assembleHasPermissionCondition(criteria, "u")).contains("u.auths");
  }

  @Test
  void assembleSearchNameCondition_escapesQuote() {
    assertThat(CriteriaUtils.assembleSearchNameCondition("", "t")).isEmpty();
    assertThat(CriteriaUtils.assembleSearchNameCondition("kw", "t")).contains("MATCH");
    assertThat(CriteriaUtils.assembleSearchNameCondition("O'Reilly", "t")).contains("''");
  }

  @Test
  void getInConditionValue_helpers() {
    assertThat(CriteriaUtils.getInConditionValue(List.of(1L, 2L))).isEqualTo("(1,2)");
    assertThat(CriteriaUtils.getInConditionValue("1,2")).isEqualTo("(1,2)");
  }

  @Test
  void getNameFilterValue() {
    Set<SearchCriteria> criteria = new HashSet<>();
    assertThat(CriteriaUtils.getNameFilterValue(criteria)).isEmpty();
    criteria.add(c("name", "n1", SearchOperation.EQUAL));
    assertThat(CriteriaUtils.getNameFilterValue(criteria)).isEqualTo("n1");
  }

  @Test
  void timestampStringToLong_digitsAndDatePattern() {
    Set<SearchCriteria> criteria = new LinkedHashSet<>();
    criteria.add(
        new SearchCriteria("timestamp", "1700000000000", SearchOperation.GREATER_THAN_EQUAL));
    criteria.add(new SearchCriteria("timestamp", "1700000001000", SearchOperation.LESS_THAN_EQUAL));
    CriteriaUtils.timestampStringToLong(criteria);
    assertThat(criteria.stream().filter(x -> "timestamp".equals(x.getKey())).count()).isEqualTo(2);

    criteria = new LinkedHashSet<>();
    criteria.add(
        new SearchCriteria("timestamp", "2024-01-15 10:00:00", SearchOperation.GREATER_THAN_EQUAL));
    criteria.add(
        new SearchCriteria("timestamp", "2024-01-16 10:00:00", SearchOperation.LESS_THAN_EQUAL));
    CriteriaUtils.timestampStringToLong(criteria);
    assertThat(criteria).hasSize(2);
    assertThat(criteria.stream().allMatch(x -> x.getValue() instanceof Long)).isTrue();
  }

  @Test
  void findInfoScope_invalidValue_throws() {
    Set<SearchCriteria> filters = new HashSet<>();
    filters.add(c(SearchCriteria.INFO_SCOPE_KEY, "NO_SUCH", SearchOperation.EQUAL));
    assertThatThrownBy(() -> CriteriaUtils.findInfoScope(filters))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
