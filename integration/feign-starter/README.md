# Feign Integration Starter

Spring Cloud OpenFeign 声明式 HTTP 客户端集成模块。

## 功能特性

### 1. 声明式 HTTP 客户端
- **@FeignClient 注解**：通过接口定义 HTTP API
- **自动配置**：自动注入 Feign 客户端 Bean
- **负载均衡**：集成 Spring Cloud LoadBalancer
- **服务发现**：支持 Eureka、Consul、Nacos

### 2. HTTP 客户端支持
- **Apache HttpClient**：高性能 HTTP 客户端（默认）
- **OkHttp**：支持 HTTP/2、连接池
- **HttpClient5**：新一代 Apache HTTP 客户端
- **URLConnection**：JDK 内置（fallback）

### 3. 请求/响应处理
- **编码/解码**：自动 JSON 序列化/反序列化
- **请求拦截器**：统一添加 Header（Token、TraceId）
- **响应拦截器**：统一错误处理、日志记录
- **超时配置**：连接超时、读超时

### 4. 容错与重试
- **重试机制**：Spring Retry 集成
- **断路器**：Hystrix/Resilience4j 支持（可选）
- **降级策略**：Fallback 机制
- **超时保护**：防止服务雪崩

### 5. 日志与监控
- **请求日志**：记录请求/响应详情
- **性能指标**：集成 Micrometer
- **链路追踪**：传递 TraceId/SpanId
- **错误监控**：异常告警

## 依赖关系

```
feign-integration-starter
  ├── core-base (核心基础功能)
  ├── spring-cloud-starter-openfeign (Spring Cloud OpenFeign)
  ├── feign-httpclient (Apache HttpClient)
  ├── feign-okhttp (OkHttp，可选)
  ├── feign-hystrix (Hystrix 断路器，可选)
  ├── spring-retry (重试机制)
  ├── httpclient5 (Apache HttpClient 5，可选)
  ├── jackson-databind (JSON 序列化)
  └── lombok (编译时注解处理)
```

## 使用场景

### 场景 1: 微服务间调用
```java
@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/users/{id}")
    UserDTO getUser(@PathVariable Long id);
}
```

### 场景 2: 第三方 API 调用
```java
@FeignClient(name = "github-api", url = "https://api.github.com")
public interface GitHubClient {
    @GetMapping("/repos/{owner}/{repo}")
    RepoDTO getRepo(@PathVariable String owner, @PathVariable String repo);
}
```

### 场景 3: 统一请求头
```java
@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor authInterceptor() {
        return template -> {
            template.header("Authorization", "Bearer " + getToken());
            template.header("X-Trace-Id", MDC.get("traceId"));
        };
    }
}
```

### 场景 4: 重试与降级
```java
@FeignClient(name = "order-service", fallback = OrderClientFallback.class)
public interface OrderClient {
    @GetMapping("/orders/{id}")
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
    OrderDTO getOrder(@PathVariable Long id);
}
```

## 配置示例

### application.yml
```yaml
# Feign 客户端配置
feign:
  client:
    config:
      default:
        connectTimeout: 5000      # 连接超时（毫秒）
        readTimeout: 10000        # 读超时（毫秒）
        loggerLevel: BASIC        # 日志级别：NONE, BASIC, HEADERS, FULL
        
  # HTTP 客户端选择
  httpclient:
    enabled: true               # 启用 Apache HttpClient
    max-connections: 200        # 最大连接数
    max-connections-per-route: 50  # 每个路由最大连接数
    
  # OkHttp 配置（可选）
  okhttp:
    enabled: false
    
  # Hystrix 配置（可选）
  hystrix:
    enabled: false
    
  # 压缩配置
  compression:
    request:
      enabled: true
      mime-types: text/xml,application/xml,application/json
      min-request-size: 2048
    response:
      enabled: true

# 重试配置
spring:
  cloud:
    loadbalancer:
      retry:
        enabled: true
```

## 包结构

```
cloud.xcan.angus.core.feign/
  ├── client/               # Feign 客户端定义
  ├── config/               # Feign 配置类
  │   ├── FeignClientConfig.java
  │   └── FeignLoggerConfig.java
  ├── interceptor/          # 请求/响应拦截器
  │   ├── AuthInterceptor.java
  │   └── TraceIdInterceptor.java
  ├── codec/                # 编码/解码器
  ├── errorhandler/         # 错误处理器
  └── fallback/             # 降级处理
```

## 迁移内容

此模块包含从 `core/fegin/` 迁移的以下内容：
- Feign 客户端接口定义
- Feign 配置类
- 请求/响应拦截器
- 编码/解码器
- 错误处理器
- 降级逻辑

## 与其他模块的关系

- **依赖 core-base**: 使用 BizTemplate、异常处理、工具类
- **可选依赖 jackson-customizer**: 自定义 JSON 序列化规则
- **可选依赖 observability-starter**: 日志、指标、链路追踪

## 最佳实践

1. **合理设置超时**: 连接超时 3-5s，读超时 10-30s
2. **启用重试**: 对幂等操作启用重试机制
3. **使用断路器**: 防止级联故障
4. **日志脱敏**: 敏感信息（Token、密码）不记录
5. **连接池优化**: 根据并发量调整连接池大小

## 注意事项

- ⚠️ **仅在需要 Feign 时引入此 Starter**
- ⚠️ **不要在单体应用中使用**（除非调用外部 API）
- ⚠️ **避免循环依赖**（服务 A 调用 B，B 又调用 A）
- ⚠️ **注意超时级联**（调用链超时时间递减）
