# Jackson Customizer

Jackson JSON 序列化/反序列化定制模块。

## 功能特性

### 1. 日期时间处理
- **Java 8 Time (JSR-310)**: LocalDateTime、LocalDate、Instant
- **传统 Date/Calendar**: Date、Calendar、Timestamp
- **Joda-Time**: DateTime、LocalDate（可选）
- **时区处理**: 自动转换时区、格式化
- **自定义格式**: yyyy-MM-dd HH:mm:ss、ISO-8601

### 2. 数值处理
- **BigDecimal**: 精度控制、科学计数法
- **Long/Integer**: 超大数字转字符串（避免 JS 精度丢失）
- **浮点数**: 小数位控制、四舍五入
- **货币**: 金额格式化（分 → 元）

### 3. 空值与默认值
- **null 处理**: 序列化时忽略 null、包含 null、自定义默认值
- **空集合**: 空数组 [] vs null
- **空字符串**: "" vs null
- **Optional<T>**: 自动解包 Optional

### 4. 命名策略
- **驼峰转下划线**: userName → user_name
- **下划线转驼峰**: user_name → userName
- **Pascal Case**: UserName
- **Kebab Case**: user-name

### 5. 自定义序列化器
- **枚举**: 序列化为 code/name/description
- **敏感信息脱敏**: 手机号、身份证、邮箱
- **Base64 编码**: 二进制数据
- **自定义注解**: @JsonMask、@JsonEncrypt

### 6. 多格式支持
- **JSON**: 标准 JSON 格式
- **XML**: 与 JSON 互转
- **YAML**: 配置文件格式
- **CSV**: 表格数据导入/导出

## 依赖关系

```
jackson-customizer
  ├── core-base (核心基础功能)
  ├── jackson-core (Jackson 核心)
  ├── jackson-databind (数据绑定)
  ├── jackson-annotations (注解)
  ├── jackson-datatype-jsr310 (Java 8 时间)
  ├── jackson-datatype-jdk8 (JDK 8 特性)
  ├── jackson-module-parameter-names (参数名)
  ├── jackson-datatype-joda (Joda-Time，可选)
  ├── jackson-dataformat-xml (XML 支持，可选)
  ├── jackson-dataformat-yaml (YAML 支持，可选)
  ├── jackson-dataformat-csv (CSV 支持，可选)
  ├── spring-boot-starter-json (Spring Boot 集成)
  └── lombok (编译时注解处理)
```

## 使用场景

### 场景 1: 日期时间格式化
```java
@Configuration
public class JacksonConfig {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> {
            // 日期格式
            builder.simpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            // Java 8 时间格式
            builder.serializers(new LocalDateTimeSerializer(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            ));
        };
    }
}
```

### 场景 2: 大整数转字符串（防 JS 精度丢失）
```java
@JsonSerialize(using = ToStringSerializer.class)
private Long id;  // 前端收到 "123456789012345678"
```

### 场景 3: 敏感信息脱敏
```java
public class MobileSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) {
        gen.writeString(value.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
    }
}

@JsonSerialize(using = MobileSerializer.class)
private String mobile;  // 13812345678 → 138****5678
```

### 场景 4: 枚举序列化
```java
public enum Status {
    ACTIVE(1, "启用"),
    INACTIVE(0, "禁用");
    
    @JsonValue  // 序列化时使用 code
    private final int code;
    private final String desc;
}
```

### 场景 5: 动态字段过滤
```java
@JsonFilter("userFilter")
public class User {
    private Long id;
    private String username;
    private String password;  // 敏感字段，选择性序列化
}

// 使用过滤器
ObjectMapper mapper = new ObjectMapper();
FilterProvider filters = new SimpleFilterProvider()
    .addFilter("userFilter", SimpleBeanPropertyFilter.serializeAllExcept("password"));
String json = mapper.writer(filters).writeValueAsString(user);
```

## 配置示例

### application.yml
```yaml
spring:
  jackson:
    # 日期格式
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    
    # 序列化配置
    serialization:
      write-dates-as-timestamps: false     # 日期不序列化为时间戳
      write-null-map-values: false         # Map 的 null 值不序列化
      indent-output: true                  # 格式化输出（开发环境）
      fail-on-empty-beans: false           # 空 Bean 不报错
      
    # 反序列化配置
    deserialization:
      fail-on-unknown-properties: false    # 忽略未知属性
      accept-empty-string-as-null-object: true  # 空字符串视为 null
      
    # 默认属性包含策略
    default-property-inclusion: non_null   # 仅序列化非 null 字段
    
    # 命名策略
    property-naming-strategy: SNAKE_CASE   # 驼峰转下划线
```

## 包结构

```
cloud.xcan.angus.jackson/
  ├── config/               # Jackson 配置类
  │   ├── JacksonConfig.java
  │   └── JacksonCustomizer.java
  ├── serializer/           # 自定义序列化器
  │   ├── DateSerializer.java
  │   ├── BigDecimalSerializer.java
  │   ├── LongToStringSerializer.java
  │   ├── MobileSerializer.java (脱敏)
  │   └── EmailSerializer.java (脱敏)
  ├── deserializer/         # 自定义反序列化器
  │   ├── DateDeserializer.java
  │   └── TrimStringDeserializer.java
  ├── annotation/           # 自定义注解
  │   ├── JsonMask.java     # 脱敏注解
  │   └── JsonEncrypt.java  # 加密注解
  ├── module/               # Jackson 模块
  │   └── AngusJacksonModule.java
  └── util/                 # 工具类
      └── JsonUtils.java
```

## 迁移内容

此模块包含从 `core/jackson/` 迁移的以下内容：
- Jackson 配置类
- 自定义序列化器/反序列化器
- 自定义注解（@JsonMask、@JsonEncrypt）
- Jackson 模块注册
- JSON 工具类

## 与其他模块的关系

- **依赖 core-base**: 使用异常处理、工具类
- **被 feign-integration-starter 依赖**: Feign 使用 Jackson 序列化
- **被 remote 依赖**: HTTP 响应序列化
- **被 persistence-*-starter 依赖**: 数据库 JSON 字段序列化

## 最佳实践

1. **日期统一格式**: 前后端约定统一的日期格式
2. **大整数转字符串**: ID、订单号等大整数转字符串
3. **敏感信息脱敏**: 手机号、身份证、邮箱等脱敏处理
4. **null 值处理**: 根据业务约定序列化或忽略 null
5. **性能优化**: 复用 ObjectMapper 实例，避免重复创建

## 常见问题

### Q: 前端收到的 Long 类型 ID 精度丢失？
**A**: JavaScript Number 最大安全整数为 2^53-1，使用 `@JsonSerialize(using = ToStringSerializer.class)` 转为字符串。

### Q: 日期格式不统一？
**A**: 在 `application.yml` 配置 `spring.jackson.date-format` 和 `time-zone`。

### Q: 前端提交的字段后端收不到？
**A**: 检查命名策略，前端 `user_name` 后端 `userName` 需要配置 `SNAKE_CASE`。

### Q: 敏感信息如何脱敏？
**A**: 使用自定义序列化器 + `@JsonSerialize` 注解。

## 注意事项

- ⚠️ **全局配置影响所有接口**: 修改 Jackson 配置会影响所有 REST API
- ⚠️ **性能开销**: 自定义序列化器会增加序列化耗时
- ⚠️ **向后兼容性**: 修改序列化规则可能导致前端无法解析
- ⚠️ **时区问题**: 确保前后端时区一致，避免时间偏差
