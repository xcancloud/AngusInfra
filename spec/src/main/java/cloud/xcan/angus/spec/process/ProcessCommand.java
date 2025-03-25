package cloud.xcan.angus.spec.process;

import static cloud.xcan.angus.spec.experimental.StandardCharsets.UTF_8;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.spec.PlatformEnum;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import cloud.xcan.angus.spec.utils.StringUtils;
import com.sun.jna.Platform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * A external process execution implementation tools class for Java.
 */
@Slf4j
public final class ProcessCommand {

  public static final PlatformEnum PLATFORM = PlatformEnum.getValue(Platform.getOSType());

  public static int PROCESS_NOT_STARTED_CODE = 101; // 101: Indicate that the process not started.
  public static int PROCESS_STARTED_CONFIG_ERROR_CODE = 102; // 102: Process started but configuration error.
  public static int PROCESS_TIMEOUT_CODE = 103; // 103: Indicate that the process is timeout.
  public static int MAX_PROCESS_ERROR_CODE = 150; // 150: The biggest error in starting process, used by angusctrl.

  public static int PROCESS_NOT_EXITED_CODE = 10; // 10: Indicate that the process not exited.
  public static int PROCESS_SUCCESS_CODE = 0; // 10: Indicate that the process not exited.

  private ProcessCommand() {
  }

  public static boolean isStartException(int exitCode) {
    return exitCode >= PROCESS_NOT_STARTED_CODE && exitCode <= MAX_PROCESS_ERROR_CODE;
  }

  public static boolean isRunning(String processKey) {
    return PLATFORM.isWindows() ? isRunningOnWindows(processKey)
        : isRunningOnLinuxOrMac(processKey);
  }

  public static boolean isRunningOnLinuxOrMac(String processKey) {
    String checkRunningCmd = "ps -ef";
    CommandResult res = ProcessCommand.runNativeCmd(checkRunningCmd);
    if (!res.success) {
      return false;
    }
    for (String re : res.getResults()) {
      if (re.contains(String.valueOf(processKey))) {
        return true;
      }
    }
    return false;
  }

  public static boolean isRunningOnWindows(String processKey) {
    String checkRunningCmd = "TASKLIST /NH /FI \"WINDOWTITLE eq " + processKey + "\"";
    CommandResult res = ProcessCommand.runNativeCmd(checkRunningCmd);
    return res.success && ObjectUtils.isNotEmpty(res.getResults());
  }

  /**
   * Executes a bash on the native bash line and returns the result.
   *
   * @param cmdToRun Command to run
   * @return Run command result
   */
  public static CommandResult runNativeCmd(String cmdToRun) {
    return runNativeCmd(cmdToRun.split(" "), null);
  }

  /**
   * Executes a bash on the native bash line and returns the result.
   *
   * @param cmdToRun Command to run
   * @return Run command result
   */
  public static CommandResult runNativeCmd(String cmdToRun,
      boolean onlyMatchSuccessMessage, String successMessageRegexp) {
    return runNativeCmd(cmdToRun.split(" "), null, onlyMatchSuccessMessage, successMessageRegexp);
  }

  /**
   * Executes a bash on the native bash line and returns the result.
   *
   * @param cmdToRun Command to run
   * @return Run command result
   */
  public static CommandResult runNativeCmd(String cmdToRun, Map<String, String> envs) {
    return runNativeCmd(cmdToRun.split(" "), envs);
  }

  /**
   * Executes a bash on the native bash line with system envs and returns the result line by line.
   *
   * @param cmdToRunWithArgs Command to run and args, in an array
   * @return Run command result
   */
  public static CommandResult runNativeWithSystemEnvs(String[] cmdToRunWithArgs) {
    return runNativeCmd(cmdToRunWithArgs, System.getenv());
  }

  /**
   * Executes a bash on the native bash line with system envs and returns the result line by line.
   *
   * @param cmdToRunWithArgs Command to run and args, in an array
   * @return Run command result
   */
  public static CommandResult runNativeCmd(String[] cmdToRunWithArgs, Map<String, String> envs) {
    return runNativeCmd(cmdToRunWithArgs, envs, false, null);
  }

