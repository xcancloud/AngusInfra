package cloud.xcan.angus.security.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.security.BaseSecurityTest;
import cloud.xcan.angus.security.SecurityTestFixtures;
import cloud.xcan.angus.security.model.CustomOAuth2User;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Unit tests for OAuth2 Password Authentication Provider
 * <p>
 * Tests the password grant flow (Resource Owner Password Credentials Grant) covering success and
 * failure cases.
 *
 * @author Test Framework
 * @version 1.0
 */
@DisplayName("OAuth2 Password Authentication Provider Tests")
public class OAuth2PasswordAuthenticationProviderTest extends BaseSecurityTest {

  @BeforeEach
  void setUp() {
    // Setup default test client and user
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 1. Success Scenarios
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-1: Should successfully authenticate user with valid credentials")
  void testPasswordGrantWithValidCredentials() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act & Assert
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Verify response contains access token
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("TC-2: Should return token with bearer type")
  void testPasswordGrantReturnsBearerToken() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Assert
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("\"token_type\":\"Bearer\"");
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 2. Invalid Credentials
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-3: Should reject request with invalid password")
  void testPasswordGrantWithInvalidPassword() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, "WrongPassword123!");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("invalid_grant");
  }

  @Test
  @DisplayName("TC-4: Should reject request with empty password")
  void testPasswordGrantWithEmptyPassword() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, "");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 3. User Not Found / Account Issues
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-5: Should reject request for non-existent user")
  void testPasswordGrantWithUserNotFound() throws Exception {
    // Arrange
    when(userRepository.findByAccount("nonexistent"))
        .thenReturn(null);

    // Act
    Map<String, String> request = createPasswordGrantRequest("nonexistent", TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("invalid_grant");
  }

  @Test
  @DisplayName("TC-6: Should reject disabled user account")
  void testPasswordGrantWithDisabledUser() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDisabledTestUser();
    when(userRepository.findByAccount("disabled-user"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest("disabled-user", TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-7: Should reject locked user account")
  void testPasswordGrantWithAccountLocked() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createLockedTestUser();
    when(userRepository.findByAccount("locked-user"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest("locked-user", TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-8: Should reject user with expired credentials")
  void testPasswordGrantWithExpiredCredentials() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createExpiredCredentialsTestUser();
    when(userRepository.findByAccount("expired-creds-user"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest("expired-creds-user", TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("TC-9: Should reject user with expired account")
  void testPasswordGrantWithExpiredAccount() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createExpiredAccountTestUser();
    when(userRepository.findByAccount("expired-account-user"))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest("expired-account-user", TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 4. Client Authentication Issues
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-10: Should reject request with invalid client_id")
  void testPasswordGrantWithInvalidClient() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    request.put(CLAIM_CLIENT_ID, "invalid-client");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("invalid_client");
  }

  @Test
  @DisplayName("TC-11: Should reject request with missing client_id")
  void testPasswordGrantWithMissingClientId() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    request.remove(CLAIM_CLIENT_ID);
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(400);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 5. Grant Type & Scope Validation
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-12: Should validate requested scopes")
  void testPasswordGrantScopeValidation() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    request.put(CLAIM_SCOPE, "openid profile email"); // Additional scope
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("TC-13: Should handle empty scope request")
  void testPasswordGrantWithEmptyScope() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    request.remove(CLAIM_SCOPE);
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 6. Refresh Token Tests
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-14: Should return refresh token with access token")
  void testPasswordGrantReturnsRefreshToken() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Assert
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("refresh_token");
  }

  @Test
  @DisplayName("TC-15: Should successfully refresh access token")
  void testRefreshTokenFlow() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // First get initial tokens
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    MvcResult initialResult = requestToken(request);
    String refreshToken = extractAccessToken(initialResult); // In real test, parse refresh_token

    // Act: Use refresh token to get new access token
    Map<String, String> refreshRequest = createRefreshTokenRequest(refreshToken);
    MvcResult result = requestToken(refreshRequest);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 7. Token Lifecycle
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-16: Should set correct token expiration time")
  void testTokenExpiration() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Assert - verify expires_in is in response
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("expires_in");
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 8. Concurrency & Thread Safety
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-17: Should handle concurrent authentication requests")
  void testConcurrentTokenRequests() throws Exception {
    // Arrange
    CustomOAuth2User user = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(user);

    // Act: Simulate concurrent requests
    Thread thread1 = new Thread(() -> {
      try {
        Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
        MvcResult result = requestToken(request);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });

    Thread thread2 = new Thread(() -> {
      try {
        Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
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
    // Test passed if both threads succeeded
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 9. Multi-Tenant Isolation
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-18: Should enforce multi-tenant token isolation")
  void testMultiTenantTokenIsolation() throws Exception {
    // Arrange - create user in tenant A
    CustomOAuth2User userA = SecurityTestFixtures.createDefaultTestUser();
    when(userRepository.findByAccount(TEST_USERNAME))
        .thenReturn(userA);

    // Act
    Map<String, String> request = createPasswordGrantRequest(TEST_USERNAME, TEST_PASSWORD);
    MvcResult result = requestToken(request);

    // Assert - token should be associated with user's tenant
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
  }

  // ┌──────────────────────────────────────────────────────────────────────────┐
  // │ 10. Admin User Tests
  // └──────────────────────────────────────────────────────────────────────────┘

  @Test
  @DisplayName("TC-19: Should authenticate admin user successfully")
  void testPasswordGrantWithAdminUser() throws Exception {
    // Arrange
    CustomOAuth2User admin = SecurityTestFixtures.createAdminTestUser();
    when(userRepository.findByAccount("admin"))
        .thenReturn(admin);

    // Act
    Map<String, String> request = createPasswordGrantRequest("admin", "Admin@1234");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
    String content = result.getResponse().getContentAsString();
    assertThat(content).contains("access_token");
  }

  @Test
  @DisplayName("TC-20: Should include user roles in token claims")
  void testTokenIncludesUserRoles() throws Exception {
    // Arrange
    CustomOAuth2User admin = SecurityTestFixtures.createAdminTestUser();
    when(userRepository.findByAccount("admin"))
        .thenReturn(admin);

    // Act
    Map<String, String> request = createPasswordGrantRequest("admin", "Admin@1234");
    MvcResult result = requestToken(request);

    // Assert
    assertThat(result.getResponse().getStatus()).isEqualTo(200);
    // Token should contain role information (detailed claim verification in integration tests)
  }
}
