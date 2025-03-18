示例测试
======

[English](README.md) | [中文](README_zh.md)

### 测试 grant_type=password 方式获取访问令牌

```bash
## 以客户端 `CLIENT_SECRET_BASIC` 认证方式获取
curl -X POST http://localhost:9090/oauth2/token \
-H "Authorization: Basic $(echo -n 'password-client:secret' | base64)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=password&account=user1&password=password1"

## 以客户端 `CLIENT_SECRET_POST` 认证方式获取
curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=password-client&client_secret=secret&grant_type=password&account=user1&password=password1"
```

响应示例：

```json
{
  "access_token": "qziqlG0vFI6dW8eZjQnxeH42n0p1AZRZ0KMD2iJgKNoZR46phaDa9ZzZXORDIhLxIN40zHXcoohv9_AqJVG1TA0JdZIMSEx6PagwZJtIk00XgVsXMjyrUZ0w9nM2j2UT",
  "refresh_token": "af3GWqaGZQcuTZmzKtV1r94PT29eKG2UotPCunQQdns2-RHmQDYW13qZwCw7SBS24OiauNwChXwc8VYPwZ1UsxgkS3CJA7IdSaEe4p9kNGjpFOw-YOX1roCfxiQN67xo",
  "token_type": "Bearer",
  "expires_in": 3599
}
```

### 测试 grant_type=refresh_token 方式刷新访问令牌

```bash
## 以客户端 `CLIENT_SECRET_BASIC` 认证方式获取
curl -X POST http://localhost:9090/oauth2/token \
-H "Authorization: Basic $(echo -n 'password-client:secret' | base64)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=refresh_token&refresh_token=af3GWqaGZQcuTZmzKtV1r94PT29eKG2UotPCunQQdns2-RHmQDYW13qZwCw7SBS24OiauNwChXwc8VYPwZ1UsxgkS3CJA7IdSaEe4p9kNGjpFOw-YOX1roCfxiQN67xo"

## 以客户端 `CLIENT_SECRET_POST` 认证方式获取
curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=password-client&client_secret=secret&grant_type=refresh_token&refresh_token=af3GWqaGZQcuTZmzKtV1r94PT29eKG2UotPCunQQdns2-RHmQDYW13qZwCw7SBS24OiauNwChXwc8VYPwZ1UsxgkS3CJA7IdSaEe4p9kNGjpFOw-YOX1roCfxiQN67xo"
```

响应示例：

```json
{
  "access_token": "xB-8WBkPpmXsRg8k7qTUPpHIgFJCRTtm11kmn52xl64g7VPlfLA7k5Z2u1UQ_HOOrvOmgW1YCfcYkItXZ2eAJ6sDEMVRQx5amvlVx6v8svXlhXqWQKgGUCJBUaI_3Gpn",
  "refresh_token": "vISYa2sfawJ5J5u7e1UdBntPHlS9OLsM9Js4f_4a9WNtXTDBQMYji1XkqkSvc5Urs4Ek2lj3XcrtPEMDoYtkih9HWcVs1sIS6r22LLh1P68qY3PZnam66cRYNlkacllV",
  "token_type": "Bearer",
  "expires_in": 3599
}
```

### 测试 grant_type=password 方式获取访问令牌

```bash
## 以客户端 `CLIENT_SECRET_BASIC` 认证方式获取
curl -X POST http://localhost:9090/oauth2/token \
-H "Authorization: Basic $(echo -n 'client-credentials-client:secret' | base64)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=client_credentials"

## 以客户端 `CLIENT_SECRET_POST` 认证方式获取
curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials"
```

响应示例：

```json
{
  "access_token": "oSJsEaNLvDke0PueNJcLAR7eLjn0QhtmwXdntQivgZnwBoizT5e7TLbXQVsQdLR24W7uxB0lNyIz9LptMGorKiinnfjaed8pmupa728j5nOAPp9EwspHwwqcPAIuJPm8",
  "token_type": "Bearer",
  "expires_in": 3599
}
```

### 验证访问令牌有效性

```bash
curl -X POST http://localhost:9090/oauth2/introspect \
-H "Authorization: Basic $(echo -n 'client-credentials-introspect-client:secret' | base64)" \
-d 'token=S35EIsGy8DRAL29IxY0OjY0KZanlVePh8AhF2Uwhl3OxP5jhKezES1gXnQfn9VzfD9c0Vprx3jA5EUQMCS70xlnyL04KP2r2iSy1HNPiNvqMd6oqqeK9YeSaCichURQG'
```

响应示例：

```json
{
  "active": true, 
  "sub": "user1",
  "aud": [
    "password-client"
  ],
  "nbf": 1742037158,
  "iss": "http://localhost:9090",
  "exp": 1742040758,
  "iat": 1742037158,
  "jti": "6f5e2b7d-1264-4bba-8d63-2aa31bcffc60",
  "client_id": "password-client",
  "token_type": "Bearer"
}
```

标准响应字段（根据 RFC 7662，Introspection 端点的响应包含以下标准字段）：

- **active**: 指示令牌是否有效（true 表示有效，false 表示无效）。
- **sub**: 令牌的主题（通常是用户 ID）。
- **aud**: 令牌的受众（通常是客户端 ID 或资源服务器 ID）。
- **nbf**: 令牌的生效时间（Unix 时间戳，表示在此时间之前令牌不可用）。
- **iss**: 令牌的签发者（通常是认证服务器的 URL）。
- **exp**: 令牌的过期时间（Unix 时间戳）。
- **iat**: 令牌的签发时间（Unix 时间戳）。
- **jti**: 令牌的唯一标识符（JWT ID）。
- **client_id**: 与令牌关联的客户端 ID。
- **token_type**: 令牌类型（如 Bearer）。
