package cloud.xcan.angus.spec.process;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import cloud.xcan.angus.spec.PlatformEnum;
import com.sun.jna.Platform;
import java.util.List;
import org.junit.Test;

/**
 * Test bash line and returning the result of execution.
 */
public class ProcessCommandTest {

  private static final String ECHO =
      PlatformEnum.getValue(Platform.getOSType()).isWindows()
          ? "cmd.exe /C echo Test" : "echo Test";

  private static final String BAD_COMMAND = "noOSshouldHaveACommandNamedThis";

  /**
   * Simulate starting a non exiting service.
   */
  private static final String LINUX_MOCK_SERVICE_START_COMMANDS[] = new String[]{
      "bash", "-c", "echo starting ; sleep 5s ; echo started; sleep 1000000s"};

  @Test
  public void testRunNative() {
    CommandResult result = ProcessCommand.runNativeCmd(ECHO);
    List<String> test = result.results;
    assertThat("echo output", test, hasSize(1));
    assertThat("echo output", test.get(0), is("Test"));
    assertThat("echo first answer", ProcessCommand.getFirstAnswer(ECHO).results.get(0), is("Test"));
  }

  @Test
  public void testRunBadCommand() {
    CommandResult result = ProcessCommand.runNativeCmd(BAD_COMMAND);
    assertThat("bad bash", result.results, is(hasSize(1)));
    assertThat("bad bash first answer", result.exitCode, is(101));
  }

  @Test
  public void testRunMockServiceStartCommands() {
    if (!PlatformEnum.getValue(Platform.getOSType()).isWindows()) {
      CommandResult result = ProcessCommand
          .runNativeServiceWithSystemEnvs(LINUX_MOCK_SERVICE_START_COMMANDS, 8000, false,
              ".*started.*");
      assertThat("started output", result.success, is(true));
      assertThat("started output exit code", result.exitCode, is(10));
    }
  }
}
