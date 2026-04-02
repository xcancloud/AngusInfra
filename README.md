# AngusInfra

[English](README.md) | [中文](README_zh.md)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2024.0.0-green)](https://spring.io/projects/spring-cloud)
[![Eureka Client](https://img.shields.io/badge/Eureka%20Client-4.2.0-lightgrey)](https://spring.io/projects/spring-cloud-netflix)
[![Open API](https://img.shields.io/badge/Open%20API-3.0.1-blue)](https://swagger.io/specification/)

**AngusInfra** is a rapid development foundational framework based on SpringBoot. It aims to
simplify and accelerate the development process of multi-tenant applications, enabling developers to
build scalable, secure, and maintainable server-side applications more efficiently.

## **Core Features**

- 🌟 **OpenAPI Support**: Strict compliance with OpenAPI specifications ensures broad community
  support for API development and management.

- 🌟 **Multi-Tenancy Architecture**: Designed as a shared-datasource multi-tenant system, AngusInfra
  simplifies management and reduces hardware/operational costs while ensuring robust data isolation
  and security.

- 🌟 **SpringBoot Foundation**: Built on SpringBoot with business-oriented extensions, it delivers
  powerful backend capabilities, leverages a mature ecosystem, and accelerates development
  workflows.

- 🌟 **Rapid Development**: Pre-built components and modules enable developers to quickly scaffold
  applications, cutting development cycles significantly.
    - 🚀 **Generic Business Logic**: Standardized templates for high-frequency business scenarios
      reduce redundant code by over 50%.
    - 🚀 **Rich Utilities**: Out-of-the-box APIs for data conversion, validation, and enhanced
      wrappers for mainstream middleware (Cache/Database/Remote) boost development efficiency by
      50%.

- 🌟 **Extensibility**: Modular architecture allows flexible customization of business logic and user
  interfaces to meet project-specific needs.

- 🌟 **Security-First Design**: Integrated authentication and authorization mechanisms (OAuth2, API
  Key, etc.) safeguard user data and application integrity.

## **Core Modules**

| Module     | Description                                                                                                                                          |
|------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`spec`** | Public specifications library defining global common models (DTOs/enums/error codes), interface contracts, and cross-module communication protocols. |
| **`core`** | Core base library providing fundamental utilities, exception handling, multi-tenancy support, business templates, and Spring extensions.             |

### **Data Layer Modules**

| Module             | Description                                                                                                                                                        |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`jpa-starter`**  | Spring Data JPA integration starter providing multi-tenant dynamic datasource configuration, generic repositories, Specification builders, and audit support.      |
| **`sharding`**     | Production-ready multi-tenant database sharding framework with dynamic routing DataSource, SQL table-name rewriting, pluggable shard-key resolution, and automatic schema management. Supports modulo and consistent-hash strategies, in-memory/JDBC/JPA table registry, and a clean SPI model. See [sharding/README.md](sharding/README.md). |
| **`cache`**      | Two-level cache library combining in-memory (Caffeine) and database (JPA) persistence, with management REST API and Spring Boot auto-configuration.                |
| **`l2cache`**    | Two-level cache library integrating Redis + Caffeine for high-performance caching, supporting distributed consistency and cache penetration prevention strategies. |
| **`lettucex`**   | Redis enhancement library standardizing Lettuce configurations and providing business extension utilities.                                                         |

### **Distributed Infrastructure**

| Module      | Description                                                                                                                                                 |
|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`idgen`** | Distributed ID generator supporting Snowflake-variant UidGenerator, custom business ID (BidGenerator), and caching strategies.                              |
| **`job`**   | Database-driven distributed task scheduling framework with SIMPLE, SHARDING, and MAP_REDUCE execution models, distributed locking, and management REST API. |
| **`queue`** | Database-backed message queue implementing lease-based SQS-style semantics with partitioning, dead-letter support, lifecycle management, and REST API.      |

### **Plugin Framework**

| Module       | Description                                                                                                                                           |
|--------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`plugin`** | Extensible plugin framework with dynamic class loading, lifecycle management, and hot-plugging support (includes api, core, and starter sub-modules). |

### **Security & Authentication**

| Module                                | Description                                                                                                           |
|---------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| **`security/auth-resource-model`**    | Authorization persistence resource models and DTOs for resource authentication.                                       |
| **`security/auth-server-starter`**    | OAuth2 authorization server with automated token issuance, key management, and authorization endpoint configuration.  |
| **`security/auth-resource-starter`**  | OAuth2 resource server supporting password and client credentials grant types for resource access authorization.      |
| **`security/auth-openapi2p-starter`** | Private API authentication module (OAuth2 client credentials) for standardized SaaS private deployment authorization. |
| **`security/auth-innerapi-starter`**  | Internal service authentication module (OAuth2 client credentials) for secure inter-service communication.            |

### **SpringBoot Rapid Integration**

| Module                                  | Description                                                                                                                  |
|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| **`integration/web-starter`**           | RESTful API development toolkit with auto-configured unified response formats, global exception handling, and CORS policies. |
| **`integration/oas3-starter`**          | OpenAPI 3.x support with automated API documentation generation and Swagger UI integration.                                  |
| **`integration/feign-starter`**         | Spring Cloud OpenFeign declarative HTTP client integration with encoding, decoding, and error handling.                      |
| **`integration/observability-starter`** | Logging, metrics, and data export utilities for application observability.                                                   |

### **Development Toolchain**

| Module          | Description                                                                                                           |
|-----------------|-----------------------------------------------------------------------------------------------------------------------|
| **`validator`** | Enhanced validation library with annotation-based rules and custom validator templates.                               |
| **`remote`**    | OpenFeign-based remote call library with DTO/VO/TO definitions, unified response formats, and multi-language support. |
| **`bom`**       | Bill of Materials (BOM) for centralized dependency version management and conflict resolution.                        |

### **Architecture Governance**

| Module       | Description                                                                                         |
|--------------|-----------------------------------------------------------------------------------------------------|
| **`parent`** | Maven parent POM defining global build configurations, plugin management, and profile strategies.   |
| **`docs`**   | Documentation repository including architecture design, module guides, and quickstart instructions. |  

## Use Cases

💡 AngusInfra is particularly well-suited for SaaS applications that require multi-tenant support,
enterprise internal systems, and other web applications that need rapid development and deployment.

## License

📜 Licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
