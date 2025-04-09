## OAuth2 Resource Server Authentication

[English](README.md) | [中文](README_zh.md)

### Resource Server Introduction

- Acts as the protector of user resources, i.e., the storage service for specific resources.
- Accessing protected resources on the resource server requires an access token (`access_token`)
  obtained from the authorization server.
- A client is often also a resource server. When services communicate with each other (to access
  resources requiring permissions), they must carry an access token.
- For protected resources, the resource server first verifies the token upon receiving a request and
  then grants the client access to the resource.

### Main Extensions

- Enhanced optional auto-configuration with the addition of the `xcan.oauth.enabled` control option.
- Unified handling of authentication exceptions, ensuring that the exception response format aligns
  with `ApiResult`. This can be enabled automatically via `xcan.oauth.translateException=true`.
- Configuration of resource service types, supporting `user_info` and `store` methods. By
  default, `ResourceServerConfiguration` looks for `UserInfoService` or `TokenStore` implementations
  in the context for injection. If both exist, `UserInfoService` takes precedence.
- Definition of `PrincipalContext`, an identity context that stores information such as the current
  accessing client, tenant, user identity, and request after authentication. It proxies the use
  of `SecurityContextHolder` and can be enabled automatically
  via `xcan.oauth.holdType=resource_server` or `gateway_head`.
- Redis storage extension with support for cluster mode.

### Configuration Steps

#### 1. Add Dependency

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.auth-resource-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### 2. Configure `application-${profile}.yml`

Example configuration for the `local` environment:

```yml
# application-local.yml
spring:
  security:
    oauth2:
      resource-server:
        opaque:
          # Preventing abuse.
          # Authorization servers can ensure that only legitimate resource servers can verify tokens.
          client-id: client-credentials-introspect-client
          client-secret: secret
          introspection-uri: http://localhost:9090/oauth2/introspect
```

***Note:***

- By default, the following resources are not protected:

```java
  String[] AUTH_WHITELIST = {
    // springboot actuator
    "/actuator/**",
    "/pubapi/**",
    "/innerapi/**",
    //"/openapi2p/**",
    "/pubview/**",
    // -- swagger ui
    "/swagger-ui/*",
    "/swagger-resources/**",
    "/v2/api-docs",
    "/v3/api-docs",
    //"/webjars/**",
    // oauth public endpoint
    //"/oauth/user/login", "/oauth/authorize"
    //"/oauth/token"
};
```

```bash
curl -X POST http://localhost:9090/oauth2/token \
-H "Authorization: Basic $(echo -n 'password-client:secret' | base64)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=password&account=user1&password=password1"
```

Response Example:

```json
{
  "access_token": "qziqlG0vFI6dW8eZjQnxeH42n0p1AZRZ0KMD2iJgKNoZR46phaDa9ZzZXORDIhLxIN40zHXcoohv9_AqJVG1TA0JdZIMSEx6PagwZJtIk00XgVsXMjyrUZ0w9nM2j2UT",
  "refresh_token": "af3GWqaGZQcuTZmzKtV1r94PT29eKG2UotPCunQQdns2-RHmQDYW13qZwCw7SBS24OiauNwChXwc8VYPwZ1UsxgkS3CJA7IdSaEe4p9kNGjpFOw-YOX1roCfxiQN67xo",
  "token_type": "Bearer",
  "expires_in": 3599
}
```
