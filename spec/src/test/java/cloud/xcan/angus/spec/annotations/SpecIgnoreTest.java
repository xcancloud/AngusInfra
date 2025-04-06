package cloud.xcan.angus.spec.annotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import lombok.SneakyThrows;
import org.junit.Assert;

public class SpecIgnoreTest {

  static class Model {

    @SpecIgnore
    String a;
    String b;
    String c;

    public Model(String a, String b, String c) {
      this.a = a;
      this.b = b;
      this.c = c;
    }

    public String getA() {
      return a;
    }

    public void setA(String a) {
      this.a = a;
    }

    @SpecIgnore
    public String getB() {
      return b;
    }

    public void setB(String b) {
      this.b = b;
    }

    public String getC() {
      return c;
    }

    public void setC(String c) {
      this.c = c;
    }
  }

  @SneakyThrows
  public static void main(String[] args) {

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

    objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
      @Override
      public boolean hasIgnoreMarker(final AnnotatedMember m) {
        return _isIgnorable(m) || super.hasIgnoreMarker(m);
      }

      protected boolean _isIgnorable(Annotated a) {
        SpecIgnore ann = _findAnnotation(a, SpecIgnore.class);
        if (ann != null) {
          return ann.value();
        }
        return false;
      }
    });

    Model model = new Model("a", "b", "c");
    System.out.println(objectMapper.writeValueAsString(model));
    Assert.assertFalse(objectMapper.writeValueAsString(model).contains("a"));
    Assert.assertFalse(objectMapper.writeValueAsString(model).contains("b"));
    Assert.assertTrue(objectMapper.writeValueAsString(model).contains("c"));

  }
}
