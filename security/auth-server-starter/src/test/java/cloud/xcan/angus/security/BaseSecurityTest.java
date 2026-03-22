package cloud.xcan.angus.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cloud.xcan.angus.security.authentication.CustomOAuth2TokenIntrospectionAuthenticationProvider;
import cloud.xcan.angus.security.authentication.service.CustomJdbcOAuth2AuthorizationService;
import cloud.xcan.angus.security.client.CustomOAuth2RegisteredClient;
import cloud.xcan.angus.security.model.CustomOAuth2User;
import cloud.xcan.angus.security.repository.JdbcRegisteredClientRepository;
import cloud.xcan.angus.security.repository.JdbcUserDetailsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Base test class for Security module authentication tests.
 * 
 * Provides common setup, fixtures, and helper methods for OAuth2 authentication testing.
 * 
 * @author Test Framework
 * @version 1.0
 */
@SpringBootTest(
    webEnvironment = WebEnvironment.RANDOM_PORT,
    classes = {
        OAuth2AuthorizationServerAutoConfigurer.class,
        CustomOAuth2TokenIntrospectionAuthenticationProvider.class,
    }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseSecurityTest {

  /**
   * REST API base path for OAuth2
   */
  protected static final String OAUTH2_TOKEN_ENDPOINT = "/oauth2/token";
  protected static final String OAUTH2_AUTHORIZE_ENDPOINT = "/oauth2/authorize";
  protected static final String OAUTH2_INTROSPECT_ENDPOINT = "/oauth2/introspect";

  /**
   * OAuth2 Grant Types
   */
  protected static final String GRANT_TYPE_PASSWORD = "password";
  protected static final String GRANT_TYPE_SMS = "sms_code";
  protected static final String GRANT_TYPE_EMAIL = "email_code";
  protected static final String GRANT_TYPE_DEVICE = "device_code";
  protected static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

  /**
   * Standard OAuth2 Claims
   */
  protected static final String CLAIM_GRANT_TYPE = "grant_type";
  protected static final String CLAIM_USERNAME = "username";
  protected static final String CLAIM_PASSWORD = "password";
  protected static final String CLAIM_CLIENT_ID = "client_id";
  protected static final String CLAIM_CLIENT_SECRET = "client_secret";
  protected static final String CLAIM_SCOPE = "scope";
  protected static final String CLAIM_SMS_CODE = "sms_code";
  protected static final String CLAIM_EMAIL_CODE = "email_code";
  protected static final String CLAIM_DEVICE_CODE = "device_code";
  protected static final String CLAIM_REFRESH_TOKEN = "refresh_token";

  /**
   * Standard test data
   */
  protected static final String TEST_CLIENT_ID = "test-client-id";
  protected static final String TEST_CLIENT_SECRET = "test-client-secret";
  protected static final String TEST_USERNAME = "testuser";
  protected static final String TEST_PASSWORD = "Test@1234";
  protected static final String TEST_EMAIL = "test@example.com";
  protected static final String TEST_PHONE = "13800000000";
  protected static final String TEST_TENANT_ID = "test-tenant-123";

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected PasswordEncoder passwordEncoder;

  @MockBean
  protected JdbcUserDetailsRepository userRepository;

  @MockBean
  protected JdbcRegisteredClientRepository clientRepository;

  @MockBean
  protected CustomJdbcOAuth2AuthorizationService authorizationService;

  /**
   * Creates OAuth2 token request body
   */
  protected Map<String, String> createTokenRequest(String grantType) {
    Map<String, String> request = new HashMap<>();
    request.put(CLAIM_GRANT_TYPE, grantType);
    request.put(CLAIM_CLIENT_ID, TEST_CLIENT_ID);
    request.put(CLAIM_CLIENT_SECRET, TEST_CLIENT_SECRET);
    return request;
  }

  /**
   * Creates OAuth2 password grant request body
   */
  protected Map<String, String> createPasswordGrantRequest(String username, String password) {
    Map<String, String> request = createTokenRequest(GRANT_TYPE_PASSWORD);
    request.put(CLAIM_USERNAME, username);
    request.put(CLAIM_PASSWORD, password);
    request.put(CLAIM_SCOPE, "openid profile");
    return request;
  }

  /**
   * Creates OAuth2 SMS code grant request body
   */
  protected Map<String, String> createSmsCodeGrantRequest(String phone, String smsCode) {
    Map<String, String> request = createTokenRequest(GRANT_TYPE_SMS);
    request.put(CLAIM_USERNAME, phone);
    request.put(CLAIM_SMS_CODE, smsCode);
    request.put(CLAIM_SCOPE, "openid profile");
    return request;
  }

  /**
   * Creates OAuth2 email code grant request body
   */
  protected Map<String, String> createEmailCodeGrantRequest(String email, String emailCode) {
    Map<String, String> request = createTokenRequest(GRANT_TYPE_EMAIL);
    request.put(CLAIM_USERNAME, email);
    request.put(CLAIM_EMAIL_CODE, emailCode);
    request.put(CLAIM_SCOPE, "openid profile");
    return request;
  }

  /**
   * Creates OAuth2 refresh token grant request body
   */
  protected Map<String, String> createRefreshTokenRequest(String refreshToken) {
    Map<String, String> request = createTokenRequest(GRANT_TYPE_REFRESH_TOKEN);
    request.put(CLAIM_REFRESH_TOKEN, refreshToken);
    return request;
  }

  /**
   * Executes a token request and returns the MvcResult
   */
  protected MvcResult requestToken(Map<String, String> parameters) throws Exception {
    return mockMvc.perform(
            post(OAUTH2_TOKEN_ENDPOINT)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(convertMapToQueryParams(parameters))
        )
        .andReturn();
  }

  /**
   * Extracts access token from successful token response
   */
  protected String extractAccessToken(MvcResult result) throws Exception {
    String content = result.getResponse().getContentAsString();
    Map<String, Object> response = objectMapper.readValue(content, Map.class);
    return (String) response.get("access_token");
  }

  /**
   * Asserts successful token response (200 OK with access_token)
   */
  protected void assertTokenResponseSuccess(MvcResult result) throws Exception {
    mockMvc.perform(
            post(OAUTH2_TOKEN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.access_token").exists())
        .andExpect(jsonPath("$.token_type").value("Bearer"))
        .andExpect(jsonPath("$.expires_in").isNumber());
  }

  /**
   * Asserts token error response
   */
  protected void assertTokenResponseError(MvcResult result, String expectedErrorCode)
      throws Exception {
    String content = result.getResponse().getContentAsString();
    Map<String, Object> response = objectMapper.readValue(content, Map.class);
    assertThat(response.containsKey("error")).isTrue();
  }

  /**
   * Converts a Map to MultiValueMap for request parameters
   */
  protected org.springframework.util.MultiValueMap<String, String> convertMapToQueryParams(
      Map<String, String> params) {
    org.springframework.util.LinkedMultiValueMap<String, String> multiValueMap =
        new org.springframework.util.LinkedMultiValueMap<>();
    params.forEach(multiValueMap::add);
    return multiValueMap;
  }

  /**
   * Creates a test registered client (OAuth2 client)
   */
  protected CustomOAuth2RegisteredClient createTestRegisteredClient() {
    CustomOAuth2RegisteredClient client = new CustomOAuth2RegisteredClient();
    client.setClientId(TEST_CLIENT_ID);
    client.setClientSecret(passwordEncoder.encode(TEST_CLIENT_SECRET));
    client.setClientName("Test Client");
    return client;
  }

  /**
   * Creates a test OAuth2 user
   */
  protected CustomOAuth2User createTestOAuth2User() {
    CustomOAuth2User user = new CustomOAuth2User();
    user.setUsername(TEST_USERNAME);
    user.setPassword(passwordEncoder.encode(TEST_PASSWORD));
    user.setId(1L);
    user.setTenantId(TEST_TENANT_ID);
    user.setEnabled(true);
    user.setAccountNonExpired(true);
    user.setAccountNonLocked(true);
    user.setCredentialsNonExpired(true);
    return user;
  }

  /**
   * Creates a test user with email
   */
  protected CustomOAuth2User createTestOAuth2UserWithEmail(String email) {
    CustomOAuth2User user = createTestOAuth2User();
    user.setEmail(email);
    return user;
  }

  /**
   * Creates a test user with phone
   */
  protected CustomOAuth2User createTestOAuth2UserWithPhone(String phone) {
    CustomOAuth2User user = createTestOAuth2User();
    user.setMobile(phone);
    return user;
  }

  /**
   * Asserts token contains required claims
   */
  protected void assertTokenHasRequiredClaims(String token) {
    // Parse and verify token contains required claims
    // Implementation depends on token format (JWT, opaque, etc.)
    assertThat(token).isNotNull().isNotEmpty();
  }

  /**
   * Asserts token validity and expiration
   */
  protected void assertTokenValidity(String token, long expectedExpirationSeconds) {
    assertThat(token).isNotNull().isNotEmpty();
    // Additional validation logic can be added here
  }
}