  /**
   * Executes a bash on the native bash line and returns the result line by line.
   *
   * @param cmdToRunWithArgs Command to run and args, in an array
   * @param envs             Process environmental variables
   * @return Run command result
   */
  public static CommandResult runNativeCmd(String[] cmdToRunWithArgs, Map<String, String> envs,
      boolean onlyMatchSuccessMessage, String successMessageRegexp) {
    Process p;
    int exitCode = PROCESS_NOT_STARTED_CODE;
    try {
      ProcessBuilder pb = new ProcessBuilder(cmdToRunWithArgs);
      if (isNotEmpty(envs)) {
        pb.environment().clear();
        pb.environment().putAll(envs);
      }
      p = pb.start();
    } catch (Exception e) {
      log.error("Couldn't run bash {}: {}", Arrays.toString(cmdToRunWithArgs), e.getMessage());
      return CommandResult.newBuilder().setSuccess(false)
          .setResults(singletonList(e.getMessage())).setExitCode(exitCode).build();
    }

    boolean execSuccess = true;
    List<String> result = new ArrayList<>();
    try {
      Scanner s = new Scanner(p.getInputStream(), UTF_8).useDelimiter(System.lineSeparator());
      while (s.hasNext()) {
        result.add(s.next());
      }
      // Block the current thread until the child process completes execution.
      exitCode = p.waitFor();
    } catch (Exception e) {
      log.error("Problem reading output by command `{}`: {}", Arrays.toString(cmdToRunWithArgs),
          e.getMessage());
      execSuccess = false;
      result.add(e.getMessage());
    }
    boolean success = execSuccess && exitCode == 0;
    if (onlyMatchSuccessMessage) {
      Pattern successPattern = Pattern.compile(successMessageRegexp);
      Matcher matcher = successPattern.matcher(StringUtils.join("\n", result));
      success = matcher.find();
    }
    return CommandResult.newBuilder().setSuccess(success)
        .setResults(result).setExitCode(exitCode).build();
  }

  public static CommandResult runNativeService(String cmdToRun, Map<String, String> envs,
      long timeoutInMilliseconds, boolean onlyMatchSuccessMessage, String successMessageRegexp) {
    return runNativeService(cmdToRun.split(" "), envs, timeoutInMilliseconds,
        onlyMatchSuccessMessage, successMessageRegexp);
  }

  /**
   * Executes a bash on the native bash line run service (not exit) with system envs and returns the
   * result line by line.
   *
   * @param cmdToRun              Command to run and args, in an array
   * @param timeoutInMilliseconds Block the current thread timeout, unit milliseconds
   * @return Run command result
   */
  public static CommandResult runNativeServiceWithSystemEnvs(String cmdToRun,
      long timeoutInMilliseconds, boolean onlyMatchSuccessMessage, String successMessageRegexp) {
    return runNativeService(cmdToRun.split(" "), System.getenv(), timeoutInMilliseconds,
        onlyMatchSuccessMessage, successMessageRegexp);
  }

  /**
   * Executes a bash on the native bash line run service (not exit) with system envs and returns the
   * result line by line.
   *
   * @param cmdToRunWithArgs      Command to run and args, in an array
   * @param timeoutInMilliseconds Block the current thread timeout, unit milliseconds
   * @return Run command result
   */
  public static CommandResult runNativeServiceWithSystemEnvs(String[] cmdToRunWithArgs,
      long timeoutInMilliseconds, boolean onlyMatchSuccessMessage, String successMessageRegexp) {
    return runNativeService(cmdToRunWithArgs, System.getenv(), timeoutInMilliseconds,
        onlyMatchSuccessMessage, successMessageRegexp);
  }

