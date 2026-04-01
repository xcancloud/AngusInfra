package cloud.xcan.angus.persistence.jpa.criteria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cloud.xcan.angus.remote.PageQuery;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SearchCriteriaBuilderTest {

  /**
   * Fresh type never passed to SearchCriteriaBuilder — static maps have no entry.
   */
  static class UnconfiguredPageQuery extends PageQuery {

  }

  static class ValidPageQuery extends PageQuery {

    @Id
    Long id;
    public String title;
  }

  static class ValidPageQuery2 extends PageQuery {

    @Id
    Long id;
    public String title;
  }

  static class ValidPageQuery3 extends PageQuery {

    @Id
    Long id;
    public String title;
  }

  static class ValidPageQuery4 extends PageQuery {

    @Id
    Long id;
    public String title;
  }

  static class ValidPageQuery5 extends PageQuery {

    @Id
    Long id;
    public String title;
  }

  @Test
  void getSubTableFields_throwsWhenNeverConfigured() {
    assertThatThrownBy(() -> SearchCriteriaBuilder.getSubTableFields(UnconfiguredPageQuery.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("SubTable");
  }

  @Test
  void subTableFields_null_noop() {
    ValidPageQuery q = new ValidPageQuery();
    SearchCriteriaBuilder<ValidPageQuery> b = new SearchCriteriaBuilder<>(q);
    assertThat(b.subTableFields((String[]) null)).isSameAs(b);
  }

  @Test
  void build_mergesFiltersAndAppliesTimestampConversion() {
    class Qts extends PageQuery {

      @Id
      Long id;
      public String title;
      @SuppressWarnings("unused")
      Long timestamp;
    }
    Qts q = new Qts();
    q.title = "hello";
    List<SearchCriteria> filters = new ArrayList<>();
    filters.add(SearchCriteria.equal("id", 5L));
    filters.add(
        new SearchCriteria("timestamp", "1700000000000", SearchOperation.GREATER_THAN_EQUAL));
    q.setFilters(filters);

    Set<SearchCriteria> built =
        new SearchCriteriaBuilder<>(q).orderByFields("id", "title")
            .rangeSearchFields("id", "timestamp")
            .matchSearchFields("title").inAndNotFields("title").timestampStringToLong(true).build();

    assertThat(built.stream().anyMatch(c -> "title".equals(c.getKey()))).isTrue();
    assertThat(built.stream().anyMatch(c -> "id".equals(c.getKey()))).isTrue();
    assertThat(built.stream().anyMatch(
        c -> "timestamp".equals(c.getKey()) && c.getValue() instanceof Long)).isTrue();
  }

  @Test
  void build_removesSubTableCriteria() {
    ValidPageQuery2 q = new ValidPageQuery2();
    q.setFilters(new ArrayList<>(List.of(SearchCriteria.equal("ghost", "x"))));
    Set<SearchCriteria> built =
        new SearchCriteriaBuilder<>(q).subTableFields("ghost").orderByFields("id", "title")
            .rangeSearchFields("id").matchSearchFields("title").inAndNotFields("title").build();
    assertThat(built.stream().noneMatch(c -> "ghost".equals(c.getKey()))).isTrue();
  }

  @Test
  void build_rejectsUnknownFilterKey() {
    ValidPageQuery3 q = new ValidPageQuery3();
    q.setFilters(new ArrayList<>(List.of(SearchCriteria.equal("notAField", "v"))));
    SearchCriteriaBuilder<ValidPageQuery3> b =
        new SearchCriteriaBuilder<>(q).orderByFields("id", "title").rangeSearchFields("id")
            .matchSearchFields("title").inAndNotFields("title");
    assertThatThrownBy(b::build).isInstanceOf(ProtocolException.class);
  }

  @Test
  void build_rejectsUnsupportedOrderBy() {
    ValidPageQuery4 q = new ValidPageQuery4();
    q.setOrderBy("evilColumn");
    q.setFilters(new ArrayList<>());
    SearchCriteriaBuilder<ValidPageQuery4> b =
        new SearchCriteriaBuilder<>(q).orderByFields("id", "title").rangeSearchFields("id")
            .matchSearchFields("title").inAndNotFields("title");
    assertThatThrownBy(b::build).isInstanceOf(ProtocolException.class);
  }

  @Test
  void build_rejectsRangeOnNonWhitelistedField() {
    ValidPageQuery5 q = new ValidPageQuery5();
    q.setFilters(new ArrayList<>(List.of(
        new SearchCriteria("title", 1, SearchOperation.GREATER_THAN))));
    SearchCriteriaBuilder<ValidPageQuery5> b =
        new SearchCriteriaBuilder<>(q).orderByFields("id", "title").rangeSearchFields("id")
            .matchSearchFields("title").inAndNotFields("title");
    assertThatThrownBy(b::build).isInstanceOf(ProtocolException.class);
  }

  @Test
  void build_rejectsMatchOnNonWhitelistedField() {
    class Qm extends PageQuery {

      @Id
      Long id;
      @SuppressWarnings("unused")
      String title;
    }
    Qm q = new Qm();
    q.setFilters(new ArrayList<>(List.of(SearchCriteria.match("id", "x"))));
    SearchCriteriaBuilder<Qm> b =
        new SearchCriteriaBuilder<>(q).orderByFields("id", "title").rangeSearchFields("id")
            .matchSearchFields("title").inAndNotFields("title");
    assertThatThrownBy(b::build).isInstanceOf(ProtocolException.class);
  }

  @Test
  void staticFieldAccessors_afterConfiguration() {
    class Q extends PageQuery {

      @Id
      Long id;
      @SuppressWarnings("unused")
      String title;
    }
    Q q = new Q();
    new SearchCriteriaBuilder<>(q).subTableFields("x").rangeSearchFields("id")
        .matchSearchFields("title").orderByFields("id").inAndNotFields("title").build();

    assertThat(SearchCriteriaBuilder.getSubTableFields(Q.class)).containsExactly("x");
    assertThat(SearchCriteriaBuilder.getRangeSearchFields(Q.class)).containsExactly("id");
    assertThat(SearchCriteriaBuilder.getMatchSearchFields(Q.class)).containsExactly("title");
    assertThat(SearchCriteriaBuilder.getOrderByFields(Q.class)).contains("id");
  }

  @Test
  void build_rejectsInOnNonIdWithoutWhitelist() {
    class Q6 extends PageQuery {

      @Id
      Long id;
      @SuppressWarnings("unused")
      String title;
    }
    Q6 q = new Q6();
    q.setFilters(new ArrayList<>(List.of(SearchCriteria.in("title", List.of("a")))));
    // IN/NOT_IN 校验仅在配置了 inAndNotFields 白名单后生效；未配置时不拦截
    SearchCriteriaBuilder<Q6> b =
        new SearchCriteriaBuilder<>(q).orderByFields("id", "title").rangeSearchFields("id")
            .matchSearchFields("title").inAndNotFields("id");
    assertThatThrownBy(b::build).isInstanceOf(ProtocolException.class);
  }

  @Test
  void build_allowsInOnWhitelistedNonId() {
    class Q7 extends PageQuery {

      @Id
      Long id;
      @SuppressWarnings("unused")
      String title;
    }
    Q7 q = new Q7();
    q.setFilters(new ArrayList<>(List.of(SearchCriteria.in("title", List.of("a")))));
    Set<SearchCriteria> built =
        new SearchCriteriaBuilder<>(q).orderByFields("id", "title").rangeSearchFields("id")
            .matchSearchFields("title").inAndNotFields("title").build();
    assertThat(built.stream().anyMatch(c -> "title".equals(c.getKey()))).isTrue();
  }

  @Test
  void build_dropsInvalidCriteria() {
    class Q8 extends PageQuery {

      @Id
      Long id;
    }
    Q8 q = new Q8();
    SearchCriteria bad = new SearchCriteria();
    bad.setKey("id");
    bad.setOp(SearchOperation.EQUAL);
    bad.setValue(null);
    q.setFilters(new ArrayList<>(List.of(bad)));
    Set<SearchCriteria> built =
        new SearchCriteriaBuilder<>(q).orderByFields("id").rangeSearchFields("id")
            .matchSearchFields("id").inAndNotFields("id").build();
    assertThat(built.stream().noneMatch(c -> c == bad)).isTrue();
  }

  @Test
  void safeFilterFields_excludesPagingKeys() {
    class Q9 extends PageQuery {

      @Id
      Long id;
      @SuppressWarnings("unused")
      Integer pageNo = 1;
    }
    Q9 q = new Q9();
    SearchCriteriaBuilder<Q9> b = new SearchCriteriaBuilder<>(q);
    assertThat(b.safeFilterFields()).noneMatch(f -> "pageNo".equals(f.getName()));
  }
}
