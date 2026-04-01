/**
 * Feign Integration Starter - Spring Cloud OpenFeign 声明式 HTTP 客户端集成
 *
 * <h2>模块职责</h2>
 * <ul>
 *   <li>提供 Feign 客户端自动配置</li>
 *   <li>统一请求/响应拦截处理</li>
 *   <li>集成重试、降级、断路器</li>
 *   <li>支持服务发现与负载均衡</li>
 *   <li>提供日志、指标、链路追踪</li>
 * </ul>
 *
 * <h2>核心功能</h2>
 * <ul>
 *   <li><b>声明式客户端</b>: 通过 @FeignClient 定义 HTTP API</li>
 *   <li><b>多 HTTP 客户端</b>: Apache HttpClient、OkHttp、HttpClient5</li>
 *   <li><b>请求拦截器</b>: 统一添加 Token、TraceId、自定义 Header</li>
 *   <li><b>响应处理</b>: 统一错误处理、日志记录</li>
 *   <li><b>重试机制</b>: Spring Retry 集成，可配置重试策略</li>
 *   <li><b>断路器</b>: Hystrix/Resilience4j 支持（可选）</li>
 *   <li><b>降级策略</b>: Fallback 机制，服务不可用时返回默认值</li>
 * </ul>
 *
 * <h2>依赖关系</h2>
 * <pre>
 * feign-starter
 *   ├── core-base (核心基础功能)
 *   ├── spring-cloud-starter-openfeign (Spring Cloud OpenFeign)
 *   ├── feign-httpclient (Apache HttpClient)
 *   ├── feign-okhttp (OkHttp，可选)
 *   ├── feign-hystrix (Hystrix 断路器，可选)
 *   ├── spring-retry (重试机制)
 *   └── jackson-databind (JSON 序列化)
 * </pre>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 1. 定义 Feign 客户端
 * @FeignClient(name = "user-service", fallback = UserClientFallback.class)
 * public interface UserClient {
 *     @GetMapping("/users/{id}")
 *     UserDTO getUser(@PathVariable Long id);
 * }
 *
 * // 2. 配置请求拦截器
 * @Configuration
 * public class FeignConfig {
 *     @Bean
 *     public RequestInterceptor authInterceptor() {
 *         return template -> {
 *             template.header("Authorization", "Bearer " + getToken());
 *             template.header("X-Trace-Id", MDC.get("traceId"));
 *         };
 *     }
 * }
 *
 * // 3. 使用客户端
 * @Service
 * public class UserService {
 *     @Autowired
 *     private UserClient userClient;
 *
 *     public UserDTO getUserInfo(Long userId) {
 *         return userClient.getUser(userId);
 *     }
 * }
 * }</pre>
 *
 * <h2>配置示例</h2>
 * <pre>{@code
 * # application.yml
 * feign:
 *   client:
 *     config:
 *       default:
 *         connectTimeout: 5000      # 连接超时
 *         readTimeout: 10000        # 读超时
 *         loggerLevel: BASIC        # 日志级别
 *   httpclient:
 *     enabled: true                 # 启用 Apache HttpClient
 *     max-connections: 200          # 最大连接数
 *   compression:
 *     request:
 *       enabled: true               # 启用请求压缩
 *     response:
 *       enabled: true               # 启用响应压缩
 * }</pre>
 *
 * <h2>迁移说明</h2>
 * <p>此模块包含从 {@code core/fegin/} 迁移的以下内容：</p>
 * <ul>
 *   <li>Feign 客户端接口定义</li>
 *   <li>Feign 配置类（超时、重试、日志）</li>
 *   <li>请求/响应拦截器</li>
 *   <li>编码/解码器</li>
 *   <li>错误处理器</li>
 *   <li>降级逻辑（Fallback）</li>
 * </ul>
 *
 * <h2>与其他模块的关系</h2>
 * <ul>
 *   <li><b>依赖 core-base</b>: 使用 BizTemplate、异常处理、工具类</li>
 *   <li><b>可选依赖 observability-starter</b>: 日志、指标、链路追踪</li>
 * </ul>
 *
 * <h2>最佳实践</h2>
 * <ul>
 *   <li>合理设置超时：连接超时 3-5s，读超时 10-30s</li>
 *   <li>启用重试：对幂等操作（GET、PUT、DELETE）启用重试</li>
 *   <li>使用断路器：防止服务雪崩，快速失败</li>
 *   <li>日志脱敏：敏感信息（Token、密码）不记录</li>
 *   <li>连接池优化：根据并发量调整连接池大小</li>
 * </ul>
 *
 * @author Angus Infrastructure Team
 * @version 1.0
 * @since 1.0
 */
package cloud.xcan.angus.feign;