  /**
   * Executes a bash on the native bash line run service (not exit) and returns the result line by
   * line.
   *
   * @param cmdToRunWithArgs      Command to run and args, in an array
   * @param envs                  Process environmental variables
   * @param timeoutInMilliseconds Block the current thread timeout, unit milliseconds
   * @return Run command result
   */
  public static CommandResult runNativeService(String[] cmdToRunWithArgs, Map<String, String> envs,
      long timeoutInMilliseconds, boolean onlyMatchSuccessMessage, String successMessageRegexp) {
    Process p;
    int processNotStartedCode = PROCESS_NOT_STARTED_CODE; // Indicate that the process not started.
    int processNotExitedCode = PROCESS_NOT_EXITED_CODE; // Indicate that the process not exited.
    int processTimeoutCode = PROCESS_TIMEOUT_CODE; // Indicate that the process is timeout.
    try {
      ProcessBuilder pb = new ProcessBuilder(cmdToRunWithArgs);
      if (isNotEmpty(envs)) {
        pb.environment().clear();
        pb.environment().putAll(envs);
      }
      p = pb.start();
    } catch (Exception e) {
      log.error("Couldn't run bash {}: {}", Arrays.toString(cmdToRunWithArgs), e.getMessage());
      return CommandResult.newBuilder().setSuccess(false)
          .setResults(singletonList(e.getMessage())).setExitCode(processNotStartedCode).build();
    }

    boolean startSuccess = false;
    Integer exitCode = null;
    List<String> result = new ArrayList<>();
    Pattern successPattern = Pattern.compile(successMessageRegexp);
    try {
      Scanner s = new Scanner(p.getInputStream(), UTF_8).useDelimiter(System.lineSeparator());

      long startTime = System.currentTimeMillis();
      long useTime = 0;
      // AtomicBoolean stopLoop = waitFor(timeoutInMilliseconds);
      while (useTime <= timeoutInMilliseconds) {
        useTime = System.currentTimeMillis() - startTime;
        if (s.hasNext()) {
          String line = s.next();
          result.add(line);
          Matcher matcher = successPattern.matcher(line);
          if (/*Fix: matcher.matches()*/ matcher.find()) {
            startSuccess = true;
            if (!onlyMatchSuccessMessage) {
              // Wait for 2 seconds to prevent the process from starting successfully and then exiting.
              TimeUnit.MILLISECONDS.sleep(2000);
            }
            // Has success message and process hasn't exited.
            exitCode = processNotExitedCode;
            break;
          }
        } else {
          // There is no content, it may be that the process has voluntarily exited
          try {
            exitCode = p.exitValue(); // The process has voluntarily exited
            break;
          } catch (Exception e) {
            // Has success message and process hasn't exited.
          }
        }
        TimeUnit.MILLISECONDS.sleep(10);
      }

      if (isNull(exitCode)) {
        result.add("Process started, no successful message matched");
        return CommandResult.newBuilder().setSuccess(false)
            .setResults(result).setExitCode(processNotStartedCode)
            .failure(result.get(result.size() - 1))
            .build();
      }

      if (useTime >= timeoutInMilliseconds && !startSuccess) {
        result.add("Process startup timeout: " + useTime + "ms");
        return CommandResult.newBuilder().setSuccess(false)
            .setResults(result).setExitCode(processTimeoutCode)
            .failure(result.get(result.size() - 1))
            .build();
      }

      // Fix:: java.lang.IllegalThreadStateException: process hasn't exited.
      // exitCode = p.exitValue();
    } catch (Exception e) {
      log.error("Problem reading output by command `{}`: {}",
          Arrays.toString(cmdToRunWithArgs), e.getMessage());
      result.add(e.getMessage());
    }
    return CommandResult.newBuilder()
        .setSuccess(startSuccess && nonNull(exitCode))
        .setResults(result)
        .setExitCode(nullSafe(exitCode, processNotExitedCode)).build();
  }

  public static AtomicBoolean waitFor(long timeoutInMilliseconds) {
    AtomicBoolean stopLoop = new AtomicBoolean(false);
    Thread stopThread = new Thread(() -> {
      try {
        TimeUnit.MILLISECONDS.sleep(timeoutInMilliseconds);
      } catch (InterruptedException e) {
        // NOOP
      }
      stopLoop.set(true);
    });
    stopThread.start();
    return stopLoop;
  }

  /**
   * Return first line of response for selected bash.
   *
   * @param cmd2launch String bash to be launched
   * @return String or empty string if bash failed
   */
  public static CommandResult getFirstAnswer(String cmd2launch) {
    return getAnswerAt(cmd2launch, 0);
  }

  /**
   * Return response on selected line index (0-based) after running selected bash.
   *
   * @param cmd2launch String bash to be launched
   * @param answerIdx  int index of line in response of the bash
   * @return String whole line in response or empty string if invalid index or running of bash fails
   */
  public static CommandResult getAnswerAt(String cmd2launch, int answerIdx) {
    CommandResult result = ProcessCommand.runNativeCmd(cmd2launch);
    if (!result.success) {
      return result;
    }
    if (answerIdx >= 0 && answerIdx < result.getResults().size()) {
      return CommandResult.newBuilder().setSuccess(true)
          .setResults(List.of(result.getResults().get(answerIdx)))
          .build();
    }
    return CommandResult.newBuilder().setSuccess(true)
        .setResults(null)
        .build();
  }

}
