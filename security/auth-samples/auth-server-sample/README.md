Example Testing
======

[English](README.md) | [中文](README_zh.md)

### Testing `grant_type=password` to Obtain Access Token

```bash
## Using `CLIENT_SECRET_BASIC` Authentication
curl -X POST http://localhost:9090/oauth2/token \
-H "Authorization: Basic $(echo -n 'password-client:secret' | base64)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=password&account=user1&password=password1"

## Using `CLIENT_SECRET_POST` Authentication
curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=password-client&client_secret=secret&grant_type=password&account=user1&password=password1"
```

Response Example:

```json
{
  "access_token": "qziqlG0vFI6dW8eZjQnxeH42n0p1AZRZ0KMD2iJgKNoZR46phaDa9ZzZXORDIhLxIN40zHXcoohv9_AqJVG1TA0JdZIMSEx6PagwZJtIk00XgVsXMjyrUZ0w9nM2j2UT",
  "refresh_token": "af3GWqaGZQcuTZmzKtV1r94PT29eKG2UotPCunQQdns2-RHmQDYW13qZwCw7SBS24OiauNwChXwc8VYPwZ1UsxgkS3CJA7IdSaEe4p9kNGjpFOw-YOX1roCfxiQN67xo",
  "token_type": "Bearer",
  "expires_in": 3599
}
```

### Testing `grant_type=refresh_token` to Refresh Access Token

```bash
## Using `CLIENT_SECRET_BASIC` Authentication
curl -X POST http://localhost:9090/oauth2/token \
-H "Authorization: Basic $(echo -n 'password-client:secret' | base64)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=refresh_token&refresh_token=af3GWqaGZQcuTZmzKtV1r94PT29eKG2UotPCunQQdns2-RHmQDYW13qZwCw7SBS24OiauNwChXwc8VYPwZ1UsxgkS3CJA7IdSaEe4p9kNGjpFOw-YOX1roCfxiQN67xo"

## Using `CLIENT_SECRET_POST` Authentication
curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=password-client&client_secret=secret&grant_type=refresh_token&refresh_token=af3GWqaGZQcuTZmzKtV1r94PT29eKG2UotPCunQQdns2-RHmQDYW13qZwCw7SBS24OiauNwChXwc8VYPwZ1UsxgkS3CJA7IdSaEe4p9kNGjpFOw-YOX1roCfxiQN67xo"
```

Response Example:

```json
{
  "access_token": "xB-8WBkPpmXsRg8k7qTUPpHIgFJCRTtm11kmn52xl64g7VPlfLA7k5Z2u1UQ_HOOrvOmgW1YCfcYkItXZ2eAJ6sDEMVRQx5amvlVx6v8svXlhXqWQKgGUCJBUaI_3Gpn",
  "refresh_token": "vISYa2sfawJ5J5u7e1UdBntPHlS9OLsM9Js4f_4a9WNtXTDBQMYji1XkqkSvc5Urs4Ek2lj3XcrtPEMDoYtkih9HWcVs1sIS6r22LLh1P68qY3PZnam66cRYNlkacllV",
  "token_type": "Bearer",
  "expires_in": 3599
}
```

### Testing `grant_type=client_credentials` to Obtain Access Token

```bash
## Using `CLIENT_SECRET_BASIC` Authentication
curl -X POST http://localhost:9090/oauth2/token \
-H "Authorization: Basic $(echo -n 'client-credentials-client:secret' | base64)" \
-H "Content-Type: application/x-www-form-urlencoded" \
-d "grant_type=client_credentials"

## Using `CLIENT_SECRET_POST` Authentication
curl -X POST http://localhost:9090/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials"
```

Response Example:

```json
{
  "access_token": "oSJsEaNLvDke0PueNJcLAR7eLjn0QhtmwXdntQivgZnwBoizT5e7TLbXQVsQdLR24W7uxB0lNyIz9LptMGorKiinnfjaed8pmupa728j5nOAPp9EwspHwwqcPAIuJPm8",
  "token_type": "Bearer",
  "expires_in": 3599
}
```

### Verify Access Token Validity

```bash
curl -X POST http://localhost:9090/oauth2/introspect \
-H "Authorization: Basic $(echo -n 'client-credentials-introspect-client:secret' | base64)" \
-d 'token=qxyKpqZPKGyeyHIS2YPUoXYdYKp8_bHU7qAlSVT-8wdeB1c3J2-iuTZebB6pBkz2DHugYsYgQD9XM_BftWaJKgRXGzLEKwL59wCXE7BPZaYnqq7MOTB2WPAdS8pc3FFj'
```

Response Example:

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

Standard Response Fields (According to RFC 7662, the response from the Introspection endpoint includes the following standard fields):

- **active**: Indicates whether the token is valid (`true` for valid, `false` for invalid).
- **sub**: The subject of the token (usually the user ID).
- **aud**: The audience of the token (usually the client ID or resource server ID).
- **nbf**: The not-before time of the token (Unix timestamp, indicating the token is not valid before this time).
- **iss**: The issuer of the token (usually the URL of the authorization server).
- **exp**: The expiration time of the token (Unix timestamp).
- **iat**: The issuance time of the token (Unix timestamp).
- **jti**: The unique identifier of the token (JWT ID).
- **client_id**: The client ID associated with the token.
- **token_type**: The type of the token (e.g., `Bearer`).
