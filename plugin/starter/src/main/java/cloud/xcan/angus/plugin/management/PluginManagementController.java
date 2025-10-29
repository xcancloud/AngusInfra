package cloud.xcan.angus.plugin.management;

import static cloud.xcan.angus.plugin.api.RestfulApiResult.SYSTEM_ERROR_CODE;

import cloud.xcan.angus.plugin.api.RestfulApiResult;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/plugin-management")
public class PluginManagementController {

  private final PluginManagementService managementService;

  @Autowired
  public PluginManagementController(PluginManagementService managementService) {
    this.managementService = managementService;
  }

  @Operation(operationId = "installPlugin", summary = "Install plugin",
      description = "Upload plugin jar and install it into the configured store.")
  @PostMapping(value = "/install", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.OK)
  public RestfulApiResult<PluginInfo> install(
      @Parameter(description = "Plugin identifier", required = true) @RequestParam("pluginId") String pluginId,
      @Parameter(description = "Plugin jar file", required = true) @RequestParam("file") MultipartFile file) {
    try {
      PluginInfo pluginInfo = managementService.install(pluginId, file.getBytes());
      return RestfulApiResult.success(pluginInfo);
    } catch (Exception e) {
      return RestfulApiResult.error(SYSTEM_ERROR_CODE, e.getMessage());
    }
  }

  @Operation(operationId = "removePlugin", summary = "Remove plugin",
      description = "Unload and optionally remove a plugin from store")
  @DeleteMapping("/{pluginId}")
  @ResponseStatus(HttpStatus.OK)
  public RestfulApiResult<?> remove(
      @Parameter(description = "Plugin identifier", required = true) @PathVariable String pluginId,
      @Parameter(description = "Also remove from store", required = false)
      @RequestParam(required = false, defaultValue = "true") boolean removeFromStore) {
    try {
      managementService.remove(pluginId, removeFromStore);
    } catch (PluginException e) {
      return RestfulApiResult.error(SYSTEM_ERROR_CODE, e.getMessage());
    }
    return RestfulApiResult.success();
  }

  @Operation(operationId = "getPlugin", summary = "Get plugin details",
      description = "Get detailed information for a plugin by its id")
  @GetMapping("/{pluginId}")
  @ResponseStatus(HttpStatus.OK)
  public RestfulApiResult<PluginInfo> getPlugin(
      @Parameter(description = "Plugin identifier", required = true) @PathVariable String pluginId) {
    PluginInfo info = managementService.getPlugin(pluginId);
    if (info == null) {
      return RestfulApiResult.error(SYSTEM_ERROR_CODE, "plugin not found");
    }
    return RestfulApiResult.success(info);
  }


  @Operation(operationId = "listPlugins", summary = "List plugins",
      description = "List all installed/loaded plugins")
  @GetMapping("/list")
  @ResponseStatus(HttpStatus.OK)
  public RestfulApiResult<List<PluginInfo>> list() {
    List<PluginInfo> list = managementService.listPlugins();
    return RestfulApiResult.success(list);
  }

  @Operation(operationId = "getPluginStats", summary = "Get plugin system statistics",
      description = "Returns total plugin count, active plugins and number of REST endpoints")
  @GetMapping("/stats")
  @ResponseStatus(HttpStatus.OK)
  public RestfulApiResult<PluginStats> stats() {
    return RestfulApiResult.success(managementService.stats());
  }
}
