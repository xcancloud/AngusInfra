package cloud.xcan.angus.core.enums;

import static cloud.xcan.angus.spec.experimental.BizConstant.GM_SERVICE;
import static cloud.xcan.angus.spec.experimental.BizConstant.GM_SERVICE_ARTIFACT_ID;
import static cloud.xcan.angus.spec.experimental.BizConstant.TESTER_SERVICE;
import static cloud.xcan.angus.spec.experimental.BizConstant.TESTER_SERVICE_ARTIFACT_ID;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.experimental.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.reflections.Reflections;
import org.springframework.util.CollectionUtils;

public class EnumStoreInMemory implements EnumStore {

  private final Map<String, Value[]> ALL_STORE = new ConcurrentHashMap<>();
  private final Map<String, Value[]> ENDPOINT_REGISTER_STORE = new ConcurrentHashMap<>();

  private final ApplicationInfo appInfo;

  public EnumStoreInMemory(ApplicationInfo appInfo) {
    this.appInfo = appInfo;
    init();
  }

  @Override
  public void init() {
    Reflections reflections = new Reflections("cloud.xcan.angus", "cloud.xcan.angus",
        "io.swagger.v3.oas.models.extension");
    Set<Class<? extends Value>> classes = reflections.getSubTypesOf(Value.class);
    if (isNotEmpty(classes)) {
      for (Class<? extends Value> clz : classes) {
        if (clz.isEnum()) {
          // Only register the sdf-api enum to the gm service
          boolean isSdfEnum = clz.getName().startsWith("cloud.xcan.angus.api.enums")
              || clz.getName().startsWith("cloud.xcan.angus.spec")
              || clz.getName().startsWith("io.swagger.v3.oas.models.extension");
          boolean isGmService = isNotEmpty(appInfo.getArtifactId()) && (
              appInfo.getArtifactId().equalsIgnoreCase(GM_SERVICE)
                  || appInfo.getArtifactId().equalsIgnoreCase(GM_SERVICE_ARTIFACT_ID)
          );
          if (isSdfEnum && !isGmService) {
            continue;
          }

          // Only register the angus-model enum to tester and ctrl service
          boolean isAngusModelEnum = clz.getName().startsWith("cloud.xcan.angus.model");
          boolean isTesterOrCtrlService = isNotEmpty(appInfo.getArtifactId()) && (
              appInfo.getArtifactId().equalsIgnoreCase(TESTER_SERVICE)
                  || appInfo.getArtifactId().equalsIgnoreCase(TESTER_SERVICE_ARTIFACT_ID)
          );
          if (isAngusModelEnum && !isTesterOrCtrlService) {
            continue;
          }

          String key = clz.getName().replaceFirst("cloud.xcan.angus", "xcm");
          ALL_STORE.put(key, clz.getEnumConstants());
          if (clz.isAnnotationPresent(EndpointRegister.class)) {
            ENDPOINT_REGISTER_STORE.put(key, clz.getEnumConstants());
          }
        }
      }
    }
  }

  @Override
  public void refresh() {
    clearStore();
    init();
  }

  @Override
  public List<String> getNames() {
    return new ArrayList<>(ALL_STORE.keySet());
  }

  @Override
  public List<String> getEndpointRegisterNames() {
    Set<String> values = ENDPOINT_REGISTER_STORE.keySet();
    return CollectionUtils.isEmpty(values) ? null : new ArrayList<>(values);
  }

  @Override
  public List<Value> get(String name) {
    Value[] values = ALL_STORE.get(name);
    return Objects.isNull(values) ? Collections.emptyList() : Arrays.asList(values);
  }

  @Override
  public Map<String, Value[]> get() {
    return ALL_STORE;
  }

  @Override
  public Map<String, Value[]> getEndpointRegister() {
    return ENDPOINT_REGISTER_STORE;
  }

  public void clearStore() {
    ALL_STORE.clear();
    ENDPOINT_REGISTER_STORE.clear();
  }
}
