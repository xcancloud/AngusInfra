package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.core.utils.CoreUtils.calcPassdStrength;
import static cloud.xcan.angus.core.utils.CoreUtils.extractMD5Key;
import static cloud.xcan.angus.core.utils.CoreUtils.runAtJar;
import static cloud.xcan.angus.spec.utils.ObjectUtils.distinctByKey;
import static cloud.xcan.angus.spec.utils.ObjectUtils.duplicateByKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.api.enums.PassdStrength;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Order;


public class CoreUtilsTest {

  private static List<TestUser> users;
  private static List<TestUser2> users21;
  private static List<TestUser2> users22;

  @BeforeClass
  public static void init() {
    users = new ArrayList<>();
    users.add(new TestUser("a"));
    users.add(new TestUser("b"));
    users.add(new TestUser("b"));
    users.add(new TestUser("c"));

    users21 = new ArrayList<>();
    users21.add(new TestUser2(1L, "a"));
    users21.add(new TestUser2(2L, "a"));
    users21.add(new TestUser2(3L, "c"));

    users22 = new ArrayList<>();
    users22.add(new TestUser2(1L, "a"));
    users22.add(new TestUser2(2L, "a"));
    users22.add(new TestUser2(1L, "b"));
  }

  @Test
  @Order(1)
  public void testRetainAll() {
    assertTrue(CoreUtils.retainAll(users21, users22));
    assertTrue(users21.size() == 2);
    assertTrue(users21.get(0).getId() == 1L);
    assertTrue(users21.get(1).getId() == 2L);
    assertTrue(users22.size() == 3);
  }

  @Test
  @Order(2)
  public void testRemoveAll() {
    assertTrue(CoreUtils.removeAll(users21, users22));
    assertTrue(users21.isEmpty());
    assertTrue(users22.size() == 3);
  }

  @Test
  public void testContains() {
    assertTrue(CoreUtils.contains(users21, new TestUser2(1L, "a")));
    assertTrue(!CoreUtils.contains(users21, new TestUser2(1L, "d")));
  }

  @Test
  public void testRunAtJar() {
    Assert.assertFalse(runAtJar());
  }

  @Test
  public void testDistinctByKey() {
    List<TestUser> newUsers = users.stream()
        .filter(distinctByKey(TestUser::getUsername))
        .toList();
    assertEquals(3, newUsers.size());
  }

  @Test
  public void testDuplicateByKey() {
    List<TestUser> newUsers = users.stream()
        .filter(duplicateByKey(TestUser::getUsername))
        .toList();
    assertEquals(1, newUsers.size());
    assertTrue(newUsers.get(0).getUsername().equalsIgnoreCase("b"));
  }

  @Test
  public void testExtractTokenKey() {
    String accessToken = "873973a4-7d0d-49c5-962e-7d33b31434e2";
    String tokenKey = "1ff317629c0b301b7682794e6c1095be";
    assertEquals(tokenKey, extractMD5Key(accessToken));
  }

  @Test
  public void testCalcPassdStrength() {
    assertEquals(PassdStrength.WEAK, calcPassdStrength("123456"));
    assertEquals(PassdStrength.WEAK, calcPassdStrength("123abc"));
    assertEquals(PassdStrength.WEAK, calcPassdStrength("123456!@#"));
    assertEquals(PassdStrength.MEDIUM, calcPassdStrength("1234567890ab"));
    assertEquals(PassdStrength.MEDIUM, calcPassdStrength("123456!@#AAA12"));
    assertEquals(PassdStrength.MEDIUM, calcPassdStrength("123456!@#Aa"));
    assertEquals(PassdStrength.STRONG, calcPassdStrength("1234567890ecv34gfb"));
    assertEquals(PassdStrength.STRONG, calcPassdStrength("1234567890acbAC"));
    assertEquals(PassdStrength.STRONG, calcPassdStrength("123456789Aa#"));
  }

  /**
   * The version number associated with this {@code UUID}.  The version number describes how this
   * {@code UUID} was generated.
   * <p>
   * The version number has the following meaning:
   * <ul>
   * <li>1    Time-based UUID
   * <li>2    DCE security UUID
   * <li>3    Name-based UUID
   * <li>4    Randomly generated UUID
   * </ul>
   */
  @Test
  public void testJavaDefaultUuidStruct() {
    UUID uuid = UUID.randomUUID();
    System.out.println("UUID:" + uuid.toString());
    System.out.println("Version:" + uuid.version());
    //System.out.println("Sequence:" + uuid.clockSequence()); // java.lang.UnsupportedOperationException: Not a time-based UUID
    //System.out.println("Timestamp:" + uuid.timestamp()); // java.lang.UnsupportedOperationException: Not a time-based UUID
    //System.out.println("Node:" + uuid.node()); // java.lang.UnsupportedOperationException: Not a time-based UUID
    System.out.println("Variant:" + uuid.variant());
    System.out.println("leastSigBits:" + uuid.getLeastSignificantBits());
    System.out.println("mostSigBits:" + uuid.getMostSignificantBits());

    // ce1ae06c-4d54-4abe-9b59-c0ed74f6de84
  }


  @Setter
  @Getter
  @AllArgsConstructor
  public static class TestUser {

    private String username;
  }

  @Setter
  @Getter
  @AllArgsConstructor
  public static class TestUser2 extends TenantAuditingEntity<TestUser2, Long> {

    private Long id;
    private String username;

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
