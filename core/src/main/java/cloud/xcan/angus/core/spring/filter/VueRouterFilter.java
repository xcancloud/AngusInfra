package cloud.xcan.angus.core.spring.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class VueRouterFilter implements Filter {

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    // Forward to index.html if the request is not a static resource
    String uri = request.getRequestURI();
    if (!uri.startsWith("/statics") // For default forward
        // For vue forward
        && !uri.startsWith("/assets") && !uri.startsWith("/meta")
        && !uri.startsWith("/iconfont") && !uri.startsWith("/favicon.ico")
        // For api forward, Note: /apis route Used by web front-end
        && !uri.startsWith("/api/") && !uri.startsWith("/innerapi/")
        && !uri.startsWith("/pubapi/") && !uri.startsWith("/openapi2p/")
        && !uri.startsWith("/pubview/") && !uri.startsWith("/actuator")
        // For swagger forward
        && !uri.startsWith("/swagger") && !uri.startsWith("/eureka") && !uri.startsWith("/webjars")
        && !uri.startsWith("/v3/api-docs") && !uri.startsWith("/v2/api-docs")
    ) {
      String indexPage = "/index.html";
      request.getRequestDispatcher(indexPage).forward(request, response);
    } else {
      chain.doFilter(req, res);
    }
  }

}
