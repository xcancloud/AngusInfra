package cloud.xcan.angus.core.spring.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MutableHttpServletRequest extends HttpServletRequestWrapper {

  private final Map<String, String> customHeaders;

  public MutableHttpServletRequest(HttpServletRequest request) {
    super(request);
    this.customHeaders = new HashMap<>();
  }

  public void putHeader(String name, String value) {
    this.customHeaders.put(name, value);
  }

  @Override
  public String getHeader(String name) {
    String headerValue = customHeaders.get(name);

    if (headerValue != null) {
      return headerValue;
    }
    return ((HttpServletRequest) getRequest()).getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    if (customHeaders.isEmpty()) {
      return super.getHeaderNames();
    }

    Set<String> set = new HashSet<String>(customHeaders.keySet());
    Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
    while (e.hasMoreElements()) {
      String n = e.nextElement();
      set.add(n);
    }

    return Collections.enumeration(set);
  }
}
