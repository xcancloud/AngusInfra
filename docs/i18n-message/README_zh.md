# 数据库配置数据国际化（I18n Message）

[English](README.md) | [中文](README_zh.md)

将应用**配置类文案**（字典标签、内置角色名、菜单标题、安全提示等）持久化到数据库，通过**稳定
messageKey** 解析，并使用可过期的**进程内 Caffeine 缓存**加速读取。不依赖 Redis。

> **不适合：** 固定枚举/状态码（请用 classpath `MessageSource` / `messages_*.properties`）；
> 用户生成的多语言内容（请用实体翻译附表或 JSON 列）。

---

## 目录

1. [概述](#1-概述)
2. [架构](#2-架构)
3. [数据模型](#3-数据模型)
4. [配置项](#4-配置项)
5. [接入指南](#5-接入指南)
6. [API 参考](#6-api-参考)
7. [缓存与失效](#7-缓存与失效)
8. [Locale 回退](#8-locale-回退)
9. [Actuator 端点](#9-actuator-端点)
10. [从旧版 default_message 查找迁移](#10-从旧版-default_message-查找迁移)
11. [最佳实践](#11-最佳实践)
12. [排查指南](#12-排查指南)

---

## 1. 概述

| 组件 | 职责 |
|------|------|
| `gm_i18n_messages` | 配置文案持久化表 |
| `I18nMessageResolver` | 主 API：单条/批量解析、缓存失效 |
| `I18nMessageCache` | 本地 Caffeine 可过期缓存（无 Redis） |
| `@MessageJoin` + `@MessageJoinField` | 可选 AOP：出参 VO 字段自动填充 |
| Actuator `messages` | 运维：查看/清空缓存 |

**设计原则**

1. **稳定 Key** — 业务侧存 `message_key`（如 `ROLE_ADMIN`），禁止用中文展示文案当查找主键。
2. **DB 持久化** — 可运营编辑的配置文案全部落在 `gm_i18n_messages`。
3. **本地可过期缓存** — 仅 Caffeine；多实例靠 TTL + 显式/接口失效达到最终一致。
4. **Locale 回退** — `zh_CN` → `zh` → 配置的默认 Locale → fallback / 原始 key。
5. **分层国际化** — 枚举走 classpath；配置文案走本模块；实体内容另建模型。

---

## 2. 架构

```
┌──────────────────┐     @MessageJoin（可选）        ┌─────────────────────┐
│  Facade / Query  │ ───────────────────────────────▶│ I18nMessageAspect   │
└────────┬─────────┘                                 └──────────┬──────────┘
         │                                                      │
         │  注入 / 直接调用                                     ▼
         │                                           ┌─────────────────────┐
         └──────────────────────────────────────────▶│ I18nMessageResolver │
                                                     └──────────┬──────────┘
                                                                │
                                              ┌─────────────────┼─────────────────┐
                                              ▼                                   ▼
                                   ┌────────────────────┐              ┌────────────────────┐
                                   │ I18nMessageCache   │              │ I18nMessageJoinRepo│
                                   │ (Caffeine + TTL)   │── miss ─────▶│ (JPA / DB)         │
                                   └────────────────────┘              └────────────────────┘
```

**读路径**

1. 解析 `(type, messageKey, locale)`。
2. 构造 locale 候选列表（见 [§8](#8-locale-回退)）。
3. 逐个候选：Caffeine 命中则返回；未命中则从 DB 加载（开启缓存时按 `type` 全量加载）→ 回填缓存 → 返回。
4. 均未命中 → 返回调用方 `fallback`，否则返回原始 `messageKey`。

---

## 3. 数据模型

### 3.1 表 `gm_i18n_messages`

| 列 | 类型 | 必填 | 说明 |
|----|------|------|------|
| `id` | BIGINT | 是 | 主键 |
| `type` | VARCHAR(50) | 是 | 分类，如 `ROLE`、`MENU`、`DICT_GENDER` |
| `language` | VARCHAR(20) | 是 | 与 `SupportedLanguage` 对齐：`zh_CN`、`en` |
| `message_key` | VARCHAR(100) | 是* | 稳定键，如 `ROLE_ADMIN` |
| `default_message` | MEDIUMTEXT | 否 | 可选默认语言展示文案（最长约 6 万字符） |
| `i18n_message` | MEDIUMTEXT | 是 | 对应 `language` 的译文（最长约 6 万字符） |
| `private0` | INT | 否 | 既有部署标志列（有则保留） |

\* 新数据必填。历史行可暂时为空；运行时会把 `default_message` 当作 key，直到回填完成。

**唯一索引：** `(type, language, message_key)`

```sql
ALTER TABLE `gm_i18n_messages`
  ADD COLUMN `message_key` varchar(100) NULL COMMENT '稳定消息键' AFTER `language`;

UPDATE `gm_i18n_messages`
SET `message_key` = `default_message`
WHERE `message_key` IS NULL OR `message_key` = '';

ALTER TABLE `gm_i18n_messages`
  MODIFY COLUMN `message_key` varchar(100) NOT NULL COMMENT '稳定消息键',
  MODIFY COLUMN `default_message` mediumtext NULL COMMENT '默认语言展示文案(最长约6万字符)',
  MODIFY COLUMN `i18n_message` mediumtext NOT NULL COMMENT '国际化消息(最长约6万字符)';

-- 如存在旧唯一键则先删除，再执行：
ALTER TABLE `gm_i18n_messages`
  ADD UNIQUE KEY `uidx_type_language_message_key` (`type`, `language`, `message_key`);
```

### 3.2 实体约定

实现 `cloud.xcan.angus.core.biz.I18nMessage`，并提供继承 `I18nMessageJoinRepository<YourEntity>`
的 Spring Data 仓库。

```java
public interface I18nMessage {
  String getType();
  String getLanguage();
  /** 稳定键；历史数据可回退到 defaultMessage */
  String getMessageKey();
  String getDefaultMessage();
  String getI18nMessage();
}
```

---

## 4. 配置项

前缀：`angus.i18n.message`

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `enabled` | `true` | Resolver / Aspect 总开关 |
| `default-locale` | `zh_CN` | 应用默认语言（`SupportedLanguage` 枚举名） |
| `skip-default-locale` | `true` | 为 `true` 时，请求 Locale 等于默认语言则 `@MessageJoin` 跳过（VO 已是默认文案）。若 VO 存的是 key、任何语言都要解析，设为 `false` |
| `cache.maximum-size` | `2048` | Caffeine 最大条目数（按 `type`） |
| `cache.expire-after-write-minutes` | `30` | 写入后过期 |
| `cache.expire-after-access-minutes` | `10` | 访问后过期 |
| `cache.enabled` | `true` | 全局类型级缓存；字段注解仍可按字段关闭 |

```yaml
angus:
  i18n:
    message:
      enabled: true
      default-locale: zh_CN
      skip-default-locale: true
      cache:
        enabled: true
        maximum-size: 2048
        expire-after-write-minutes: 30
        expire-after-access-minutes: 10
```

---

## 5. 接入指南

### 5.1 依赖

已使用 `xcan-angusinfra.web-starter` 的应用会自动装配。

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.web-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

### 5.2 提供仓库 Bean

```java
@Repository("commonI18nMessageRepo")
public interface I18nMessageRepo
    extends I18nMessageJoinRepository<I18nMessages>, BaseRepository<I18nMessages, Long> {
}
```

Spring Data 会派生：

- `findByType(String type)`
- `findByTypeAndLanguageAndMessageKeyIn(String type, String language, Collection<String> keys)`
- `findByTypeAndLanguageAndDefaultMessageIn(...)` — 兼容旧数据

### 5.3 推荐：直接调用 `I18nMessageResolver`

```java
@Resource
private I18nMessageResolver i18nMessageResolver;

public RoleVo toVo(Role role) {
  RoleVo vo = new RoleVo();
  vo.setCode(role.getCode());
  vo.setName(i18nMessageResolver.resolve("ROLE", role.getCode(), role.getName()));
  return vo;
}
```

批量：

```java
Map<String, String> names = i18nMessageResolver.resolveBatch(
    "ROLE", Set.of("ROLE_ADMIN", "ROLE_USER"), SdfLocaleHolder.getLocale());
```

### 5.4 可选：`@MessageJoin` 装配 VO

**VO** — 被注解字段会被覆写为译文；key 来自 `keyField`（为空则用该字段自身当前值）：

```java
@Data
public class RoleVo {
  private String code;

  /** 稳定 key 来源 */
  private String nameKey;

  @MessageJoinField(type = "ROLE", keyField = "nameKey")
  private String name;
}
```

**Facade**

```java
@MessageJoin
public PageResult<RoleVo> list(RoleFindDto dto) {
  // 组装 VO 时设置 nameKey；name 可为默认语言文案或空
  return buildVoPageResult(page, RoleAssembler::toVo);
}
```

规则：

- 没有 `@MessageJoinField` → Aspect **静默跳过**（不再抛异常）。
- 字段 `type` 为空 → 仍抛异常（配置错误）。
- 扫描含父类字段（`getAllFields`）。
- **异构嵌套 VO**（如 `UserCurrentDetailVo.accessApp` / `authApps` / `accessAppFuncTree`）：递归收集各具体类型实例，按类型分别填充。
- **树形 VO**（如菜单 `children`）：同上递归收集，子节点同样生效。
- 若 VO 含 `permission.menuName`，在填充 `name` 时会同步更新该字段。
- 跳过 JDK / Jakarta 类型，并用对象身份去重，避免环引用。

### 5.5 种子数据示例

```sql
INSERT INTO gm_i18n_messages (id, type, language, message_key, default_message, i18n_message, private0)
VALUES
  (1, 'ROLE', 'zh_CN', 'ROLE_ADMIN', '管理员', '管理员', 0),
  (2, 'ROLE', 'en',    'ROLE_ADMIN', '管理员', 'Administrator', 0),
  (3, 'ROLE', 'zh_CN', 'ROLE_USER',  '普通用户', '普通用户', 0),
  (4, 'ROLE', 'en',    'ROLE_USER',  '普通用户', 'User', 0);
```

### 5.6 写入/更新文案后

```java
i18nMessageResolver.evict("ROLE");   // 按 type
i18nMessageResolver.evictAll();      // 全部
// 或调用 Actuator（见 §9）
```

---

## 6. API 参考

### `I18nMessageResolver`

| 方法 | 说明 |
|------|------|
| `resolve(type, messageKey)` | 使用当前 `SdfLocaleHolder` Locale |
| `resolve(type, messageKey, fallback)` | 同上，带 fallback |
| `resolve(type, messageKey, locale)` | 指定 Locale |
| `resolve(type, messageKey, locale, fallback)` | 完整形式 |
| `resolveBatch(type, keys, locale)` | 批量 `key → text` |
| `evict(type)` / `evictAll()` | 失效本地缓存 |

### 注解

| 注解 | 作用目标 | 说明 |
|------|----------|------|
| `@MessageJoin` | 方法 | 启用返回 VO 的消息填充 |
| `@MessageJoinField(type, keyField="", enabledCache=true)` | 字段 | 标记待填充字段；`keyField` 指定 key 属性名 |

---

## 7. 缓存与失效

- **介质：** 仅进程内 Caffeine（无 Redis / 无 L2）。
- **缓存键：** `type` → 嵌套 `language → (messageKey → I18nMessage)`。
- **加载策略：** 开启缓存且未命中时，`findByType(type)` 全量加载该 type。
- **TTL：** `expireAfterWrite` + `expireAfterAccess`（可配置）。
- **一致性：** 多实例依赖 TTL 或显式 `evict*`；管理端 CRUD 成功后应在本服务调用 `evict`。

---

## 8. Locale 回退

对 Locale `zh_CN` 的候选顺序：

1. 精确标签：`zh_CN`
2. 仅语言：`zh`（若与上不同）
3. 配置项 `angus.i18n.message.default-locale`（如 `zh_CN`）
4. 调用方 `fallback`，否则原始 `messageKey`

当前 `SupportedLanguage`：`zh_CN`、`en`。入库的 `language` 建议与枚举名完全一致。

---

## 9. Actuator 端点

| 操作 | HTTP | 说明 |
|------|------|------|
| 查看缓存快照 | `GET /actuator/messages?type=ROLE` | `type` 可选 |
| 清空缓存 | `POST /actuator/messages` | 清空全部内存条目 |

需在 `management.endpoints.web.exposure.include` 中包含 `messages`。

---

## 10. 从旧版 default_message 查找迁移

| 步骤 | 动作 |
|------|------|
| 1 | 增加 `message_key` 列；回填 `message_key = default_message` |
| 2 | 停止仅用中文展示文案标识翻译；分配稳定编码（`ROLE_ADMIN` 等） |
| 3 | VO / Assembler 改为传 key；`default_message` 可保留作默认语言展示 |
| 4 | 新代码优先 `I18nMessageResolver`；需要时再保留 `@MessageJoin` |
| 5 | 固定枚举迁出 DB，改用 classpath i18n |

运行时仍兼容历史行：`message_key` 为空时 `getMessageKey()` 回退到 `default_message`。
查找优先 `message_key`，未命中再尝试 `default_message`。

---

## 11. 最佳实践

1. **禁止**用可变展示文案作为翻译主键。
2. `type` 保持粗粒度且稳定（`ROLE`、`MENU`、`DICT_*`），勿按租户动态拆分。
3. Assembler 中优先 Resolver（易单测）；薄 Facade 可用 `@MessageJoin`。
4. 文案写接口成功后务必 `evict`。
5. 高基数用户内容不要写入 `gm_i18n_messages`。
6. `language` 与 `SupportedLanguage` 枚举名对齐（`zh_CN`、`en`）。

---

## 12. 排查指南

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 一直返回 key / 中文 | 无对应 locale 行；`type`/`message_key` 错误 | 查表与唯一索引 |
| 更新后仍旧文案 | 本地缓存未失效 | 调用 `evict(type)` 或 Actuator |
| Aspect 无效果 | 无 `@MessageJoinField` / 默认语言被跳过 | 补注解或设 `skip-default-locale=false` |
| `type is empty` | `@MessageJoinField` 未设 `type` | 补全 `type` |
| Repository NPE | 未注册实现 Bean | 注册 `I18nMessageJoinRepository` 实现 |

---

## 相关类

| 类 | 模块 |
|----|------|
| `cloud.xcan.angus.core.biz.I18nMessage` | `xcan-angusinfra.core` |
| `cloud.xcan.angus.core.biz.I18nMessageResolver` | `xcan-angusinfra.core` |
| `cloud.xcan.angus.core.biz.I18nMessageAspect` | `xcan-angusinfra.core` |
| `cloud.xcan.angus.core.biz.MessageJoin` | `xcan-angusinfra.core` |
| `cloud.xcan.angus.remote.MessageJoinField` | `xcan-angusinfra.remote` |
| `cloud.xcan.angus.web.endpoint.MessageEndpoint` | `xcan-angusinfra.web-starter` |
