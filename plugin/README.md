# AngusInfra — Plugin Module

[English](README.md) | [中文](README_zh.md)

## Overview

The `plugin` module provides an extensible plugin framework for AngusInfra. Key features include:

- Storage and management of plugin artifacts (Disk or JPA storage backends)
- Plugin lifecycle management (install, uninstall, load, unload)
- Isolated class loading (`PluginClassLoader`) and a plugin runtime context
- Dynamic registration of REST endpoints (plugins can expose APIs at runtime)
- Management APIs for uploading, removing, listing and inspecting plugins

## Module Layout

- `api` — Public plugin interfaces and models (e.g. `Plugin`, `PluginContext`, `PluginManager`, `PluginInfo`).
- `core` — Core runtime implementations (default manager, class loader, store interfaces and implementations).
- `starter` — Spring Boot starter that provides auto-configuration, optional JPA store, and management controllers.
- `examples` — Example plugin projects (e.g. `angus-plugin.github`, `angus-plugin.jenkins`).
- `bom` — Dependency version management.

## Key Interfaces / Classes (examples)

- `cloud.xcan.angus.plugin.api.Plugin`
- `cloud.xcan.angus.plugin.api.PluginManager`
- `cloud.xcan.angus.plugin.api.PluginContext`
- `cloud.xcan.angus.plugin.model.PluginInfo`, `PluginDescriptor`, `PluginState`
- `cloud.xcan.angus.plugin.core.DefaultPluginManager`
- `cloud.xcan.angus.plugin.core.PluginClassLoader`
- `cloud.xcan.angus.plugin.store.PluginStore` (implementations: `DiskPluginStore`, `JpaPluginStore`)
- `cloud.xcan.angus.plugin.core.DynamicRestEndpointManager`
- `cloud.xcan.angus.plugin.autoconfigure.PluginProperties`

## Quick Start

### 1. Build the module

From the project root or inside the `AngusInfra` module run:

```bash
mvn -pl plugin -am clean install
```

### 2. Add the starter to your Spring Boot application

Maven dependency example:

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.plugin-starter</artifactId>
  <version>${project.version}</version>
</dependency>
```

### 3. Configuration (example `application.yml`)

The starter binds configuration to `StarterPluginProperties` (which extends `PluginProperties`). Configuration prefix: `angus.plugin`.

```yaml
angus:
  plugin:
    storage-type: DISK          # DISK or JPA
    directory: ./plugins       # Directory for plugin jars when using disk storage
    data-directory: ./plugin-data
    auto-load: true
    max-upload-size: 52428800  # Max upload size (bytes)
    enable-management-api: true
    management-api-prefix: /api/plugins
    enable-security-check: true
    allowed-sources: ['*']
    scan-interval: 30000
    validate-on-startup: true

spring:
  datasource:
    url: jdbc:h2:mem:angusdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: update
```

> Note: The property names in YAML use Spring Boot kebab-case (e.g. `storage-type`, `data-directory`) and map to fields in `PluginProperties`.

## Management API

The `starter` module includes `PluginManagementController` which is mapped by default under:

`/api/v1/plugin-management`

Main REST endpoints:

- `POST /api/v1/plugin-management/install` — Upload and install a plugin (multipart/form-data with `pluginId` and `file` parameters)
- `DELETE /api/v1/plugin-management/{pluginId}` — Uninstall a plugin; optional query parameter `removeFromStore` (default `true`)
- `GET /api/v1/plugin-management/{pluginId}` — Get plugin details
- `GET /api/v1/plugin-management/list` — List all plugins
- `GET /api/v1/plugin-management/stats` — Get plugin system statistics

Example: upload a plugin with curl (multipart):

```bash
curl -v \
  -F "pluginId=my-plugin" \
  -F "file=@./my-plugin.jar" \
  http://localhost:8080/api/v1/plugin-management/install
```

## Building and Deploying Plugins

1. See the example plugins under the `examples` submodule (for instance `angus-plugin.github`, `angus-plugin.jenkins`).
2. Each example includes a `plugin.json` describing the plugin id, version and entry class. Build the plugin as a JAR.
3. Place the plugin JAR into the configured plugin directory (`angus.plugin.directory`, e.g. `./plugins`), or upload and install it via the management API.
4. After installation the `PluginManager` will manage the plugin lifecycle. If `auto-load=true`, plugins found in the directory will be automatically loaded on application startup.

## Testing

Run unit tests for the core module:

```bash
mvn -pl plugin/core test
```

## Contributing

Contributions via issues and pull requests are welcome. Suggested guidelines:

- Add unit tests that cover key logic
- Follow existing coding style and Lombok usage patterns

