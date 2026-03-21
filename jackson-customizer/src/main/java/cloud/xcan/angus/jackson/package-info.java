/**
 * Jackson Customizer - Jackson JSON 序列化/反序列化定制模块
 *
 * <h2>模块职责</h2>
 * <ul>
 *   <li>提供 Jackson 自定义配置</li>
 *   <li>统一日期时间序列化格式</li>
 *   <li>自定义序列化器/反序列化器</li>
 *   <li>敏感信息脱敏处理</li>
 *   <li>支持多种数据格式（JSON、XML、YAML、CSV）</li>
 * </ul>
 *
 * <h2>核心功能</h2>
 * <ul>
 *   <li><b>日期时间处理</b>: 支持 Java 8 Time、传统 Date、Joda-Time</li>
 *   <li><b>数值处理</b>: 大整数转字符串（防 JS 精度丢失）、BigDecimal 精度控制</li>
 *   <li><b>空值处理</b>: 序列化时忽略/包含 null、Optional 自动解包</li>
 *   <li><b>命名策略</b>: 驼峰、下划线、Pascal Case、Kebab Case</li>
 *   <li><b>敏感信息脱敏</b>: 手机号、身份证、邮箱</li>
 *   <li><b>多格式支持</b>: JSON、XML、YAML、CSV</li>
 * </ul>
 *
 * <h2>依赖关系</h2>
 * <pre>
 * jackson-customizer
 *   ├── core-base (核心基础功能)
 *   ├── jackson-core (Jackson 核心)
 *   ├── jackson-databind (数据绑定)
 *   ├── jackson-annotations (注解)
 *   ├── jackson-datatype-jsr310 (Java 8 时间)
 *   ├── jackson-datatype-jdk8 (JDK 8 特性)
 *   ├── jackson-module-parameter-names (参数名)
 *   ├── jackson-dataformat-xml (XML 支持，可选)
 *   └── spring-boot-starter-json (Spring Boot 集成)
 * </pre>
 *
 * <h2>使用示例</h2>
 * <pre>{@code
 * // 1. 日期时间格式化
 * @Configuration
 * public class JacksonConfig {
 *     @Bean
 *     public Jackson2ObjectMapperBuilderCustomizer customizer() {
 *         return builder -> {
 *             builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
 *             builder.serializers(new LocalDateTimeSerializer(
 *                 DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
 *             ));
 *         };
 *     }
 * }
 *
 * // 2. 大整数转字符串（防 JS 精度丢失）
 * public class User {
 *     @JsonSerialize(using = ToStringSerializer.class)
 *     private Long id;  // 前端收到 "123456789012345678"
 * }
 *
 * // 3. 敏感信息脱敏
 * public class User {
 *     @JsonSerialize(using = MobileSerializer.class)
 *     private String mobile;  // 13812345678 → 138****5678
 * }
 *
 * // 4. 枚举序列化
 * public enum Status {
 *     ACTIVE(1, "启用"),
 *     INACTIVE(0, "禁用");
 *     
 *     @JsonValue  // 序列化时使用 code
 *     private final int code;
 *     private final String desc;
 * }
 * }</pre>
 *
 * <h2>配置示例</h2>
 * <pre>{@code
 * # application.yml
 * spring:
 *   jackson:
 *     date-format: yyyy-MM-dd HH:mm:ss
 *     time-zone: GMT+8
 *     serialization:
 *       write-dates-as-timestamps: false
 *       indent-output: true
 *     deserialization:
 *       fail-on-unknown-properties: false
 *     default-property-inclusion: non_null
 *     property-naming-strategy: SNAKE_CASE
 * }</pre>
 *
 * <h2>迁移说明</h2>
 * <p>此模块包含从 {@code core/jackson/} 迁移的以下内容：</p>
 * <ul>
 *   <li>Jackson 配置类（日期格式、命名策略、null 处理）</li>
 *   <li>自定义序列化器（日期、数值、枚举、脱敏）</li>
 *   <li>自定义反序列化器（日期、字符串 trim）</li>
 *   <li>自定义注解（@JsonMask、@JsonEncrypt）</li>
 *   <li>Jackson 模块注册</li>
 *   <li>JSON 工具类（JsonUtils）</li>
 * </ul>
 *
 * <h2>与其他模块的关系</h2>
 * <ul>
 *   <li><b>依赖 core-base</b>: 使用异常处理、工具类</li>
 *   <li><b>被 feign-integration-starter 依赖</b>: Feign 使用 Jackson 序列化</li>
 *   <li><b>被 remote 依赖</b>: HTTP 响应序列化</li>
 *   <li><b>被 persistence-*-starter 依赖</b>: 数据库 JSON 字段序列化</li>
 * </ul>
 *
 * <h2>最佳实践</h2>
 * <ul>
 *   <li><b>日期统一格式</b>: 前后端约定统一的日期格式（推荐 ISO-8601）</li>
 *   <li><b>大整数转字符串</b>: ID、订单号等大整数转字符串（防 JS 精度丢失）</li>
 *   <li><b>敏感信息脱敏</b>: 手机号、身份证、邮箱等脱敏处理</li>
 *   <li><b>null 值处理</b>: 根据业务约定序列化或忽略 null</li>
 *   <li><b>性能优化</b>: 复用 ObjectMapper 实例，避免重复创建</li>
 *   <li><b>时区一致</b>: 确保前后端时区一致，避免时间偏差</li>
 * </ul>
 *
 * <h2>常见问题</h2>
 * <ul>
 *   <li><b>Q: 前端收到的 Long 类型 ID 精度丢失？</b><br>
 *       A: JavaScript Number 最大安全整数为 2^53-1，使用 @JsonSerialize(using = ToStringSerializer.class)</li>
 *   <li><b>Q: 日期格式不统一？</b><br>
 *       A: 在 application.yml 配置 spring.jackson.date-format 和 time-zone</li>
 *   <li><b>Q: 前端提交的字段后端收不到？</b><br>
 *       A: 检查命名策略，前端 user_name 后端 userName 需要配置 SNAKE_CASE</li>
 * </ul>
 *
 * @author Angus Infrastructure Team
 * @version 1.0
 * @since 1.0
 */
package cloud.xcan.angus.jackson;
