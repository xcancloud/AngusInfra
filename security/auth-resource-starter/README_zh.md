## OAuth2 资源服务器认证

[English](README.md) | [中文](README_zh.md)

### 资源服务（Resource Server）介绍

- 作为用户资源持有保护者，即具体资源的存储服务。
- 要访问资源服务器受保护的资源需要携带访问令牌 access_token（从授权服务器获得）。
- 客户端往往同时也是一个资源服务器，各个服务之间的通信（访问需要权限的资源）时需携带访问令牌。
- 对于受保护的资源，资源服务器接受到请求后先确认令牌无误，然后向客户端开放资源。

### 主要扩展

- 自动装配可选性加强，增加 xcan.oauth.enabled 控制选项。
- 认证异常统一处理，使异常返回时报文格式和 ApiResult 保持一致，通过 xcan.oauth.translateException=true
  来自动开启。
- 配置资源服务类型，支持 user_info 和 store 两种方式，默认 ResourceServerConfiguration 会从 Context 中找
  UserInfoService 或 TokenStore 实现来注入，同时存在时其中 UserInfoService 优先级更高。
- 定义 PrincipalContext 身份上下文，认证后存储当前访问端、租户、用户身份、请求等信息，代理使用
  SecurityContextHolder，通过 xcan.oauth.holdType=resource_server或gateway_head 来自动开启。
- Redis 存储扩展支持集群模式。

### 配置步骤

#### 1. 引入依赖

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-infra.auth-resource-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

#### 2. 配置 application-${profile}.yml

local 环境配置示例：

```yml
# application-local.yml
spring:
  security:
    oauth2:
      resource-server:
        opaque-token:
          # Preventing abuse.
          # Authorization servers can ensure that only legitimate resource servers can verify tokens.
          client-id: client-credentials-introspect-client
          client-secret: secret
          introspection-uri: http://localhost:9090/oauth2/introspect
```

***注意：***

- 默认忽略保护的资源包括：

```java
  String[] AUTH_WHITELIST = {
    // springboot actuator
    "/actuator/**",
    "/pubapi/**",
    "/doorapi/**",
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

#### 3. 获取 access_token 访问受保护 API

```bash
curl -X POST http://localhost:9090/oauth2/token \
-H "Authorization: Basic $(echo -n 'password-client:secret' | base64)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=password&account=user1&password=password1"
```

响应示例:

```json
{
  "access_token": "qziqlG0vFI6dW8eZjQnxeH42n0p1AZRZ0KMD2iJgKNoZR46phaDa9ZzZXORDIhLxIN40zHXcoohv9_AqJVG1TA0JdZIMSEx6PagwZJtIk00XgVsXMjyrUZ0w9nM2j2UT",
  "refresh_token": "af3GWqaGZQcuTZmzKtV1r94PT29eKG2UotPCunQQdns2-RHmQDYW13qZwCw7SBS24OiauNwChXwc8VYPwZ1UsxgkS3CJA7IdSaEe4p9kNGjpFOw-YOX1roCfxiQN67xo",
  "token_type": "Bearer",
  "expires_in": 3599
}
```
