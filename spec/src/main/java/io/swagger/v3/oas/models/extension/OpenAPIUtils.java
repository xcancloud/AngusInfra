package io.swagger.v3.oas.models.extension;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.emptySafe;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.spec.annotations.ThirdExtension;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ThirdExtension
public class OpenAPIUtils {

  private static final String REF_FIELD = "$ref";

  /**
   * @see swagger-parser#InlineModelResolver
   */
  public static Map<String, io.swagger.v3.oas.models.Operation> flatPaths(Paths paths) {
    if (ObjectUtils.isEmpty(paths)) {
      // Warn: Since 3.1 is not required
      return null;
    }
    Map<String, io.swagger.v3.oas.models.Operation> operationsMap = new HashMap<>();

    // Flat operation
    for (String uri : paths.keySet()) {
      io.swagger.v3.oas.models.PathItem pathItem = paths.get(uri);
      Map<String, io.swagger.v3.oas.models.Operation> operations = new HashMap<>();
      if (nonNull(pathItem)) {
        boolean hasContext = false;
        if (nonNull(pathItem.getGet())) {
          hasContext = flatToOperation(uri, operations, pathItem.getGet(), "get",
              pathItem.getParameters());
        }
        if (nonNull(pathItem.getPut())) {
          hasContext = flatToOperation(uri, operations, pathItem.getPut(), "put",
              pathItem.getParameters());
        }
        if (nonNull(pathItem.getPost())) {
          hasContext = flatToOperation(uri, operations, pathItem.getPost(), "post",
              pathItem.getParameters());
        }
        if (nonNull(pathItem.getDelete())) {
          hasContext = flatToOperation(uri, operations, pathItem.getDelete(), "delete",
              pathItem.getParameters());
          addFlatOperation(pathItem, operations, hasContext);
        }
        if (nonNull(pathItem.getOptions())) {
          hasContext = flatToOperation(uri, operations, pathItem.getOptions(), "options",
              pathItem.getParameters());
        }
        if (nonNull(pathItem.getHead())) {
          hasContext = flatToOperation(uri, operations, pathItem.getHead(), "head",
              pathItem.getParameters());
        }
        if (nonNull(pathItem.getPatch())) {
          hasContext = flatToOperation(uri, operations, pathItem.getPatch(), "patch",
              pathItem.getParameters());
        }
        if (nonNull(pathItem.getTrace())) {
          hasContext = flatToOperation(uri, operations, pathItem.getTrace(), "trace",
              pathItem.getParameters());
        }

        // Parse pathItem $ref
        if (!hasContext && isNotEmpty(pathItem.get$ref())) {
          log.warn("Flat paths unhand $ref: {}", pathItem.get$ref());
        }

        if (isNotEmpty(operations)) {
          addFlatOperation(pathItem, operations, hasContext);

          operationsMap.putAll(operations);
        }
      }
    }
    return operationsMap;
  }

  private static void addFlatOperation(
      PathItem pathItem, Map<String, io.swagger.v3.oas.models.Operation> operations,
      boolean hasContext) {
    if (hasContext) {
      for (io.swagger.v3.oas.models.Operation operation : operations.values()) {
        // Merge common parameters schema in pathItem
        if (nonNull(pathItem.getParameters())) {
          for (Parameter parameter : pathItem.getParameters()) {
            operation.addParametersItem(parameter);
          }
        }
        // Merge common servers schema in pathItem
        if (nonNull(pathItem.getServers())) {
          for (Server server : pathItem.getServers()) {
            operation.addServersItem(server);
          }
        }
        // Merge common extension schema in pathItem
        if (nonNull(pathItem.getExtensions())) {
          for (String key : pathItem.getExtensions().keySet()) {
            operation.addExtension(key, pathItem.getExtensions().get(key));
          }
        }
      }
    }
  }

