# Database Config Data i18n (I18n Message)

[English](README.md) | [中文](README_zh.md)

Persist application **configuration copy** (dictionary labels, built-in role names, menu titles,
security tips, etc.) in the database, resolve them by a **stable message key**, and serve them
through an expireable **in-memory Caffeine cache**. No Redis is required.

> **Not for:** fixed enums/status codes (prefer classpath `MessageSource` / `messages_*.properties`)
> or user-generated multilingual content (prefer entity translation tables / JSON columns).

---

## Table of Contents

1. [Overview](#1-overview)
2. [Architecture](#2-architecture)
3. [Data Model](#3-data-model)
4. [Configuration](#4-configuration)
5. [Integration Guide](#5-integration-guide)
6. [API Reference](#6-api-reference)
7. [Caching & Invalidation](#7-caching--invalidation)
8. [Locale Fallback](#8-locale-fallback)
9. [Actuator Endpoint](#9-actuator-endpoint)
10. [Migration from Legacy default_message Lookup](#10-migration-from-legacy-default_message-lookup)
11. [Best Practices](#11-best-practices)
12. [Troubleshooting](#12-troubleshooting)

---

## 1. Overview

| Piece | Responsibility |
|-------|----------------|
| `gm_i18n_messages` | Persistent store for translated config copy |
| `I18nMessageResolver` | Primary API: resolve / batch-resolve / evict |
| `I18nMessageCache` | Local Caffeine cache with TTL (no Redis) |
| `@MessageJoin` + `@MessageJoinField` | Optional AOP assembly that fills VO fields via the resolver |
| Actuator `messages` | Ops: inspect / clear cache |

**Design principles**

1. **Stable key** — business code stores `message_key` (e.g. `ROLE_ADMIN`), never Chinese text as the lookup key.
2. **DB persistence** — all editable config copy lives in `gm_i18n_messages`.
3. **Local expireable cache** — Caffeine only; multi-instance eventual consistency via TTL + manual/API eviction.
4. **Locale fallback** — `zh_CN` → `zh` → configured default locale → fallback text / original key.
5. **Layered i18n** — enums → classpath; config copy → this module; entity content → separate model.

---

## 2. Architecture

```
┌──────────────────┐     @MessageJoin (optional)     ┌─────────────────────┐
│  Facade / Query  │ ───────────────────────────────▶│ I18nMessageAspect   │
└────────┬─────────┘                                 └──────────┬──────────┘
         │                                                      │
         │  inject / call                                       ▼
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

**Read path**

1. Resolve `(type, messageKey, locale)`.
2. Build locale candidate list (see [§8](#8-locale-fallback)).
3. For each candidate: Caffeine hit → return; miss → load from DB (by `type` when cache enabled) → warm cache → return.
4. If nothing found → return caller `fallback`, else original `messageKey`.

---

## 3. Data Model

### 3.1 Table `gm_i18n_messages`

| Column | Type | Required | Description |
|--------|------|----------|-------------|
| `id` | BIGINT | Yes | Primary key |
| `type` | VARCHAR(50) | Yes | Message category, e.g. `ROLE`, `MENU`, `DICT_GENDER` |
| `language` | VARCHAR(20) | Yes | Locale tag aligned with `SupportedLanguage`: `zh_CN`, `en` |
| `message_key` | VARCHAR(100) | Yes* | Stable key, e.g. `ROLE_ADMIN` |
| `default_message` | MEDIUMTEXT | No | Optional default-locale display text (up to ~60k chars) |
| `i18n_message` | MEDIUMTEXT | Yes | Translated text for `language` (up to ~60k chars) |
| `private0` | INT | No | Deployment flag (existing column; keep if present) |

\* Required for new rows. Legacy rows may leave `message_key` null; the runtime falls back to
`default_message` as the key until backfilled.

**Unique index:** `(type, language, message_key)`

```sql
ALTER TABLE `gm_i18n_messages`
  ADD COLUMN `message_key` varchar(100) NULL COMMENT 'Stable message key' AFTER `language`;

UPDATE `gm_i18n_messages`
SET `message_key` = `default_message`
WHERE `message_key` IS NULL OR `message_key` = '';

ALTER TABLE `gm_i18n_messages`
  MODIFY COLUMN `message_key` varchar(100) NOT NULL COMMENT 'Stable message key',
  MODIFY COLUMN `default_message` mediumtext NULL COMMENT 'Default-locale display text (up to ~60k chars)',
  MODIFY COLUMN `i18n_message` mediumtext NOT NULL COMMENT 'Translated text (up to ~60k chars)';

-- Drop legacy unique key if present, then:
ALTER TABLE `gm_i18n_messages`
  ADD UNIQUE KEY `uidx_type_language_message_key` (`type`, `language`, `message_key`);
```

### 3.2 Entity contract

Implement `cloud.xcan.angus.core.biz.I18nMessage` and expose a Spring Data repository that extends
`I18nMessageJoinRepository<YourEntity>`.

```java
public interface I18nMessage {
  String getType();
  String getLanguage();
  /** Stable key; may fall back to defaultMessage for legacy rows. */
  String getMessageKey();
  String getDefaultMessage();
  String getI18nMessage();
}
```

---

## 4. Configuration

Prefix: `angus.i18n.message`

| Property | Default | Description |
|----------|---------|-------------|
| `enabled` | `true` | Master switch for resolver + aspect beans |
| `default-locale` | `zh_CN` | Application default locale (`SupportedLanguage` name) |
| `skip-default-locale` | `true` | When `true`, `@MessageJoin` skips work if request locale equals default (VO already holds default text). Set `false` when VO fields hold keys and must always resolve. |
| `cache.maximum-size` | `2048` | Max Caffeine entries (keyed by `type`) |
| `cache.expire-after-write-minutes` | `30` | Expire after write |
| `cache.expire-after-access-minutes` | `10` | Expire after access |
| `cache.enabled` | `true` | Global type-level cache; field annotation may still disable per field |

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

## 5. Integration Guide

### 5.1 Dependencies

Applications that already use `xcan-angusinfra.web-starter` get auto-configuration for free.

```xml
<dependency>
  <groupId>cloud.xcan.angus</groupId>
  <artifactId>xcan-angusinfra.web-starter</artifactId>
  <version>${angusinfra.version}</version>
</dependency>
```

### 5.2 Provide repository bean

```java
@Repository("commonI18nMessageRepo")
public interface I18nMessageRepo
    extends I18nMessageJoinRepository<I18nMessages>, BaseRepository<I18nMessages, Long> {
}
```

Spring Data derives:

- `findByType(String type)`
- `findByTypeAndLanguageAndMessageKeyIn(String type, String language, Collection<String> keys)`
- `findByTypeAndLanguageAndDefaultMessageIn(...)` — legacy compatibility

### 5.3 Preferred: call `I18nMessageResolver` directly

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

Batch:

```java
Map<String, String> names = i18nMessageResolver.resolveBatch(
    "ROLE", Set.of("ROLE_ADMIN", "ROLE_USER"), SdfLocaleHolder.getLocale());
```

### 5.4 Optional: VO assembly with `@MessageJoin`

**VO** — annotated field is overwritten with the translated text; key is taken from `keyField`
(or from the field itself when `keyField` is empty):

```java
@Data
public class RoleVo {
  private String code;

  /** Stable key source */
  private String nameKey;

  @MessageJoinField(type = "ROLE", keyField = "nameKey")
  private String name;
}
```

**Facade**

```java
@MessageJoin
public PageResult<RoleVo> list(RoleFindDto dto) {
  // assemble VOs with nameKey set; name may be default-locale text or empty
  return buildVoPageResult(page, RoleAssembler::toVo);
}
```

Rules:

- Missing `@MessageJoinField` → aspect **no-ops** (does not throw).
- Empty `type` on a field → still throws (configuration error).
- Inherited fields are scanned (`getAllFields`).
- Mixed element types in one collection are not supported (same as `NameJoin`).

### 5.5 Seed data example

```sql
INSERT INTO gm_i18n_messages (id, type, language, message_key, default_message, i18n_message, private0)
VALUES
  (1, 'ROLE', 'zh_CN', 'ROLE_ADMIN', '管理员', '管理员', 0),
  (2, 'ROLE', 'en',    'ROLE_ADMIN', '管理员', 'Administrator', 0),
  (3, 'ROLE', 'zh_CN', 'ROLE_USER',  '普通用户', '普通用户', 0),
  (4, 'ROLE', 'en',    'ROLE_USER',  '普通用户', 'User', 0);
```

### 5.6 After writing / updating messages

```java
i18nMessageResolver.evict("ROLE");   // one type
i18nMessageResolver.evictAll();      // all types
// or POST actuator endpoint (see §9)
```

---

## 6. API Reference

### `I18nMessageResolver`

| Method | Description |
|--------|-------------|
| `resolve(type, messageKey)` | Resolve with current `SdfLocaleHolder` locale |
| `resolve(type, messageKey, fallback)` | Same, with explicit fallback |
| `resolve(type, messageKey, locale)` | Resolve for a given locale |
| `resolve(type, messageKey, locale, fallback)` | Full form |
| `resolveBatch(type, keys, locale)` | Batch map `key → text` (missing keys omitted or mapped to themselves per impl) |
| `evict(type)` / `evictAll()` | Invalidate local cache |

### Annotations

| Annotation | Target | Description |
|------------|--------|-------------|
| `@MessageJoin` | Method | Enable post-return VO message filling |
| `@MessageJoinField(type, keyField="", enabledCache=true)` | Field | Mark VO field to fill; `keyField` names the key-holding property |

---

## 7. Caching & Invalidation

- **Store:** process-local Caffeine only (no Redis / no L2).
- **Cache key:** message `type` → nested map `language → (messageKey → I18nMessage)`.
- **Load strategy:** on miss with cache enabled, `findByType(type)` loads all rows for that type.
- **TTL:** `expireAfterWrite` + `expireAfterAccess` (configurable).
- **Consistency:** multi-instance deployments become consistent after TTL or explicit `evict*`.
  Call `evict` in the same service after admin CRUD on messages.

---

## 8. Locale Fallback

Candidate order for locale `zh_CN`:

1. Exact tag: `zh_CN`
2. Language only: `zh` (if distinct)
3. Configured `angus.i18n.message.default-locale` (e.g. `zh_CN`)
4. Caller `fallback` argument, else original `messageKey`

`SupportedLanguage` values today: `zh_CN`, `en`. Prefer storing `language` exactly as those enum names.

---

## 9. Actuator Endpoint

| Operation | HTTP | Description |
|-----------|------|-------------|
| Read cache snapshot | `GET /actuator/messages?type=ROLE` | Optional `type` filter |
| Clear cache | `POST /actuator/messages` | Evicts all in-memory entries |

Ensure `management.endpoints.web.exposure.include` contains `messages` in environments that need it.

---

## 10. Migration from Legacy default_message Lookup

| Step | Action |
|------|--------|
| 1 | Add `message_key` column; backfill `message_key = default_message` |
| 2 | Stop using Chinese display text as the only identity; assign codes (`ROLE_ADMIN`, …) |
| 3 | Point VO / assembler to keys; keep `default_message` for default-locale display if useful |
| 4 | Prefer `I18nMessageResolver` in new code; keep `@MessageJoin` where VO assembly is convenient |
| 5 | Move fixed enums out of DB into classpath i18n |

Runtime still accepts legacy rows: if `message_key` is blank, `getMessageKey()` falls back to
`default_message`. Lookup tries `message_key` first, then `default_message` for cache miss paths.

---

## 11. Best Practices

1. **Never** use mutable display text as the primary key of a translation.
2. Keep `type` coarse and stable (`ROLE`, `MENU`, `DICT_*`), not per-tenant dynamic.
3. Prefer resolver in assemblers for testability; use `@MessageJoin` for thin facades.
4. Evict cache after every successful message write API.
5. Do not put high-cardinality user content into `gm_i18n_messages`.
6. Align `language` with `SupportedLanguage` names (`zh_CN`, `en`).

---

## 12. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Always returns key / Chinese | No DB row for locale; wrong `type`/`message_key` | Check table + unique index |
| Stale text after update | Local cache not evicted | Call `evict(type)` or actuator clear |
| Aspect does nothing | No `@MessageJoinField` / default locale skipped | Add annotation or set `skip-default-locale=false` |
| `IllegalArgumentException: type is empty` | `@MessageJoinField` without `type` | Set `type` |
| Repository NPE | Bean missing | Register `I18nMessageJoinRepository` implementation |

---

## Related classes

| Class | Module |
|-------|--------|
| `cloud.xcan.angus.core.biz.I18nMessage` | `xcan-angusinfra.core` |
| `cloud.xcan.angus.core.biz.I18nMessageResolver` | `xcan-angusinfra.core` |
| `cloud.xcan.angus.core.biz.I18nMessageAspect` | `xcan-angusinfra.core` |
| `cloud.xcan.angus.core.biz.MessageJoin` | `xcan-angusinfra.core` |
| `cloud.xcan.angus.remote.MessageJoinField` | `xcan-angusinfra.remote` |
| `cloud.xcan.angus.web.endpoint.MessageEndpoint` | `xcan-angusinfra.web-starter` |
