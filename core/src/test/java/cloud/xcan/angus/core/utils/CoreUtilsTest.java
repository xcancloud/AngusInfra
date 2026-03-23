package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.core.utils.CoreUtils.calcPasswordStrength;
import static cloud.xcan.angus.core.utils.CoreUtils.extractMD5Key;
import static cloud.xcan.angus.core.utils.CoreUtils.runAtJar;
import static cloud.xcan.angus.spec.utils.ObjectUtils.distinctByKey;
import static cloud.xcan.angus.spec.utils.ObjectUtils.duplicateByKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.api.enums.PasswordStrength;
import cloud.xcan.angus.spec.experimental.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CoreUtilsTest {

  private List<TestUser> users;

  @BeforeEach
  void setUp() {
    users = new ArrayList<>();
    users.add(new TestUser("a"));
    users.add(new TestUser("b"));
    users.add(new TestUser("b"));
    users.add(new TestUser("c"));
  }

  private static List<TestUser2> newUsers21() {
    List<TestUser2> list = new ArrayList<>();
    list.add(new TestUser2(1L, "a"));
    list.add(new TestUser2(2L, "a"));
    list.add(new TestUser2(3L, "c"));
    return list;
  }

  private static List<TestUser2> newUsers22() {
    List<TestUser2> list = new ArrayList<>();
    list.add(new TestUser2(1L, "a"));
    list.add(new TestUser2(2L, "a"));
    list.add(new TestUser2(1L, "b"));
    return list;
  }

  @Test
  void testRetainAll() {
    List<TestUser2> users21 = newUsers21();
    List<TestUser2> users22 = newUsers22();
    assertTrue(CoreUtils.retainAll(users21, users22));
    assertEquals(2, users21.size());
    assertEquals(1L, users21.get(0).getId());
    assertEquals(2L, users21.get(1).getId());
    assertEquals(3, users22.size());
  }

  @Test
  void testRemoveAll() {
    List<TestUser2> users21 = newUsers21();
    List<TestUser2> users22 = newUsers22();
    CoreUtils.retainAll(users21, users22);
    assertTrue(CoreUtils.removeAll(users21, users22));
    assertTrue(users21.isEmpty());
    assertEquals(3, users22.size());
  }

  @Test
  void testContains() {
    List<TestUser2> users21 = newUsers21();
    assertTrue(CoreUtils.contains(users21, new TestUser2(1L, "a")));
    assertFalse(CoreUtils.contains(users21, new TestUser2(1L, "d")));
  }

  @Test
  void testRunAtJar() {
    assertFalse(runAtJar());
  }

  @Test
  void testDistinctByKey() {
    List<TestUser> newUsers = users.stream()
        .filter(distinctByKey(TestUser::getUsername))
        .toList();
    assertEquals(3, newUsers.size());
  }

  @Test
  void testDuplicateByKey() {
    List<TestUser> newUsers = users.stream()
        .filter(duplicateByKey(TestUser::getUsername))
        .toList();
    assertEquals(1, newUsers.size());
    assertTrue(newUsers.get(0).getUsername().equalsIgnoreCase("b"));
  }

  @Test
  void testExtractTokenKey() {
    String accessToken = "873973a4-7d0d-49c5-962e-7d33b31434e2";
    String tokenKey = "1ff317629c0b301b7682794e6c1095be";
    assertEquals(tokenKey, extractMD5Key(accessToken));
  }

  @Test
  void testCalcPassdStrength() {
    assertEquals(PasswordStrength.WEAK, calcPasswordStrength("123456"));
    assertEquals(PasswordStrength.WEAK, calcPasswordStrength("123abc"));
    assertEquals(PasswordStrength.WEAK, calcPasswordStrength("123456!@#"));
    assertEquals(PasswordStrength.MEDIUM, calcPasswordStrength("1234567890ab"));
    assertEquals(PasswordStrength.MEDIUM, calcPasswordStrength("123456!@#AAA12"));
    assertEquals(PasswordStrength.MEDIUM, calcPasswordStrength("123456!@#Aa"));
    assertEquals(PasswordStrength.STRONG, calcPasswordStrength("1234567890ecv34gfb"));
    assertEquals(PasswordStrength.STRONG, calcPasswordStrength("1234567890acbAC"));
    assertEquals(PasswordStrength.STRONG, calcPasswordStrength("123456789Aa#"));
  }

  @Test
  void testJavaDefaultUuidStruct() {
    UUID uuid = UUID.randomUUID();
    assertNotNull(uuid.toString());
    assertEquals(4, uuid.version());
    assertEquals(2, uuid.variant());
  }

  @Setter
  @Getter
  @AllArgsConstructor
  public static class TestUser {

    private String username;
  }

  public static class TestUser2 implements Entity<TestUser2, Long> {

    private final Long id;
    private final String username;

    public TestUser2(Long id, String username) {
      this.id = id;
      this.username = username;
    }

    public Long getId() {
      return id;
    }

    public String getUsername() {
      return username;
    }

    @Override
    public boolean sameIdentityAs(TestUser2 other) {
      return Objects.nonNull(username) && other != null && username.equals(other.username);
    }

    @Override
    public Long identity() {
      return id;
    }
  }
}
