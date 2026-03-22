# Observability Starter

可观测性模块，提供日志、指标、链路追踪和数据导出功能。

## 功能特性

### 1. 日志管理 (Logging)

#### 1.1 结构化日志

- **Logback**: 高性能日志框架
- **Logstash Encoder**: JSON 格式日志
- **MDC (Mapped Diagnostic Context)**: TraceId、UserId、RequestId
- **日志级别动态调整**: 运行时修改日志级别

#### 1.2 日志聚合

- **文件输出**: 按日期、大小滚动
- **控制台输出**: 彩色日志、格式化
- **远程输出**: Logstash、Fluentd、Elasticsearch
- **日志脱敏**: 敏感信息自动脱敏

### 2. 指标监控 (Metrics)

#### 2.1 Micrometer 指标

- **Counter**: 计数器（请求次数、错误次数）
- **Gauge**: 仪表盘（当前活跃连接、内存使用）
- **Timer**: 计时器（接口响应时间）
- **Distribution Summary**: 分布汇总（请求大小分布）

#### 2.2 JVM 指标

- **内存**: heap、non-heap、GC 统计
- **线程**: 活跃线程、死锁检测
- **CPU**: CPU 使用率、负载
- **类加载**: 已加载类数量

#### 2.3 业务指标

- **自定义指标**: 业务相关指标（订单量、用户数）
- **标签（Tags）**: 多维度聚合（regioncode、环境、版本）
- **百分位统计**: P50、P95、P99

### 3. 链路追踪 (Tracing)

#### 3.1 分布式追踪

- **OpenTelemetry**: 统一的可观测性标准
- **Zipkin**: 轻量级链路追踪
- **Jaeger**: 高级链路分析（可选）
- **TraceId 传递**: HTTP Header、MQ、RPC

#### 3.2 Span 管理

- **自动埋点**: HTTP 请求、数据库查询、缓存操作
- **手动埋点**: 自定义 Span
- **Span 标签**: 添加业务上下文
- **异常记录**: 异常堆栈自动记录

### 4. 数据导出 (Export)

#### 4.1 Excel 导出

- **Apache POI**: 支持 .xlsx 格式
- **大数据量**: 流式写入，避免 OOM
- **样式定制**: 标题、格式、合并单元格
- **模板导出**: 基于模板填充数据

#### 4.2 CSV 导出

- **OpenCSV**: 高性能 CSV 读写
- **字符编码**: UTF-8、GBK
- **批量导出**: 分批查询、分批写入

#### 4.3 PDF 导出

- **iText**: PDF 生成（可选）
- **中文支持**: 字体嵌入
- **复杂布局**: 表格、图片、水印

### 5. 健康检查 (Health Check)

#### 5.1 Actuator 端点

- **/actuator/health**: 健康状态
- **/actuator/metrics**: 指标查询
- **/actuator/prometheus**: Prometheus 格式指标
- **/actuator/info**: 应用信息

#### 5.2 自定义健康检查

- **数据库健康检查**: 检查数据库连接
- **Redis 健康检查**: 检查 Redis 可用性
- **磁盘空间检查**: 检查磁盘剩余空间
- **依赖服务检查**: 检查下游服务

## 依赖关系

```
observability-starter
  ├── core-base (核心基础功能)
  ├── spring-boot-starter-actuator (健康检查、指标暴露)
  ├── micrometer-core (指标收集)
  ├── micrometer-registry-prometheus (Prometheus 导出)
  ├── micrometer-tracing (链路追踪)
  ├── micrometer-tracing-bridge-otel (OpenTelemetry，可选)
  ├── opentelemetry-exporter-otlp (OTLP 导出，可选)
  ├── zipkin-reporter-brave (Zipkin 链路追踪，可选)
  ├── logback-classic (日志框架)
  ├── logstash-logback-encoder (结构化日志)
  ├── poi-ooxml (Excel 导出)
  ├── opencsv (CSV 导出，可选)
  └── itextpdf (PDF 导出，可选)
```

## 使用场景

### 场景 1: 结构化日志

```java

@Slf4j
@RestController
public class UserController {

  @GetMapping("/users/{id}")
  public User getUser(@PathVariable Long id) {
    MDC.put("userId", String.valueOf(id));
    log.info("Fetching user, userId={}", id);
    // ...
    MDC.clear();
  }
}
```

### 场景 2: 自定义指标