  private static boolean flatToOperation(String uri,
      Map<String, io.swagger.v3.oas.models.Operation> operations,
      Operation operation, String method, List<Parameter> parameters) {
    // Safe uri
    uri = isEmpty(uri) ? "" : uri.split("\\?")[0];

    operation.method = method;
    // operation.currentServer = operation.currentServer;
    operation.endpoint = uri;
    // operation.authentication = operation.authentication;;
    // Fix:: A list of parameters that are applicable for all the operations described under this path
    if (isNotEmpty(parameters)) {
      if (isEmpty(operation.getParameters())) {
        operation.setParameters(parameters);
      } else {
        // need to propagate path level down to the operation
        Map<String, Parameter> parametersMap = operation.getParameters().stream()
            .collect(Collectors.toMap(OpenAPIUtils::generateParameterId, x -> x));
        for (Parameter parameter : parameters) {
          // skip propagation if a parameter with the same name is already defined at the operation level
          if (!parametersMap.containsKey(generateParameterId(parameter))) {
            operation.getParameters().add(parameter);
          }
        }
      }
    }
    operations.put(method + ":" + uri, operation);
    return true;
  }

  private static String generateParameterId(Parameter parameter) {
    return parameter.getName() + ":" + parameter.getIn();
  }

  public static String getExtensionString(Map<String, Object> extensions, String key,
      String defaultValue) {
    return emptySafe(getExtensionString(extensions, key), defaultValue);
  }

  public static String getExtensionString(Map<String, Object> extensions, String key) {
    if (isEmpty(extensions)) {
      return null;
    }
    Object value = extensions.get(key);
    return isNull(value) ? null : value.toString();
  }

  public static Long getExtensionLong(Map<String, Object> extensions, String key,
      Long defaultValue) {
    return nullSafe(getExtensionLong(extensions, key), defaultValue);
  }

  public static Long getExtensionLong(Map<String, Object> extensions, String key) {
    if (isEmpty(extensions)) {
      return null;
    }
    Object value = extensions.get(key);
    return isNull(value) ? null : Long.valueOf(value.toString());
  }

  public static boolean getExtensionBoolean(Map<String, Object> extensions, String key,
      boolean defaultValue) {
    return nullSafe(getExtensionBoolean(extensions, key), defaultValue);
  }

  public static Boolean getExtensionBoolean(Map<String, Object> extensions, String key) {
    if (isEmpty(extensions)) {
      return null;
    }
    Object value = extensions.get(key);
    if (isNull(value)) {
      return null;
    }
    String valueStr = value.toString();
    if ("true".equalsIgnoreCase(valueStr)) {
      return true;
    }
    if ("false".equalsIgnoreCase(valueStr)) {
      return false;
    }
    return null;
  }

  public static Set<String> findAllRef(Map<String, Object> openApi) {
    Set<String> refPaths = new HashSet<>();
    findRefPaths(openApi, "", refPaths);
    return refPaths;
  }

  private static void findRefPaths(Object object, String path, Set<String> refPaths) {
    if (object instanceof Map) {
      Map<String, Object> map = (Map<String, Object>) object;
      for (Map.Entry<String, Object> entry : map.entrySet()) {
        String key = entry.getKey();
        Object value = entry.getValue();
        String newPath = path + "/" + key;
        if (key.equals(REF_FIELD)) {
          if (value instanceof String) {
            refPaths.add((String) value);
          }
        } else if (value instanceof Map) {
          findRefPaths(value, newPath, refPaths);
        } else if (value instanceof List) {
          findRefPaths((List<Object>) value, newPath, refPaths);
        }
      }
    } else if (object instanceof List) {
      findRefPaths((List<Object>) object, path, refPaths);
    }
  }

  private static void findRefPaths(List<Object> list, String path, Set<String> refPaths) {
    int index = 0;
    for (Object object : list) {
      String newPath = path + "[" + index + "]";
      findRefPaths(object, newPath, refPaths);
      index++;
    }
  }
}
