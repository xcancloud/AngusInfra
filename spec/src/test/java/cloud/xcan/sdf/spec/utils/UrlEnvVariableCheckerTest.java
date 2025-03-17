package cloud.xcan.sdf.spec.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UrlEnvVariableCheckerTest {

  @Test
  void testContainsEnvironmentVariable() {
    // 测试包含环境变量的URL
    assertTrue(UrlEnvVariableChecker.containsEnvVariable("https://{HOST}/api"));
    assertTrue(
        UrlEnvVariableChecker.containsEnvVariable("https://{HOST}:{PORT}/api"));
    assertTrue(
        UrlEnvVariableChecker.containsEnvVariable("{PROTOCOL}://{HOST}/api"));

    // 测试不包含环境变量的URL
    assertFalse(
        UrlEnvVariableChecker.containsEnvVariable("https://example.com/api"));
    assertFalse(UrlEnvVariableChecker.containsEnvVariable("http://localhost:8080"));

    // 测试边界情况
    assertFalse(UrlEnvVariableChecker.containsEnvVariable(""));
    assertFalse(UrlEnvVariableChecker.containsEnvVariable(null));

    // 测试特殊情况
    assertFalse(UrlEnvVariableChecker.containsEnvVariable(
        "https://example.com/NOT_A_VAR"));
    assertTrue(UrlEnvVariableChecker.containsEnvVariable(
        "https://example.com/{VAR}not_closed"));
    assertFalse(
        UrlEnvVariableChecker.containsEnvVariable("https://example.com/{}"));
  }

  @Test
  void testMultipleEnvironmentVariables() {
    assertTrue(UrlEnvVariableChecker.containsEnvVariable(
        "{PROTOCOL}://{HOST}:{PORT}/{PATH}"));
  }

  @Test
  void testPartialMatch() {
    assertTrue(UrlEnvVariableChecker.containsEnvVariable("prefix_{VAR}_suffix"));
  }

  @Test
  void testCaseSensitivity() {
    assertTrue(UrlEnvVariableChecker.containsEnvVariable("https://{host}/api"));
    assertTrue(UrlEnvVariableChecker.containsEnvVariable("https://{HOST}/api"));
  }
}
