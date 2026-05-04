package cloud.xcan.angus.core.spring.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Wraps a request with additional headers. Header names are matched case-insensitively, consistent
 * with {@link HttpServletRequest#getHeader(String)}.
 */
public class MutableHttpServletRequest extends HttpServletRequestWrapper {

  private final Map<String, String> customHeaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  public MutableHttpServletRequest(HttpServletRequest request) {
    super(request);
  }

  public void putHeader(String name, String value) {
    customHeaders.put(name, value);
  }

  @Override
  public String getHeader(String name) {
    String headerValue = customHeaders.get(name);
    if (headerValue != null) {
      return headerValue;
    }
    return super.getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    String headerValue = customHeaders.get(name);
    if (headerValue != null) {
      return Collections.enumeration(Set.of(headerValue));
    }
    return super.getHeaders(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    if (customHeaders.isEmpty()) {
      return super.getHeaderNames();
    }
    Set<String> names = new HashSet<>(customHeaders.keySet());
    Enumeration<String> e = super.getHeaderNames();
    while (e.hasMoreElements()) {
      names.add(e.nextElement());
    }
    return Collections.enumeration(names);
  }
}
