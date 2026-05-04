package cloud.xcan.angus.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BeanFieldUtilsTest {

  static class SampleBean {

    public String name;
    public String desc;
  }

  @Test
  void getSearchFields_mapsCamelToUnderScore_andSkipsUnknown() {
    String out = BeanFieldUtils.getSearchFields("name, unknown , desc", SampleBean.class);
    assertNotNull(out);
    assertTrue(out.contains("name"));
    assertTrue(out.contains("desc"));
    assertFalse(out.contains("unknown"));
  }

  @Test
  void getSearchFields_emptySearch_returnsNull() {
    assertEquals(null, BeanFieldUtils.getSearchFields("", SampleBean.class));
    assertEquals(null, BeanFieldUtils.getSearchFields(null, SampleBean.class));
  }

  @Test
  void hasProperty() {
    assertTrue(BeanFieldUtils.hasProperty(new SampleBean(), "name"));
    assertFalse(BeanFieldUtils.hasProperty(new SampleBean(), "missing"));
  }

  @Test
  void search2Entity_valueWithEqualsSign() throws Exception {
    SampleBean bean = BeanFieldUtils.search2Entity("desc=a=b", SampleBean.class);
    assertEquals("a=b", bean.desc);
  }

  @Test
  void getPropertyNames_containsDeclaredFields() {
    assertTrue(BeanFieldUtils.getPropertyNames(SampleBean.class).contains("name"));
  }
}
