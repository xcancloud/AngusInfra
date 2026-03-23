package cloud.xcan.angus.persistence.jpa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.remote.search.SearchOperation;
import cloud.xcan.angus.spec.experimental.Value;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.Query;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Metamodel;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class AbstractSearchRepositoryTest {

  @Mock
  EntityManager em;
  @Mock
  Query query;
  @Mock
  Query queryCount;

  TestSearchRepo repo;

  @BeforeEach
  void setUp() {
    repo = new TestSearchRepo();
    repo.entityManager = em;
  }

  static final class TestSearchRepo extends AbstractSearchRepository<SrEntity> {
    @Override
    public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<SrEntity> mainClz,
        Object[] params, String... match) {
      return new StringBuilder("SELECT %s FROM sr_entity t WHERE 1=1 ");
    }
  }

  static final class TestSearchRepoWithMatch extends AbstractSearchRepository<SrEntity> {
    @Override
    public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<SrEntity> mainClz,
        Object[] params, String... match) {
      return new StringBuilder("SELECT %s FROM sr_entity t WHERE 1=1 ");
    }

    @Override
    public String getMatchFields() {
      return "t.col_name";
    }
  }

  @Entity
  @Table(name = "sr_entity")
  static class SrEntity {
    enum Kind {
      A, B
    }

    enum Label implements Value<String> {
      L_A("alpha"),
      L_B("beta");
      private final String val;

      Label(String val) {
        this.val = val;
      }

      @Override
      public String getValue() {
        return val;
      }
    }

    static final class StrBox implements Value<String> {
      private final String v;

      StrBox(String v) {
        this.v = v;
      }

      @Override
      public String getValue() {
        return v;
      }
    }

    @Id
    Long id;
    @Column(name = "col_name")
    String name;
    @Column(name = "col_age")
    int age;
    @Column(name = "deleted")
    Boolean deleted;
    @Column(name = "kind_col")
    Kind kind;
    @Column(name = "flag")
    Boolean flag;
    @Column(name = "label_col")
    Label label;
  }

  @Entity
  @Table(name = "sr_del_bare")
  static class SrDelBare {
    @Id
    Long id;
    Boolean deleted;
  }

  @Entity
  @Table(name = "sr_plain")
  static class SrPlain {
    @Id
    Long id;
    @Column(name = "x")
    String x;
  }

  @Test
  void getEntityManager_accessor() {
    assertThat(repo.getEntityManager()).isSameAs(em);
  }

  @Test
  void find0_requiresPageableAndMainClass() {
    assertThatThrownBy(
        () -> repo.find0(Set.of(), null, SrEntity.class, null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
        () -> repo.find0(Set.of(), PageRequest.of(0, 1), null, null, null, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void getReturnFieldsCondition_and_count_defaults() {
    assertThat(repo.getReturnFieldsCondition(Set.of(), new Object[] {})).isEqualTo("*");
    assertThat(repo.getReturnCountCondition(Set.of(), new Object[] {})).isEqualTo("count(*)");
  }

  @Test
  void find0_shortPage_entityMapping_skipsCount() {
    when(em.createNativeQuery(anyString(), eq(SrEntity.class))).thenReturn(query);
    SrEntity row = new SrEntity();
    when(query.getResultList()).thenReturn(List.of(row));

    Pageable p = PageRequest.of(0, 10);
    Page<SrEntity> page = repo.find0(Set.of(), p, SrEntity.class, null, null, null);
    assertThat(page.getContent()).containsExactly(row);
    assertThat(page.getTotalElements()).isEqualTo(1);
    verify(query, never()).getSingleResult();
  }

  @Test
  void find0_fullPage_runsCount() {
    when(em.createNativeQuery(anyString(), eq(SrEntity.class))).thenReturn(query);
    when(em.createNativeQuery(anyString())).thenReturn(queryCount);
    List<SrEntity> full = Collections.nCopies(10, new SrEntity());
    when(query.getResultList()).thenReturn(full);
    when(queryCount.getSingleResult()).thenReturn(33L);

    Pageable p = PageRequest.of(0, 10);
    Page<SrEntity> page = repo.find0(Set.of(), p, SrEntity.class, null, null, null);
    assertThat(page.getTotalElements()).isEqualTo(33);
    verify(queryCount).getSingleResult();
  }

  @Test
  void find0_withMapper_usesObjectArrayNativeQuery() {
    when(em.createNativeQuery(anyString())).thenReturn(query);
    when(query.getResultList()).thenReturn(List.<Object[]>of(new Object[] {"nm", 1}));

    Function<Object[], SrEntity> mapper = o -> {
      SrEntity e = new SrEntity();
      e.name = (String) o[0];
      e.age = ((Number) o[1]).intValue();
      return e;
    };
    Pageable p = PageRequest.of(0, 5);
    Page<SrEntity> page = repo.find0(Set.of(), p, SrEntity.class, mapper, null, null);
    assertThat(page.getContent()).hasSize(1);
    assertThat(page.getContent().get(0).name).isEqualTo("nm");
  }

  @Test
  void getList_sorted_appendsOrderBy() {
    when(em.createNativeQuery(anyString(), eq(SrEntity.class))).thenReturn(query);
    when(query.getResultList()).thenReturn(Collections.emptyList());

    Pageable p = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "name"));
    repo.getList(Set.of(), p, SrEntity.class, null, null,
        new StringBuilder("SELECT %s FROM sr_entity t WHERE 1=1 "));
    verify(em).createNativeQuery(org.mockito.ArgumentMatchers.argThat(
        s -> s.contains("ORDER BY") && s.contains("name") && s.contains("DESC")),
        eq(SrEntity.class));
  }

  @Test
  void getCount_supportsBigInteger() {
    when(em.createNativeQuery(anyString())).thenReturn(query);
    when(query.getSingleResult()).thenReturn(BigInteger.valueOf(7));
    long c = repo.getCount(Set.of(), SrEntity.class, null, "SELECT %s FROM x");
    assertThat(c).isEqualTo(7L);
  }

  @Test
  void getCount_supportsLongWrapper() {
    when(em.createNativeQuery(anyString())).thenReturn(query);
    when(query.getSingleResult()).thenReturn(42L);
    long c = repo.getCount(Set.of(), SrEntity.class, null, "SELECT %s FROM x");
    assertThat(c).isEqualTo(42L);
  }

  @Test
  void getCriteriaAliasCondition_comparisonOps() {
    Set<SearchCriteria> criteria = new HashSet<>();
    criteria.add(new SearchCriteria("name", "a", SearchOperation.EQUAL));
    criteria.add(new SearchCriteria("age", "1", SearchOperation.GREATER_THAN));
    criteria.add(new SearchCriteria("age", "9", SearchOperation.LESS_THAN));
    criteria.add(new SearchCriteria("age", "2", SearchOperation.GREATER_THAN_EQUAL));
    criteria.add(new SearchCriteria("age", "8", SearchOperation.LESS_THAN_EQUAL));
    criteria.add(new SearchCriteria("name", "b", SearchOperation.NOT_EQUAL));

    StringBuilder sb =
        repo.getCriteriaAliasCondition(criteria, SrEntity.class, "t", SearchMode.LIKE, false);
    String sql = sb.toString();
    assertThat(sql).contains("t.col_name = :").contains("t.col_age > :")
        .contains("t.col_age < :").contains("t.col_age >= :").contains("t.col_age <= :")
        .contains("<> :");
  }

  @Test
  void getCriteriaAliasCondition_likeMatchOps() {
    Set<SearchCriteria> criteria =
        Set.of(new SearchCriteria("name", "txt", SearchOperation.MATCH));
    StringBuilder sb =
        repo.getCriteriaAliasCondition(criteria, SrEntity.class, "t", SearchMode.LIKE, false, "name");
    assertThat(sb.toString()).contains("like").contains("txt");

    String end = repo
        .getCriteriaAliasCondition(Set.of(new SearchCriteria("name", "e", SearchOperation.MATCH_END)),
            SrEntity.class, "t", SearchMode.LIKE, false, "name")
        .toString();
    assertThat(end).contains("like").contains("e");

    String nm = repo
        .getCriteriaAliasCondition(
            Set.of(new SearchCriteria("name", "n", SearchOperation.NOT_MATCH)), SrEntity.class, "t",
            SearchMode.LIKE, false, "name")
        .toString();
    assertThat(nm).contains("not like");

    String nme = repo
        .getCriteriaAliasCondition(
            Set.of(new SearchCriteria("name", "z", SearchOperation.NOT_MATCH_END)), SrEntity.class,
            "t", SearchMode.LIKE, false, "name")
        .toString();
    assertThat(nme).contains("not like");
  }

  @Test
  void getCriteriaAliasCondition_fulltextMode() {
    Set<SearchCriteria> criteria =
        Set.of(new SearchCriteria("name", "hello", SearchOperation.MATCH));
    StringBuilder sb =
        repo.getCriteriaAliasCondition(criteria, SrEntity.class, "t", SearchMode.MATCH, false,
            "name");
    assertThat(sb.toString()).contains("MATCH(").contains("AGAINST").contains("BOOLEAN MODE");
  }

  @Test
  void getCriteriaAliasCondition_in_notIn_null() {
    Set<SearchCriteria> criteria = new HashSet<>();
    criteria.add(SearchCriteria.in("name", List.of("a", "b")));
    criteria.add(SearchCriteria.notIn("name", List.of("c")));
    criteria.add(SearchCriteria.isNull("name"));
    criteria.add(SearchCriteria.isNotNull("name"));
    String sql =
        repo.getCriteriaAliasCondition(criteria, SrEntity.class, "t", SearchMode.LIKE, false)
            .toString();
    assertThat(sql).contains(" in :").contains(" not in :").contains(" IS NULL ")
        .contains(" IS NOT NULL ");
  }

  @Test
  void getCriteriaAliasCondition_keywordBecomesMatchLike() {
    Set<SearchCriteria> criteria =
        Set.of(new SearchCriteria("keyword", "kw", SearchOperation.EQUAL));
    String sql =
        repo.getCriteriaAliasCondition(criteria, SrEntity.class, "t", SearchMode.LIKE, false,
            "name").toString();
    assertThat(sql).contains("like").contains("kw");
  }

  @Test
  void getCriteriaAliasCondition_notDeleted_whenPresent() {
    Set<SearchCriteria> criteria = Set.of(new SearchCriteria("name", "a", SearchOperation.EQUAL));
    String sql =
        repo.getCriteriaAliasCondition(criteria, SrEntity.class, "t", SearchMode.LIKE, true)
            .toString();
    assertThat(sql).contains("t.deleted = 0");
  }

  @Test
  void getCriteriaAliasCondition_skipsUnknownColumn() {
    Set<SearchCriteria> criteria =
        Set.of(new SearchCriteria("noSuchField", "x", SearchOperation.EQUAL));
    String sql =
        repo.getCriteriaAliasCondition(criteria, SrEntity.class, "t", SearchMode.LIKE, false)
            .toString();
    assertThat(sql).doesNotContain("noSuchField");
  }

  @Test
  void getAliasMatchFields_prefersMatchVarargs() {
    SearchCriteria c = SearchCriteria.match("name", "v");
    String fields =
        repo.getAliasMatchFields(SrEntity.class, "t", c, "name", "name");
    assertThat(fields).isEqualTo("t.col_name,t.col_name");
  }

  @Test
  void getAliasMatchFields_fallsBackToRepoGetMatchFields() {
    TestSearchRepoWithMatch r = new TestSearchRepoWithMatch();
    r.entityManager = em;
    SearchCriteria c = SearchCriteria.match("name", "v");
    assertThat(r.getAliasMatchFields(SrEntity.class, "t", c)).isEqualTo("t.col_name");
  }

  @Test
  void hasDeletedField_true_and_false() {
    assertThat(repo.hasDeletedField(SrEntity.class)).isTrue();
    AbstractSearchRepository<SrPlain> plainRepo = new AbstractSearchRepository<>() {
      @Override
      public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<SrPlain> mainClz,
          Object[] params, String... match) {
        return new StringBuilder();
      }
    };
    plainRepo.entityManager = em;
    assertThat(plainRepo.hasDeletedField(SrPlain.class)).isFalse();
  }

  @Test
  void hasTenantField_trueWhenTenantColumnMapped() {
    SingleTableEntityPersister step = mock(SingleTableEntityPersister.class);
    when(step.getPropertyColumnNames("tenantId")).thenReturn(new String[] {"tenant_id"});
    assertThat(repo.hasTenantField(step)).isTrue();
  }

  @Test
  void hasTenantField_falseWhenPersisterThrows() {
    SingleTableEntityPersister step = mock(SingleTableEntityPersister.class);
    when(step.getPropertyColumnNames("tenantId")).thenThrow(new IllegalArgumentException());
    assertThat(repo.hasTenantField(step)).isFalse();
  }

  @Test
  void hasTenantField_falseWhenColumnNamesEmpty() {
    SingleTableEntityPersister step = mock(SingleTableEntityPersister.class);
    when(step.getPropertyColumnNames("tenantId")).thenReturn(new String[0]);
    assertThat(repo.hasTenantField(step)).isFalse();
  }

  @Test
  void hasDeletedField_falseWhenMetadataThrows() {
    EntityManager emBare = mock(EntityManager.class);
    AbstractSearchRepository<SrDelBare> bareRepo = new AbstractSearchRepository<>() {
      @Override
      public StringBuilder getSqlTemplate(Set<SearchCriteria> criteria, Class<SrDelBare> mainClz,
          Object[] params, String... match) {
        return new StringBuilder();
      }
    };
    bareRepo.entityManager = emBare;
    Metamodel mm = mock(Metamodel.class);
    when(emBare.getMetamodel()).thenReturn(mm);
    when(mm.entity(SrDelBare.class)).thenThrow(new IllegalArgumentException("no entity"));
    assertThat(bareRepo.hasDeletedField(SrDelBare.class)).isFalse();
  }

  @Test
  void getMatchFields_joinsPersisterColumns() {
    SingleTableEntityPersister step = mock(SingleTableEntityPersister.class);
    when(step.getPropertyColumnNames("name")).thenReturn(new String[] {"c1"});
    when(step.getPropertyColumnNames("age")).thenReturn(new String[] {"c2"});
    SearchCriteria c = SearchCriteria.equal("k", "v");
    assertThat(repo.getMatchFields(step, c, "name", "age")).isEqualTo("c1,c2");
  }

  @Test
  void getMatchFields_requiresMatchParams() {
    SingleTableEntityPersister step = mock(SingleTableEntityPersister.class);
    SearchCriteria c = SearchCriteria.equal("k", "v");
    assertThatThrownBy(() -> repo.getMatchFields(step, c))
        .isInstanceOf(ProtocolException.class);
  }

  @Test
  void isBooleanValue() {
    assertThat(repo.isBooleanValue("true")).isTrue();
    assertThat(repo.isBooleanValue("FALSE")).isTrue();
    assertThat(repo.isBooleanValue("no")).isFalse();
  }

  @Test
  void detectFulltextSearchValue_branches() {
    assertThat(AbstractSearchRepository.detectFulltextSearchValue("")).isEmpty();
    assertThat(AbstractSearchRepository.detectFulltextSearchValue("\"x\"")).startsWith("\"");
    assertThat(AbstractSearchRepository.detectFulltextSearchValue("a+b")).contains("\"");
    assertThat(AbstractSearchRepository.detectFulltextSearchValue("/a/b")).contains("+");
    assertThat(AbstractSearchRepository.detectFulltextSearchValue("plain")).contains("\"");
    assertThat(AbstractSearchRepository.detectFulltextSearchValue("it's ok")).contains("\"");
  }

  @Test
  void getAliasMatchFields_fallbackToSingleColumnNameWithoutAlias() {
    SearchCriteria c = SearchCriteria.match("name", "v");
    assertThat(repo.getAliasMatchFields(SrEntity.class, "t", c)).isEqualTo("col_name");
  }

  @Test
  void getList_mapper_emptyObjectArrayList_returnsEmptyList() {
    when(em.createNativeQuery(anyString())).thenReturn(query);
    when(query.getResultList()).thenReturn(Collections.emptyList());
    Function<Object[], SrEntity> mapper = o -> new SrEntity();
    Pageable p = PageRequest.of(0, 5);
    List<SrEntity> list =
        repo.getList(Set.of(), p, SrEntity.class, mapper, null,
            new StringBuilder("SELECT %s FROM sr_entity t WHERE 1=1 "));
    assertThat(list).isEmpty();
  }

  @Test
  void setQueryParameter_equal_string() {
    Set<SearchCriteria> criteria = Set.of(new SearchCriteria("name", "v", SearchOperation.EQUAL));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query).setParameter(org.mockito.ArgumentMatchers.contains("EQUAL"), eq("v"));
  }

  @Test
  void setQueryParameter_inFromCommaString() {
    Set<SearchCriteria> criteria = Set.of(new SearchCriteria("name", "a,b", SearchOperation.IN));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query).setParameter(anyString(), org.mockito.ArgumentMatchers.any(Set.class));
  }

  @Test
  void setQueryParameter_inFromCollection() {
    Set<SearchCriteria> criteria = Set.of(SearchCriteria.in("name", List.of("a", "b")));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query).setParameter(anyString(), eq(List.of("a", "b")));
  }

  @Test
  void setQueryParameter_boolean() {
    Set<SearchCriteria> criteria = Set.of(new SearchCriteria("flag", true, SearchOperation.EQUAL));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query).setParameter(anyString(), eq(true));
  }

  @Test
  void setQueryParameter_enumByName() {
    Set<SearchCriteria> criteria = Set.of(new SearchCriteria("kind", "A", SearchOperation.EQUAL));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query).setParameter(org.mockito.ArgumentMatchers.contains("EQUAL"), eq(SrEntity.Kind.A));
  }

  @Test
  void setQueryParameter_enumCaseInsensitive() {
    Set<SearchCriteria> criteria = Set.of(new SearchCriteria("kind", "b", SearchOperation.EQUAL));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query).setParameter(org.mockito.ArgumentMatchers.contains("EQUAL"), eq(SrEntity.Kind.B));
  }

  @Test
  void setQueryParameter_invalidEnum_throws() {
    Set<SearchCriteria> criteria =
        Set.of(new SearchCriteria("kind", "ZZZ", SearchOperation.EQUAL));
    assertThatThrownBy(() -> repo.setQueryParameter(query, criteria, SrEntity.class))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported enum");
  }

  @Test
  void setQueryParameter_skipsMatchCriteria() {
    Set<SearchCriteria> criteria = Set.of(SearchCriteria.match("name", "x"));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query, never()).setParameter(anyString(), any());
  }

  @Test
  void setQueryParameter_valueInstanceUsesGetValue() {
    Set<SearchCriteria> criteria =
        Set.of(new SearchCriteria("name", new SrEntity.StrBox("boxed"), SearchOperation.EQUAL));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query).setParameter(org.mockito.ArgumentMatchers.contains("EQUAL"), eq("boxed"));
  }

  @Test
  void setQueryParameter_enumImplementingValue_matchedByDisplayValue() {
    Set<SearchCriteria> criteria =
        Set.of(new SearchCriteria("label", "beta", SearchOperation.EQUAL));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query).setParameter(org.mockito.ArgumentMatchers.contains("EQUAL"), eq("beta"));
  }

  @Test
  void setQueryParameter_enumImplementingValue_invalidThrows() {
    Set<SearchCriteria> criteria =
        Set.of(new SearchCriteria("label", "nope", SearchOperation.EQUAL));
    assertThatThrownBy(() -> repo.setQueryParameter(query, criteria, SrEntity.class))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported enum value");
  }

  @Test
  void setQueryParameter_inFromObjectArray_varargsListOfElements() {
    SearchCriteria criteria = SearchCriteria.in("name", new String[] {"p", "q"});
    repo.setQueryParameter(query, Set.of(criteria), SrEntity.class);
    verify(query).setParameter(anyString(), eq(List.of("p", "q")));
  }

  @Test
  void setQueryParameter_stringParsesToBoolean() {
    Set<SearchCriteria> criteria =
        Set.of(new SearchCriteria("flag", "true", SearchOperation.EQUAL));
    repo.setQueryParameter(query, criteria, SrEntity.class);
    verify(query).setParameter(anyString(), eq(Boolean.TRUE));
  }

  @Test
  void setQueryParameter_skipsIgnoredAndInvalidCriteria() {
    SearchCriteria ignored = new SearchCriteria("pageNo", 1, SearchOperation.EQUAL);
    SearchCriteria invalid = new SearchCriteria("name", null, SearchOperation.EQUAL);
    repo.setQueryParameter(query, Set.of(ignored, invalid), SrEntity.class);
    verify(query, never()).setParameter(anyString(), any());
  }
}
