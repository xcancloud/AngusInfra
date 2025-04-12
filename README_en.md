# AngusInfra

[English](README_en.md) | [中文](README.md)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.0-brightgreen)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-4.2.0-brightgreen)](https://spring.io/projects/spring-cloud)
[![Eureka Client](https://img.shields.io/badge/Eureka%20Client-2.0.4-lightgrey)](https://spring.io/projects/spring-cloud-netflix)
[![Open API](https://img.shields.io/badge/Open%20API-3.0.1-blue)](https://swagger.io/specification/)

**AngusInfra** is a rapid development foundational framework based on SpringBoot. It aims to
simplify and accelerate the development process of multi-tenant applications, enabling developers to
build scalable, secure, and maintainable server-side applications more efficiently.

## **Core Features**

- 🌟 **OpenAPI Support**: Strict compliance with OpenAPI specifications ensures broad community support for API development and management.

- 🌟 **Multi-Tenancy Architecture**: Designed as a shared-datasource multi-tenant system, AngusInfra simplifies management and reduces hardware/operational costs while ensuring robust data isolation and security.

- 🌟 **SpringBoot Foundation**: Built on SpringBoot with business-oriented extensions, it delivers powerful backend capabilities, leverages a mature ecosystem, and accelerates development workflows.

- 🌟 **Rapid Development**: Pre-built components and modules enable developers to quickly scaffold applications, cutting development cycles significantly.
  - 🚀 **Generic Business Logic**: Standardized templates for high-frequency business scenarios reduce redundant code by over 50%.
  - 🚀 **Rich Utilities**: Out-of-the-box APIs for data conversion, validation, and enhanced wrappers for mainstream middleware (Cache/Database/Remote) boost development efficiency by 50%.

- 🌟 **Extensibility**: Modular architecture allows flexible customization of business logic and user interfaces to meet project-specific needs.

- 🌟 **Security-First Design**: Integrated authentication and authorization mechanisms (OAuth2, API Key, etc.) safeguard user data and application integrity.

## **Core Modules**
| Module | Description                                                                                                                                                                                  |  
|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|  
| **`spec`** | Public specifications library defining global common models (DTOs/enums/error codes), interface contracts, and cross-module communication protocols.                                         |  
| **`core`** | Core foundational library providing common utilities (Exception handling, Event collection, Multi-Tenancy handling, Spring extensions, etc.) as the underlying support for all components. |  

### **Data Layer Modules**
| Module | Description |  
|--------|-------------|  
| **`datasource`** | Datasource extension library offering multi-tenant dynamic datasource configuration, basic sharding strategies, and read-write separation based on Spring JPA. |  
| **`l2cache`** | Two-level cache library integrating Redis + Caffeine for high-performance caching, supporting distributed consistency and cache penetration prevention strategies. |  
| **`lettucex`** | Redis enhancement library standardizing Lettuce configurations and providing business extension utilities. |  

### **Security & Authentication**
| Module | Description |  
|--------|-------------|  
| **`security/auth-server-starter`** | OAuth2 authorization server with automated token issuance, key management, and authorization endpoint configuration. |  
| **`security/auth-resource-starter`** | Resource server component supporting password and client credentials grant types for resource access authorization. |  
| **`security/auth-openapi-starter`** | Private API authentication module (OAuth2 client credentials) for third-party system integration. |  
| **`security/auth-openapi2p-starter`** | Open API authentication module (API Key schema) for standardized SaaS private deployment authorization. |  
| **`security/auth-innerapi-starter`** | Internal service authentication module (lightweight signature verification) for secure inter-service communication. |  

### **SpringBoot Rapid Integration**
| Module | Description                                                                                                                  |  
|--------|------------------------------------------------------------------------------------------------------------------------------|  
| **`integration/web-starter`** | RESTful API development toolkit with auto-configured unified response formats, global exception handling, and CORS policies. |  
| **`integration/oas3-starter`** | OpenAPI 3.x support with automated API documentation generation and Swagger UI integration.                                  |  
| **`integration/validator-starter`** | Enhanced validation library extending JSR-380 with business rules (password/mobile/ID card format validation).               |  

### **Development Toolchain**
| Module | Description |  
|--------|-------------|  
| **`idgen`** | Distributed ID generator supporting Snowflake, custom business IDs, and other strategies. |  
| **`validator`** | Enhanced validation library with annotation-based rules and custom validator templates. |  
| **`checkstyle`** | Automated code style rules enforcing Google coding standards. |  
| **`remote`** | OpenFeign-based remote call library with DTO/VO/TO definitions, unified response formats, and multi-language support. |  
| **`bom`** | Bill of Materials (BOM) for centralized dependency version management and conflict resolution. |  

### **Architecture Governance**
| Module | Description |  
|--------|-------------|  
| **`parent`** | Maven parent POM defining global build configurations, plugin management, and profile strategies. |  
| **`docs`** | Documentation repository including architecture design, module guides, and quickstart instructions. |  

## Use Cases

💡 AngusInfra is particularly well-suited for SaaS applications that require multi-tenant support,
enterprise internal systems, and other web applications that need rapid development and deployment.

## License

📜 Licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.html)
