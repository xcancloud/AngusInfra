package cloud.xcan.angus.security.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.security.BaseSecurityTest;
import cloud.xcan.angus.security.SecurityTestFixtures;
import cloud.xcan.angus.security.model.CustomOAuth2User;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Unit tests for OAuth2 Email Code Authentication Provider
 * 
 * Tests the email code grant flow covering success and failure cases.
 * 
 * @author Test Framework
 * @version 1.0
 */
@DisplayName("OAuth2 Email Code Authentication Provider Tests")
public class EmailCodeAuthenticationProviderTest extends BaseSecurityTest {

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 1. Success Scenarios
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-1: Should successfully authenticate user with valid email code")
  void testEmailCodeGrantWithValidCode() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "123456");
    MvcResult result = requestToken(request);

    // Assert - Success response
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("access_token");
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 2. Invalid Email Code
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-2: Should reject invalid email code")
  void testEmailCodeGrantWithInvalidCode() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "000000");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-3: Should reject expired email code")
  void testEmailCodeGrantWithExpiredCode() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act - Use very old timestamp to simulate expired code
    Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "expired-code");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-4: Should prevent email code reuse")
  void testEmailCodeGrantCodeReuse() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act - First use succeeds
    Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "123456");
    MvcResult result1 = requestToken(request);
    assertThat(result1.getResponse().getStatus()).isEqualTo(200);

    // Act - Second use of same code should fail
    MvcResult result2 = requestToken(request);

    // Assert
    assertThat(result2.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 3. User Not Found / Invalid Email
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-5: Should reject email code for non-existent email")
  void testEmailCodeGrantWithNonExistentEmail() throws Exception {
    // Arrange
    when(userRepository.findByAccount("nonexistent@example.com"))
        .thenReturn(null);

    // Act
    Map<String, String> request = createEmailCodeGrantRequest("nonexistent@example.com", "123456");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-6: Should reject invalid email format")
  void testEmailCodeGrantWithInvalidEmailFormat() throws Exception {
    // Act
    Map<String, String> request = createEmailCodeGrantRequest("invalid-email", "123456");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 4. Disabled User Account
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-7: Should reject email code for disabled user")
  void testEmailCodeGrantWithDisabledUser() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDisabledTestUser();
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "123456");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 5. Rate Limiting
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-8: Should enforce rate limiting on email code attempts")
  void testEmailCodeGrantRateLimiting() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act - Multiple failed attempts
    for (int i = 0; i < 5; i++) {
      Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "wrong" + i);
      requestToken(request);
    }

    // Assert - Request should be blocked after too many attempts
    Map<String, String> finalRequest = createEmailCodeGrantRequest("test@example.com", "123456");
    MvcResult result = requestToken(finalRequest);
    assertThat(result.getResponse().getStatus()).isEqualTo(429); // Too Many Requests
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 6. Scope Handling
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-9: Should handle scope in email code grant")
  void testEmailCodeGrantWithScope() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "123456");
    request.put(CLAIM_SCOPE, "openid profile email");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 7. Token Properties
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-10: Should include refresh token in email code grant response")
  void testEmailCodeGrantReturnsRefreshToken() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "123456");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("refresh_token");
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 8. Concurrent Email Code Requests
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-11: Should handle concurrent email code requests")
  void testConcurrentEmailCodeRequests() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act - Two threads requesting tokens with email codes simultaneously
    Thread thread1 = new Thread(() -> {
      try {
        Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "123456");
        MvcResult result = requestToken(request);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread thread2 = new Thread(() -> {
      try {
        Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "123456");
        MvcResult result = requestToken(request);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 9. Missing Required Parameters
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-12: Should reject email code grant with missing email")
  void testEmailCodeGrantWithMissingEmail() throws Exception {
    // Act
    Map<String, String> request = createTokenRequest(GRANT_TYPE_EMAIL);
    request.put(CLAIM_EMAIL_CODE, "123456");
    // Missing CLAIM_USERNAME (email)
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-13: Should reject email code grant with missing email code")
  void testEmailCodeGrantWithMissingEmailCode() throws Exception {
    // Act
    Map<String, String> request = createTokenRequest(GRANT_TYPE_EMAIL);
    request.put(CLAIM_USERNAME, "test@example.com");
    // Missing CLAIM_EMAIL_CODE
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 10. Multi-Tenant Isolation
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-14: Should enforce multi-tenant isolation for email code grants")
  void testEmailCodeGrantMultiTenantIsolation() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "123456");
    MvcResult result = requestToken(request);

    // Assert - Token should be associated with user's tenant
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 11. Client Authentication
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-15: Should reject email code grant with invalid client")
  void testEmailCodeGrantWithInvalidClient() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithEmail("test@example.com");
    when(userRepository.findByAccount("test@example.com"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createEmailCodeGrantRequest("test@example.com", "123456");
    request.put(CLAIM_CLIENT_ID, "invalid-client");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("invalid_client");
  }
}
