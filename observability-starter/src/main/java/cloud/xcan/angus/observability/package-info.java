/**
 * Observability Starter - 可观测性模块（日志、指标、链路追踪、数据导出）
 *
 * <h2>模块职责</h2>
 * <ul>
 *   <li>提供结构化日志管理</li>
 *   <li>提供指标收集与监控</li>
 *   <li>提供分布式链路追踪</li>
 *   <li>提供数据导出功能（Excel、CSV、PDF）</li>
 *   <li>提供健康检查端点</li>
 * </ul>
 *
 * <h2>核心功能</h2>
 *
 * <h3>1. 日志管理 (Logging)</h3>
 * <ul>
 *   <li><b>结构化日志</b>: Logstash JSON 格式</li>
 *   <li><b>MDC 增强</b>: TraceId、UserId、RequestId</li>
 *   <li><b>日志聚合</b>: Logstash、Fluentd、Elasticsearch</li>
 *   <li><b>日志脱敏</b>: 敏感信息自动脱敏</li>
 *   <li><b>动态调整</b>: 运行时修改日志级别</li>
 * </ul>
 *
 * <h3>2. 指标监控 (Metrics)</h3>
 * <ul>
 *   <li><b>Micrometer 指标</b>: Counter、Gauge、Timer、Distribution Summary</li>
 *   <li><b>JVM 指标</b>: 内存、线程、CPU、GC</li>
 *   <li><b>业务指标</b>: 自定义业务相关指标</li>
 *   <li><b>Prometheus 导出</b>: 兼容 Prometheus 采集</li>
 *   <li><b>多维度聚合</b>: 标签（Tags）支持</li>
 * </ul>
 *
 * <h3>3. 链路追踪 (Tracing)</h3>
 * <ul>
 *   <li><b>OpenTelemetry</b>: 统一的可观测性标准</li>
 *   <li><b>Zipkin</b>: 轻量级链路追踪</li>
 *   <li><b>自动埋点</b>: HTTP、数据库、缓存、MQ</li>
 *   <li><b>TraceId 传递</b>: HTTP Header、MQ、RPC</li>
 *   <li><b>Span 管理</b>: 手动创建 Span、添加标签</li>
 * </ul>
 *
 * <h3>4. 数据导出 (Export)</h3>
 * <ul>
 *   <li><b>Excel 导出</b>: Apache POI，支持 .xlsx 格式</li>
 *   <li><b>CSV 导出</b>: OpenCSV，高性能读写</li>
 *   <li><b>PDF 导出</b>: iText（可选）</li>
 *   <li><b>流式写入</b>: 大数据量导出，避免 OOM</li>
 * </ul>
 *
 * <h3>5. 健康检查 (Health Check)</h3>
 * <ul>
 *   <li><b>/actuator/health</b>: 健康状态</li>
 *   <li><b>/actuator/metrics</b>: 指标查询</li>
 *   <li><b>/actuator/prometheus</b>: Prometheus 格式指标</li>
 *   <li><b>自定义健康检查</b>: 数据库、Redis、磁盘、依赖服务</li>
 * </ul>
 *
 * <h2>依赖关系</h2>
 * <pre>
 * observability-starter
 *   ├── core-base (核心基础功能)
 *   ├── spring-boot-starter-actuator (健康检查、指标暴露)
 *   ├── micrometer-core (指标收集)
 *   ├── micrometer-registry-prometheus (Prometheus 导出)
 *   ├── micrometer-tracing (链路追踪)
 *   ├── logback-classic (日志框架)
 *   ├── logstash-logback-encoder (结构化日志)
 *   ├── poi-ooxml (Excel 导出)
 *   ├── opencsv (CSV 导出，可选)
 *   └── itextpdf (PDF 导出，可选)
 * </pre>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 1. 结构化日志
 * @Slf4j
 * @RestController
 * public class UserController {
 *     @GetMapping("/users/{id}")
 *     public User getUser(@PathVariable Long id) {
 *         MDC.put("userId", String.valueOf(id));
 *         log.info("Fetching user, userId={}", id);
 *         // ...
 *         MDC.clear();
 *     }
 * }
 *
 * // 2. 自定义指标
 * @Service
 * public class OrderService {
 *     @Autowired
 *     private MeterRegistry meterRegistry;
 *     
 *     public void createOrder(Order order) {
 *         // 计数器
 *         meterRegistry.counter("orders.created").increment();
 *         
 *         // 计时器
 *         Timer.Sample sample = Timer.start(meterRegistry);
 *         processOrder(order);
 *         sample.stop(meterRegistry.timer("orders.process.time"));
 *     }
 * }
 *
 * // 3. 链路追踪
 * @Service
 * public class PaymentService {
 *     @Autowired
 *     private Tracer tracer;
 *     
 *     public void processPayment(Payment payment) {
 *         Span span = tracer.nextSpan().name("processPayment").start();
 *         try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
 *             span.tag("paymentId", payment.getId().toString());
 *             // 处理支付逻辑
 *         } finally {
 *             span.end();
 *         }
 *     }
 * }
 *
 * // 4. Excel 导出
 * @Service
 * public class ExportService {
 *     public void exportUsers(HttpServletResponse response) {
 *         response.setContentType("application/vnd.ms-excel");
 *         try (Workbook workbook = new XSSFWorkbook()) {
 *             Sheet sheet = workbook.createSheet("Users");
 *             // 写入数据...
 *             workbook.write(response.getOutputStream());
 *         }
 *     }
 * }
 * }</pre>
 *
 * <h2>配置示例</h2>
 * <pre>{@code
 * # application.yml
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health,metrics,prometheus
 *   metrics:
 *     export:
 *       prometheus:
 *         enabled: true
 *   tracing:
 *     sampling:
 *       probability: 1.0  # 采样率
 * 
 * logging:
 *   level:
 *     root: INFO
 *     cloud.xcan.angus: DEBUG
 * }</pre>
 *
 * <h2>迁移说明</h2>
 * <p>此模块包含从 {@code core/} 迁移的以下内容：</p>
 * <ul>
 *   <li>{@code log/} - 日志配置、日志过滤器、日志脱敏</li>
 *   <li>{@code meter/} - Micrometer 指标、自定义指标</li>
 *   <li>{@code export/} - Excel、CSV、PDF 导出</li>
 * </ul>
 *
 * <h2>与其他模块的关系</h2>
 * <ul>
 *   <li><b>依赖 core-base</b>: 使用 BizTemplate、异常处理</li>
 *   <li><b>被所有模块依赖</b>: 日志、指标、链路追踪是基础设施</li>
 * </ul>
 *
 * <h2>最佳实践</h2>
 * <ul>
 *   <li><b>日志脱敏</b>: 敏感信息（密码、Token、身份证）不记录</li>
 *   <li><b>指标命名</b>: 使用小写、点分隔（orders.created）</li>
 *   <li><b>采样率</b>: 生产环境建议 10-50%，避免性能开销</li>
 *   <li><b>日志级别</b>: 生产环境 INFO，开发环境 DEBUG</li>
 *   <li><b>大数据导出</b>: 使用流式写入，避免 OOM</li>
 * </ul>
 *
 * <h2>关键指标</h2>
 * <ul>
 *   <li><b>请求量 (QPS)</b>: {@code http.server.requests}</li>
 *   <li><b>响应时间</b>: {@code http.server.requests.percentile}</li>
 *   <li><b>错误率</b>: {@code http.server.requests\{status=~"5.."\}}</li>
 *   <li><b>JVM 内存</b>: {@code jvm.memory.used}</li>
 *   <li><b>GC 时间</b>: {@code jvm.gc.pause}</li>
 * </ul>
 *
 * @author Angus Infrastructure Team
 * @version 1.0
 * @since 1.0
 */
package cloud.xcan.angus.observability;
