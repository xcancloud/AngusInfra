package cloud.xcan.angus.plugin.core;

import cloud.xcan.angus.plugin.api.PluginContext;
import cloud.xcan.angus.plugin.api.PluginController;
import cloud.xcan.angus.plugin.api.RestfulPlugin;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class DynamicRestEndpointManager {

  private static final Logger log = LoggerFactory.getLogger(DynamicRestEndpointManager.class);

  private final RequestMappingHandlerMapping requestMappingHandlerMapping;

  private final ApplicationContext applicationContext;

  public DynamicRestEndpointManager(RequestMappingHandlerMapping requestMappingHandlerMapping,
      ApplicationContext applicationContext) {
    this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    this.applicationContext = applicationContext;
  }

  private final Map<String, List<RequestMappingInfo>> pluginEndpoints = new ConcurrentHashMap<>();
  private final Map<String, List<Object>> pluginControllers = new ConcurrentHashMap<>();

  public boolean registerPlugin(String pluginId, RestfulPlugin plugin, PluginContext context) {
    try {
      log.info("Registering REST endpoints for plugin: {}", pluginId);
      List<Class<?>> controllerClasses = plugin.getControllerClasses();
      if (controllerClasses == null || controllerClasses.isEmpty()) {
        return true;
      }
      List<RequestMappingInfo> mappingInfos = new ArrayList<>();
      List<Object> controllers = new ArrayList<>();
      String apiPrefix = plugin.getApiPrefix();
      if (apiPrefix == null || apiPrefix.isEmpty()) {
        apiPrefix = "/api/plugins/" + pluginId;
      }
      for (Class<?> controllerClass : controllerClasses) {
        try {
          Object controller = createControllerInstance(controllerClass, context);
          controllers.add(controller);
          List<RequestMappingInfo> controllerMappings = registerControllerMethods(controller,
              controllerClass, apiPrefix);
          mappingInfos.addAll(controllerMappings);
        } catch (Exception e) {
          log.error("Failed to register controller: {}", controllerClass.getName(), e);
        }
      }
      pluginEndpoints.put(pluginId, mappingInfos);
      pluginControllers.put(pluginId, controllers);
      log.info("Successfully registered {} REST endpoints for plugin: {}", mappingInfos.size(),
          pluginId);
      return true;
    } catch (Exception e) {
      log.error("Failed to register plugin REST endpoints: " + pluginId, e);
      unregisterPlugin(pluginId);
      return false;
    }
  }

  public boolean unregisterPlugin(String pluginId) {
    try {
      log.info("Unregistering REST endpoints for plugin: {}", pluginId);
      List<RequestMappingInfo> mappingInfos = pluginEndpoints.remove(pluginId);
      if (mappingInfos != null) {
        for (RequestMappingInfo mappingInfo : mappingInfos) {
          requestMappingHandlerMapping.unregisterMapping(mappingInfo);
        }
        log.info("Unregistered {} endpoints for plugin: {}", mappingInfos.size(), pluginId);
      }
      List<Object> controllers = pluginControllers.remove(pluginId);
      if (controllers != null) {
        for (Object controller : controllers) {
          try {
            if (controller instanceof PluginController) {
              ((PluginController) controller).setPluginContext(null);
            }
            if (controller instanceof AutoCloseable) {
              ((AutoCloseable) controller).close();
            }
          } catch (Exception e) {
            log.debug("Failed to cleanup controller for plugin {}: {}", pluginId, e.getMessage());
          }
        }
      }
      return true;
    } catch (Exception e) {
      log.error("Failed to unregister plugin REST endpoints: " + pluginId, e);
      return false;
    }
  }

  private Object createControllerInstance(Class<?> controllerClass, PluginContext context)
      throws Exception {
    Object controller = controllerClass.getDeclaredConstructor().newInstance();
    if (controller instanceof PluginController) {
      ((PluginController) controller).setPluginContext(context);
    }
    applicationContext.getAutowireCapableBeanFactory().autowireBean(controller);
    return controller;
  }

  private List<RequestMappingInfo> registerControllerMethods(Object controller,
      Class<?> controllerClass, String apiPrefix) {
    List<RequestMappingInfo> mappingInfos = new ArrayList<>();
    RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
    String[] classLevelPaths = classMapping != null ? classMapping.value() : new String[]{""};
    for (Method method : controllerClass.getDeclaredMethods()) {
      RequestMappingInfo mappingInfo = createMappingInfo(method, classLevelPaths, apiPrefix);
      if (mappingInfo != null) {
        requestMappingHandlerMapping.registerMapping(mappingInfo, controller, method);
        mappingInfos.add(mappingInfo);
      }
    }
    return mappingInfos;
  }

  private RequestMappingInfo createMappingInfo(Method method, String[] classLevelPaths,
      String apiPrefix) {
    GetMapping getMapping = method.getAnnotation(GetMapping.class);
    PostMapping postMapping = method.getAnnotation(PostMapping.class);
    PutMapping putMapping = method.getAnnotation(PutMapping.class);
    DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
    PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

    String[] paths = null;
    RequestMethod[] methods = null;

    if (getMapping != null) {
      paths = getMapping.value();
      methods = new RequestMethod[]{RequestMethod.GET};
    } else if (postMapping != null) {
      paths = postMapping.value();
      methods = new RequestMethod[]{RequestMethod.POST};
    } else if (putMapping != null) {
      paths = putMapping.value();
      methods = new RequestMethod[]{RequestMethod.PUT};
    } else if (deleteMapping != null) {
      paths = deleteMapping.value();
      methods = new RequestMethod[]{RequestMethod.DELETE};
    } else if (patchMapping != null) {
      paths = patchMapping.value();
      methods = new RequestMethod[]{RequestMethod.PATCH};
    } else if (requestMapping != null) {
      paths = requestMapping.value();
      methods = requestMapping.method();
    }

    if (paths == null || paths.length == 0) {
      return null;
    }

    List<String> fullPaths = new ArrayList<>();
    for (String classPath : classLevelPaths) {
      for (String methodPath : paths) {
        String fullPath = combinePaths(apiPrefix, classPath, methodPath);
        fullPaths.add(fullPath);
      }
    }

    return RequestMappingInfo
        .paths(fullPaths.toArray(new String[0]))
        .methods(methods != null ? methods : new RequestMethod[0])
        .build();
  }

  private String combinePaths(String... paths) {
    StringBuilder result = new StringBuilder();
    for (String path : paths) {
      if (path == null || path.isEmpty()) {
        continue;
      }
      if (!path.startsWith("/")) {
        result.append("/");
      }
      result.append(path);
    }
    String finalPath = result.toString().replaceAll("/+", "/");
    return finalPath.isEmpty() ? "/" : finalPath;
  }

  public List<EndpointInfo> getPluginEndpoints(String pluginId) {
    List<RequestMappingInfo> mappingInfos = pluginEndpoints.get(pluginId);
    if (mappingInfos == null) {
      return Collections.emptyList();
    }
    List<EndpointInfo> out = new ArrayList<>();
    for (RequestMappingInfo info : mappingInfos) {
      out.add(toEndpointInfo(info));
    }
    return out;
  }

  public Map<String, List<EndpointInfo>> getAllPluginEndpoints() {
    Map<String, List<EndpointInfo>> result = new HashMap<>();
    pluginEndpoints.forEach(
        (pluginId, mappings) -> result.put(pluginId, getPluginEndpoints(pluginId)));
    return result;
  }

  private EndpointInfo toEndpointInfo(RequestMappingInfo mappingInfo) {
    Set<String> patterns = mappingInfo.getPatternValues();
    Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
    return EndpointInfo.builder().path(patterns.isEmpty() ? "" : patterns.iterator().next())
        .methods(methods.stream().map(Enum::name).toList()).build();
  }

  @Setter
  @Getter
  public static class EndpointInfo {

    private String path;
    private List<String> methods;

    public EndpointInfo() {
    }

    public EndpointInfo(String path, List<String> methods) {
      this.path = path;
      this.methods = methods;
    }

    public static EndpointInfo of(String path, List<String> methods) {
      return new EndpointInfo(path, methods);
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {

      private String path;
      private List<String> methods;

      public Builder path(String p) {
        this.path = p;
        return this;
      }

      public Builder methods(List<String> m) {
        this.methods = m;
        return this;
      }

      public EndpointInfo build() {
        return new EndpointInfo(path, methods);
      }
    }
  }
}
