package cloud.xcan.angus.core.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class ServletContextHolder {

  private ServletContextHolder() {
  }

  private static ServletRequestAttributes currentAttributes() {
    var attrs = RequestContextHolder.getRequestAttributes();
    if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
      throw new IllegalStateException(
          "No ServletRequestAttributes bound to current thread (not in a web request)");
    }
    return servletAttrs;
  }

  public static HttpServletRequest request() {
    return currentAttributes().getRequest();
  }

  public static @Nullable HttpServletResponse response() {
    return currentAttributes().getResponse();
  }

  public static HttpSession session() {
    return request().getSession();
  }

}
