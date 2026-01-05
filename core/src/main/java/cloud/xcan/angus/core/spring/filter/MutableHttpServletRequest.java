package cloud.xcan.angus.core.spring.filter;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

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
  private String modifiedRequestURI;
  private String modifiedContextPath;

  public MutableHttpServletRequest(HttpServletRequest request) {
    super(request);
    this.customHeaders = new HashMap<>();

    this.modifiedRequestURI = null;
    this.modifiedContextPath = null;

    // 检查并修改路径
    modifyRequestPathIfNeeded();
  }

  private void modifyRequestPathIfNeeded() {
    String originalUri = super.getRequestURI();
    String originalContextPath = super.getContextPath();

    // 检查请求路径是否包含.git
    if (originalUri.contains(".git")) {
      // 从原始URI中移除contextPath部分
      String uriWithoutContext = originalUri;
      if (originalContextPath != null && !originalContextPath.isEmpty()
          && originalUri.startsWith(originalContextPath)) {
        uriWithoutContext = originalUri.substring(originalContextPath.length());
      }

      // 确保uriWithoutContext以/开头
      if (!uriWithoutContext.startsWith("/")) {
        uriWithoutContext = "/" + uriWithoutContext;
      }

      // 检查是否已经包含/repos路由，避免重复添加
      if (!uriWithoutContext.startsWith("/repos/")) {
        // 在路径前添加/repos
        String modifiedUriWithoutContext = "/repos" + uriWithoutContext;

        // 重新组合完整URI
        this.modifiedRequestURI = originalContextPath + modifiedUriWithoutContext;

        // 如果原始contextPath为空，我们可能需要设置一个虚拟的contextPath
        this.modifiedContextPath = isEmpty(originalContextPath) ? "/repos" : originalContextPath;

        log.debug("Modified request URI from '{}' to '{}'", originalUri, this.modifiedRequestURI);
      } else {
        // 已经包含/repos，保持原样
        this.modifiedRequestURI = originalUri;
        this.modifiedContextPath = originalContextPath;
      }
    } else {
      // 不包含.git，保持原样
      this.modifiedRequestURI = originalUri;
      this.modifiedContextPath = originalContextPath;
    }
  }

  @Override
  public String getRequestURI() {
    if (modifiedRequestURI != null) {
      return modifiedRequestURI;
    }
    return super.getRequestURI();
  }

  @Override
  public StringBuffer getRequestURL() {
    StringBuffer originalUrl = super.getRequestURL();
    if (modifiedRequestURI != null && !modifiedRequestURI.equals(super.getRequestURI())) {
      try {
        // 重建URL，替换URI部分
        String originalUrlStr = originalUrl.toString();
        String originalUri = super.getRequestURI();
        int uriIndex = originalUrlStr.indexOf(originalUri);

        if (uriIndex != -1) {
          StringBuffer newUrl = new StringBuffer();
          newUrl.append(originalUrlStr, 0, uriIndex);
          newUrl.append(modifiedRequestURI);

          // 如果有查询参数，保留它们
          String queryString = super.getQueryString();
          if (queryString != null && !queryString.isEmpty()) {
            newUrl.append("?").append(queryString);
          }

          return newUrl;
        }
      } catch (Exception e) {
        log.warn("Failed to modify request URL, returning original", e);
      }
    }
    return originalUrl;
  }

  @Override
  public String getContextPath() {
    if (modifiedContextPath != null) {
      return modifiedContextPath;
    }
    return super.getContextPath();
  }

  @Override
  public String getServletPath() {
    String originalServletPath = super.getServletPath();
    if (modifiedRequestURI != null && !modifiedRequestURI.equals(super.getRequestURI())) {
      // 尝试计算新的servlet路径
      String contextPath = getContextPath();
      String requestURI = getRequestURI();

      if (requestURI.startsWith(contextPath)) {
        String pathAfterContext = requestURI.substring(contextPath.length());
        // 简单实现：返回路径的第一个部分作为servlet路径
        int nextSlash = pathAfterContext.indexOf('/', 1);
        if (nextSlash != -1) {
          return pathAfterContext.substring(0, nextSlash);
        } else {
          return pathAfterContext;
        }
      }
    }
    return originalServletPath;
  }

  @Override
  public String getPathInfo() {
    String originalPathInfo = super.getPathInfo();
    if (modifiedRequestURI != null && !modifiedRequestURI.equals(super.getRequestURI())) {
      // 尝试计算新的pathInfo
      String contextPath = getContextPath();
      String requestURI = getRequestURI();
      String servletPath = getServletPath();

      if (requestURI.startsWith(contextPath + servletPath)) {
        String pathAfterServlet = requestURI.substring(contextPath.length() + servletPath.length());
        if (pathAfterServlet.isEmpty()) {
          return null;
        }
        return pathAfterServlet;
      }
    }
    return originalPathInfo;
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
  public Enumeration<String> getHeaders(String name) {
    String headerValue = customHeaders.get(name);
    if (headerValue != null) {
      return Collections.enumeration(Set.of(headerValue));
    }

    return ((HttpServletRequest) getRequest()).getHeaders(name);
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

  public void putHeader(String name, String value) {
    this.customHeaders.put(name, value);
  }
}
