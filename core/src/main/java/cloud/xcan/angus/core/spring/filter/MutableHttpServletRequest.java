package cloud.xcan.angus.core.spring.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MutableHttpServletRequest extends HttpServletRequestWrapper {

  private final Map<String, String> customHeaders;

  /**
   * Fix: Failed to convert property value of type 'java.lang.String' to required type
   * 'cloud.xcan.angus.api.enums.Priority' for property 'Priority'.
   * <p>
   * FireFox auto write `Priority: u=0` in header, and conflicts with execution, use cases, and task list queries.
   */
  private final Set<String> headersToRemove;

  public MutableHttpServletRequest(HttpServletRequest request) {
    super(request);
    this.customHeaders = new HashMap<>();
    this.headersToRemove = Set.of("Priority", "priority");
  }

  public void putHeader(String name, String value) {
    this.customHeaders.put(name, value);
  }

  @Override
  public String getHeader(String name) {
    // Return null to indicate that the header field does not exist
    if (headersToRemove.contains(name)) {
      log.info("Removing incompatible headers for the backend '{}'", name);
      return null;
    }

    String headerValue = customHeaders.get(name);
    if (headerValue != null) {
      return headerValue;
    }

    return ((HttpServletRequest) getRequest()).getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    if (headersToRemove.contains(name)) {
      log.info("Removing incompatible headers for the backend '{}'", name);
      return Collections.emptyEnumeration();
    }

    String headerValue = customHeaders.get(name);
    if (headerValue != null) {
      return Collections.enumeration(Set.of(headerValue));
    }

    return super.getHeaders(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    Set<String> filteredNames = new HashSet<>();
    Enumeration<String> originalNames = super.getHeaderNames();
    while (originalNames.hasMoreElements()) {
      String name = originalNames.nextElement();
      if (headersToRemove.contains(name)) {
        log.info("Removing incompatible headers for the backend '{}'", name);
      }else {
        filteredNames.add(name);
      }
    }

    if (customHeaders.isEmpty()) {
      return Collections.enumeration(filteredNames);
    }

    Set<String> custom = new HashSet<String>(customHeaders.keySet());
    Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
    while (e.hasMoreElements()) {
      String n = e.nextElement();
      custom.add(n);
    }
    return Collections.enumeration(custom);
  }
}
