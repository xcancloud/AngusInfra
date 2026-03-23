package cloud.xcan.angus.core.spring.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * SPA fallback: non-API and non-static paths are forwarded to {@code /index.html}.
 */
public final class VueRouterFilter implements Filter {

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;
    String uri = request.getRequestURI();
    if (shouldServeDirectly(uri)) {
      chain.doFilter(request, response);
    } else {
      request.getRequestDispatcher("/index.html").forward(request, response);
    }
  }

  private static boolean shouldServeDirectly(String uri) {
    return uri.startsWith("/statics")
        || uri.startsWith("/ws")
        || uri.startsWith("/api/")
        || uri.startsWith("/innerapi/")
        || uri.startsWith("/pubapi/")
        || uri.startsWith("/openapi2p/")
        || uri.startsWith("/pubview/")
        || uri.startsWith("/actuator")
        || uri.startsWith("/assets")
        || uri.startsWith("/meta")
        || uri.equals("/favicon.ico")
        || uri.startsWith("/swagger")
        || uri.startsWith("/eureka")
        || uri.startsWith("/webjars")
        || uri.startsWith("/v3/api-docs")
        || uri.startsWith("/v2/api-docs")
        || uri.endsWith(".git")
        || uri.contains(".git/");
  }
}
