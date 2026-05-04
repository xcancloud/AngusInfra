package cloud.xcan.angus.plugin.web;

import static cloud.xcan.angus.remote.ApiConstant.ECode.SYSTEM_ERROR_CODE;

import cloud.xcan.angus.plugin.autoconfigure.PluginProperties;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.management.PluginManagementService;
import cloud.xcan.angus.plugin.management.PluginStats;
import cloud.xcan.angus.plugin.model.PluginInfo;
import cloud.xcan.angus.remote.ApiLocaleResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Tag(name = "Plugin", description = "APIs to manage and monitor the plugins")
@RestController
@RequestMapping("/api/v1/plugin-management")
public class PluginManagementController {

  private final PluginManagementService managementService;
  private final PluginProperties pluginProperties;

  @Autowired
  public PluginManagementController(PluginManagementService managementService,
      PluginProperties pluginProperties) {
    this.managementService = managementService;
    this.pluginProperties = pluginProperties;
  }

  @Operation(operationId = "installPlugin", summary = "Install plugin",
      description = "Upload plugin jar and install it into the configured store.")
  @PostMapping(value = "/install", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.OK)
  public ApiLocaleResult<PluginInfo> install(
      @Parameter(description = "Plugin identifier", required = true) @RequestParam("pluginId") String pluginId,
      @Parameter(description = "Plugin jar file", required = true) @RequestParam("file") MultipartFile file) {
    try {
      if (pluginId == null || pluginId.isBlank()) {
        return ApiLocaleResult.error(SYSTEM_ERROR_CODE, "pluginId is required", null);
      }
      String originalFilename = file.getOriginalFilename();
      if (originalFilename == null || !originalFilename.endsWith(".jar")) {
        return ApiLocaleResult.error(SYSTEM_ERROR_CODE,
            "Only .jar files are accepted", null);
      }
      if (file.getSize() > pluginProperties.getMaxUploadSize()) {
        return ApiLocaleResult.error(SYSTEM_ERROR_CODE,
            "File size exceeds maximum allowed: " + pluginProperties.getMaxUploadSize() + " bytes",
            null);
      }
      log.info("Plugin install requested: pluginId={}, fileName={}, fileSize={}",
          pluginId, originalFilename, file.getSize());
      PluginInfo pluginInfo = managementService.install(pluginId, file.getBytes());
      if (pluginInfo == null) {
        return ApiLocaleResult.error(SYSTEM_ERROR_CODE,
            "Plugin stored but failed to load", null);
      }
      return ApiLocaleResult.success(pluginInfo);
    } catch (Exception e) {
      log.error("Plugin install failed for: {}", pluginId, e);
      return ApiLocaleResult.error(SYSTEM_ERROR_CODE,
          e.getMessage() != null ? e.getMessage() : "Unexpected error", null);
    }
  }

  @Operation(operationId = "removePlugin", summary = "Remove plugin",
      description = "Unload and optionally remove a plugin from store")
  @DeleteMapping("/{pluginId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(
      @Parameter(description = "Plugin identifier", required = true) @PathVariable String pluginId,
      @Parameter(description = "Also remove from store", required = false)
      @RequestParam(required = false, defaultValue = "true") boolean removeFromStore)
      throws PluginException {
    log.warn("Plugin remove requested: pluginId={}, removeFromStore={}", pluginId, removeFromStore);
    managementService.remove(pluginId, removeFromStore);
  }

  @Operation(operationId = "getPlugin", summary = "Get plugin details",
      description = "Get detailed information for a plugin by its id")
  @GetMapping("/{pluginId}")
  @ResponseStatus(HttpStatus.OK)
  public ApiLocaleResult<PluginInfo> getPlugin(
      @Parameter(description = "Plugin identifier", required = true) @PathVariable String pluginId) {
    PluginInfo info = managementService.getPlugin(pluginId);
    if (info == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plugin not found: " + pluginId);
    }
    return ApiLocaleResult.success(info);
  }

  @Operation(operationId = "listPlugins", summary = "List plugins",
      description = "List all installed/loaded plugins")
  @GetMapping("/list")
  @ResponseStatus(HttpStatus.OK)
  public ApiLocaleResult<List<PluginInfo>> list() {
    List<PluginInfo> list = managementService.listPlugins();
    return ApiLocaleResult.success(list);
  }

  @Operation(operationId = "getPluginStats", summary = "Get plugin system statistics",
      description = "Returns total plugin count, active plugins and number of REST endpoints")
  @GetMapping("/stats")
  @ResponseStatus(HttpStatus.OK)
  public ApiLocaleResult<PluginStats> stats() {
    return ApiLocaleResult.success(managementService.stats());
  }
}
