package cloud.xcan.angus.core.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ServletContextHolder {

  private ServletContextHolder() { /* no instance */ }

  public static HttpServletRequest request() {
    return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
  }

  public static HttpServletResponse response() {
    return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
  }

  public static HttpSession session() {
    return request().getSession();
  }

}