```java
@Service
public class OrderService {
    private final MeterRegistry meterRegistry;
    
    public void createOrder(Order order) {
        // 计数器：订单创建次数
        meterRegistry.counter("orders.created", "region", order.getRegion()).increment();
        
        // 仪表盘：当前待处理订单数
        meterRegistry.gauge("orders.pending", getPendingOrderCount());
        
        // 计时器：订单处理耗时
        Timer.Sample sample = Timer.start(meterRegistry);
        processOrder(order);
        sample.stop(meterRegistry.timer("orders.process.time"));
    }
}
```

### 场景 3: 链路追踪

```java
@Service
public class PaymentService {
    @Autowired
    private Tracer tracer;
    
    public void processPayment(Payment payment) {
        Span span = tracer.nextSpan().name("processPayment").start();
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("paymentId", payment.getId().toString());
            span.tag("amount", payment.getAmount().toString());
            // 处理支付逻辑
        } finally {
            span.end();
        }
    }
}
```

### 场景 4: Excel 导出

```java
@Service
public class ExportService {
    public void exportUsers(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=users.xlsx");
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Users");
            
            // 标题行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Name");
            
            // 数据行
            List<User> users = userRepository.findAll();
            for (int i = 0; i < users.size(); i++) {
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(users.get(i).getId());
                row.createCell(1).setCellValue(users.get(i).getName());
            }
            
            workbook.write(response.getOutputStream());
        }
    }
}
```

## 配置示例

### application.yml

```yaml
# Actuator 端点配置
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info  # 暴露的端点
      base-path: /actuator                       # 端点基础路径
  endpoint:
    health:
      show-details: always                       # 显示详细健康信息
    metrics:
      enabled: true
  metrics:
    tags:
      application: ${spring.application.name}    # 全局标签
      environment: ${spring.profiles.active}
    export:
      prometheus:
        enabled: true                            # 启用 Prometheus 导出
        step: 10s                                # 采集间隔
  tracing:
    sampling:
      probability: 1.0                           # 采样率（1.0 = 100%）

# 日志配置
logging:
  level:
    root: INFO
    cloud.xcan.angus: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 100MB
    max-history: 30
```

### logback-spring.xml

```xml

<configuration>
  <appender name="JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/app-json.log</file>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <includeMdcKeyName>traceId</includeMdcKeyName>
      <includeMdcKeyName>userId</includeMdcKeyName>
    </encoder>
  </appender>
</configuration>
```

## 包结构

```
cloud.xcan.angus.observability/
  ├── log/                  # 日志管理
  │   ├── config/           # 日志配置
  │   ├── filter/           # 日志过滤器
  │   ├── appender/         # 日志输出器
  │   └── masking/          # 日志脱敏
  ├── meter/                # 指标监控
  │   ├── config/           # Micrometer 配置
  │   ├── custom/           # 自定义指标
  │   └── annotation/       # 指标注解
  ├── tracing/              # 链路追踪
  │   ├── config/           # 追踪配置
  │   ├── filter/           # TraceId 传递
  │   └── annotation/       # 追踪注解
  └── export/               # 数据导出
      ├── excel/            # Excel 导出
      ├── csv/              # CSV 导出
      └── pdf/              # PDF 导出
```

## 迁移内容

此模块包含从 `core/` 迁移的以下内容：

- `log/` - 日志配置、日志过滤器、日志脱敏
- `meter/` - Micrometer 指标、自定义指标
- `export/` - Excel、CSV、PDF 导出

## 与其他模块的关系

- **依赖 core-base**: 使用 BizTemplate、异常处理
- **被所有模块依赖**: 日志、指标、链路追踪是基础设施

## 最佳实践

1. **日志脱敏**: 敏感信息（密码、Token、身份证）不记录
2. **指标命名**: 使用小写、点分隔（orders.created）
3. **采样率**: 生产环境建议 10%，避免性能开销
4. **日志级别**: 生产环境 INFO，开发环境 DEBUG
5. **大数据导出**: 使用流式写入，避免 OOM

## 监控指标

### 关键指标

- **请求量 (QPS)**: `http.server.requests`
- **响应时间**: `http.server.requests.percentile`
- **错误率**: `http.server.requests{status=~"5.."}"`
- **JVM 内存**: `jvm.memory.used`
- **GC 时间**: `jvm.gc.pause`

### Grafana 仪表盘

- 使用 Prometheus + Grafana 可视化
- 导入社区仪表盘（Spring Boot 2.x Dashboard）

## 注意事项

- ⚠️ **性能开销**: 链路追踪和指标收集会增加 5-10% 性能开销
- ⚠️ **存储成本**: 日志和追踪数据需要大量存储空间
- ⚠️ **采样率**: 生产环境建议降低采样率（10-50%）
- ⚠️ **日志脱敏**: 避免记录敏感信息
