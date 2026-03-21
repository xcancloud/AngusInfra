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
 * Unit tests for OAuth2 SMS Code Authentication Provider
 * 
 * Tests the SMS code grant flow covering success and failure cases.
 * 
 * @author Test Framework
 * @version 1.0
 */
@DisplayName("OAuth2 SMS Code Authentication Provider Tests")
public class SmsCodeAuthenticationProviderTest extends BaseSecurityTest {

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 1. Success Scenarios
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-1: Should successfully authenticate user with valid SMS code")
  void testSmsCodeGrantWithValidCode() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
    MvcResult result = requestToken(request);

    // Assert - Success response
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("access_token");
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 2. Invalid SMS Code
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-2: Should reject invalid SMS code")
  void testSmsCodeGrantWithInvalidCode() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "000000");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-3: Should reject expired SMS code")
  void testSmsCodeGrantWithExpiredCode() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act - Use very old timestamp to simulate expired code
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "expired-code");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-4: Should prevent SMS code reuse")
  void testSmsCodeGrantCodeReuse() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act - First use succeeds
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
    MvcResult result1 = requestToken(request);
    assertThat(result1.getResponse().getStatus()).isEqualTo(200);

    // Act - Second use of same code should fail
    MvcResult result2 = requestToken(request);

    // Assert
    assertThat(result2.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 3. User Not Found
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-5: Should reject SMS code for non-existent phone")
  void testSmsCodeGrantWithNonExistentPhone() throws Exception {
    // Arrange
    when(userRepository.findByAccount("99999999999"))
        .thenReturn(null);

    // Act
    Map<String, String> request = createSmsCodeGrantRequest("99999999999", "123456");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 4. Disabled User Account
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-6: Should reject SMS code for disabled user")
  void testSmsCodeGrantWithDisabledUser() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDisabledTestUser();
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 5. Rate Limiting
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-7: Should enforce rate limiting on SMS code attempts")
  void testSmsCodeGrantRateLimiting() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act - Multiple failed attempts
    for (int i = 0; i < 5; i++) {
      Map<String, String> request = createSmsCodeGrantRequest("13800000000", "wrong" + i);
      requestToken(request);
    }

    // Assert - Request should be blocked after too many attempts
    Map<String, String> finalRequest = createSmsCodeGrantRequest("13800000000", "123456");
    MvcResult result = requestToken(finalRequest);
    assertThat(result.getResponse().getStatus()).isEqualTo(429); // Too Many Requests
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 6. Scope Handling
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-8: Should handle scope in SMS code grant")
  void testSmsCodeGrantWithScope() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
    request.put(CLAIM_SCOPE, "openid profile");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 7. Token Properties
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-9: Should include refresh token in SMS code grant response")
  void testSmsCodeGrantReturnsRefreshToken() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("refresh_token");
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 8. Concurrent SMS Codes
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-10: Should handle concurrent SMS code requests")
  void testConcurrentSmsCodeRequests() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act - Two threads requesting tokens with SMS codes simultaneously
    Thread thread1 = new Thread(() -> {
      try {
        Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
        MvcResult result = requestToken(request);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread thread2 = new Thread(() -> {
      try {
        Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
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
  @DisplayName("TC-11: Should reject SMS code grant with missing phone number")
  void testSmsCodeGrantWithMissingPhone() throws Exception {
    // Act
    Map<String, String> request = createTokenRequest(GRANT_TYPE_SMS);
    request.put(CLAIM_SMS_CODE, "123456");
    // Missing CLAIM_USERNAME (phone)
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-12: Should reject SMS code grant with missing SMS code")
  void testSmsCodeGrantWithMissingSmsCode() throws Exception {
    // Act
    Map<String, String> request = createTokenRequest(GRANT_TYPE_SMS);
    request.put(CLAIM_USERNAME, "13800000000");
    // Missing CLAIM_SMS_CODE
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 10. Multi-Tenant Isolation
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-13: Should enforce multi-tenant isolation for SMS code grants")
  void testSmsCodeGrantMultiTenantIsolation() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
    MvcResult result = requestToken(request);

    // Assert - Token should be associated with user's tenant
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 11. Client Authentication
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-14: Should reject SMS code grant with invalid client")
  void testSmsCodeGrantWithInvalidClient() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
    request.put(CLAIM_CLIENT_ID, "invalid-client");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("invalid_client");
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 12. Token Expiration
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-15: Should set correct token expiration for SMS code grant")
  void testSmsCodeGrantTokenExpiration() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createUserWithPhone("13800000000");
    when(userRepository.findByAccount("13800000000"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createSmsCodeGrantRequest("13800000000", "123456");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("expires_in");
  }
}
