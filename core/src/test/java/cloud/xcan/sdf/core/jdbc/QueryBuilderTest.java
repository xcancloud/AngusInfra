package cloud.xcan.angus.core.jdbc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class QueryBuilderTest {

  @Test
  public void test() {
    Query q = new QueryBuilder("`").appendQuery("SELECT * FROM member").build();
    assertThat(q.getSQL(), is("SELECT * FROM member"));
    assertThat(q.getParameters(), is(Collections.emptyList()));
  }

  @Test
  public void testAppendQueryAndParameter() {
    Query q = new QueryBuilder("`")
        .appendQuery("SELECT * FROM member WHERE ")
        .appendQueryAndParameter("id=?", 9)
        .build();
    assertThat(q.getSQL(), is("SELECT * FROM member WHERE id=?"));
    assertThat(q.getParameters(), is(Arrays.asList(9)));
  }

  @Test
  public void testAppendQueryAndParameters() {
    Query q = new QueryBuilder("`")
        .appendQuery("SELECT * FROM member WHERE ")
        .appendQueryAndParameters("id IN (?,?,?)",
            Arrays.asList(1, 2, 3))
        .build();
    assertThat(q.getSQL(), is("SELECT * FROM member WHERE id IN (?,?,?)"));
    assertThat(q.getParameters(), is(Arrays.asList(1, 2, 3)));
  }

  @Test
  public void testAddParameter() {
    Query q = new QueryBuilder("`")
        .appendQuery("SELECT * FROM member WHERE id=?")
        .addParameter(9)
        .build();
    assertThat(q.getSQL(), is("SELECT * FROM member WHERE id=?"));
    assertThat(q.getParameters(), is(Arrays.asList(9)));
  }

  @Test
  public void testAddParameters() {
    Query q = new QueryBuilder("`")
        .appendQuery("SELECT * FROM member WHERE id IN (?,?,?)")
        .addParameters(Arrays.asList(1, 2, 3))
        .build();
    assertThat(q.getSQL(), is("SELECT * FROM member WHERE id IN (?,?,?)"));
    assertThat(q.getParameters(), is(Arrays.asList(1, 2, 3)));
  }

  @Test
  public void testAppend() {
    Query q = new QueryBuilder("`")
        .appendQuery("SELECT * FROM member WHERE ")
        .append(new Query("id=?", Arrays.asList(9)))
        .build();
    assertThat(q.getSQL(), is("SELECT * FROM member WHERE id=?"));
    assertThat(q.getParameters(), is(Arrays.asList(9)));
  }

  @Test
  public void testAppendIdentifier() {
    Query q = new QueryBuilder("`")
        .appendQuery("SELECT * FROM ")
        .appendIdentifier("order")
        .build();
    assertThat(q.getSQL(), is("SELECT * FROM `order`"));
    assertThat(q.getParameters(), is(Arrays.asList()));
  }

  @Test
  public void testIn() {
    Query q = new QueryBuilder("`")
        .appendQuery("SELECT * FROM x WHERE id ")
        .in(Arrays.asList(1, 2, 3))
        .build();
    assertThat(q.getSQL(), is("SELECT * FROM x WHERE id  IN (?,?,?)"));
    assertThat(q.getParameters(), is(Arrays.asList(1, 2, 3)));
  }

  @Test
  public void testNotIn() {
    Query q = new QueryBuilder("`")
        .appendQuery("SELECT * FROM x WHERE id ")
        .notIn(Arrays.asList(1, 2, 3))
        .build();
    assertThat(q.getSQL(), is("SELECT * FROM x WHERE id  NOT IN (?,?,?)"));
    assertThat(q.getParameters(), is(Arrays.asList(1, 2, 3)));
  }

}
