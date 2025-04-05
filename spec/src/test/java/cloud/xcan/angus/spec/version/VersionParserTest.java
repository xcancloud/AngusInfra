package cloud.xcan.angus.spec.version;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class VersionParserTest {

  @Test
  public void shouldParseNormalVersion() {
    NormalVersion version = VersionParser.parseVersionCore("1.0.0");
    assertEquals(new NormalVersion(1, 0, 0), version);
  }

  @Test
  public void shouldRaiseErrorIfNumericIdentifierHasLeadingZeroes() {
    try {
      VersionParser.parseVersionCore("01.1.0");
    } catch (ParseException e) {
      return;
    }
    fail("Numeric identifier MUST NOT contain leading zeroes");
  }

  @Test
  public void shouldParsePreReleaseVersion() {
    MetadataVersion preRelease = VersionParser.parsePreRelease("beta-1.1");
    assertEquals(new MetadataVersion(new String[]{"beta-1", "1"}), preRelease);
  }

  @Test
  public void shouldNotAllowDigitsInPreReleaseVersion() {
    try {
      VersionParser.parsePreRelease("alpha.01");
    } catch (ParseException e) {
      return;
    }
    fail("Should not allow digits in pre-release version");
  }

  @Test
  public void shouldRaiseErrorForEmptyPreReleaseIdentifier() {
    try {
      VersionParser.parsePreRelease("beta-1..1");
    } catch (ParseException e) {
      return;
    }
    fail("Identifiers MUST NOT be empty");
  }

  @Test
  public void shouldParseBuildMetadata() {
    MetadataVersion build = VersionParser.parseBuild("build.1");
    assertEquals(new MetadataVersion(new String[]{"build", "1"}), build);
  }

  @Test
  public void shouldAllowDigitsInBuildMetadata() {
    try {
      VersionParser.parseBuild("build.01");
    } catch (ParseException e) {
      fail("Should allow digits in build metadata");
    }
  }

  @Test
  public void shouldRaiseErrorForEmptyBuildIdentifier() {
    try {
      VersionParser.parseBuild(".build.01");
    } catch (ParseException e) {
      return;
    }
    fail("Identifiers MUST NOT be empty");
  }

  @Test
  public void shouldParseValidSemVer() {
    VersionParser parser = new VersionParser("1.0.0-rc.2+build.05");
    Version version = parser.parse(null);
    assertEquals(
        new Version(
            new NormalVersion(1, 0, 0),
            new MetadataVersion(new String[]{"rc", "2"}),
            new MetadataVersion(new String[]{"build", "05"})
        ),
        version
    );
  }

  @Test
  public void shouldRaiseErrorForIllegalInputString() {
    for (String illegal : new String[]{"", null}) {
      try {
        new VersionParser(illegal);
      } catch (IllegalArgumentException e) {
        continue;
      }
      fail("Should raise error for illegal input string");
    }
  }
}
