package cloud.xcan.sdf.spec.http;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class HeaderSortTest {

  @Test
  public void headersSortTest() {
    List<HttpHeader> headers = new ArrayList<>();
    headers.add(HttpHeader.newBuilder().name("2").value("").build());
    headers.add(HttpHeader.newBuilder().name("1").value("").build());
    headers.add(HttpHeader.newBuilder().name("3").value("").build());
    headers.add(HttpHeader.newBuilder().name("c").value("").build());
    headers.add(HttpHeader.newBuilder().name("B").value("").build());
    headers.add(HttpHeader.newBuilder().name("A").value("").build());
    headers.add(HttpHeader.newBuilder().name("3").value("").build());

    List<HttpHeader> expectedHeaders = new ArrayList<>();
    expectedHeaders.add(HttpHeader.newBuilder().name("1").value("").build());
    expectedHeaders.add(HttpHeader.newBuilder().name("2").value("").build());
    expectedHeaders.add(HttpHeader.newBuilder().name("3").value("").build());
    expectedHeaders.add(HttpHeader.newBuilder().name("3").value("").build());
    expectedHeaders.add(HttpHeader.newBuilder().name("A").value("").build());
    expectedHeaders.add(HttpHeader.newBuilder().name("B").value("").build());
    expectedHeaders.add(HttpHeader.newBuilder().name("c").value("").build());

    Headers sorted = new Headers(headers);
    sorted.getHeaders();
    Assert.assertEquals(sorted.getHeaders(), expectedHeaders);
  }


  static class Headers {

    List<HttpHeader> headers;

    public Headers(List<HttpHeader> headers) {
      this.headers = headers;
    }

    public List<HttpHeader> getHeaders() {
      if (isNotEmpty(headers)) {
        headers.sort(Comparator.comparing(HttpHeader::getName));
      }
      return headers;
    }
  }
}
