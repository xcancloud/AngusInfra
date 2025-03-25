package cloud.xcan.angus.spec.process;

import java.util.List;

public class CommandResult {

  /**
   * Whether the process command was called successfully.
   */
  public boolean success;

  /**
   * Executing process console output contents.
   */
  public List<String> results;

  /**
   * In Linux, the exit code of a process is conventionally represented by an 8-bit integer ranging
   * from 0 to 255. A value of 0 indicates a normal termination, while non-zero values indicate
   * abnormal termination with custom-defined meanings by the process. Typically, non-zero exit
   * codes are considered as process execution failures or errors. Some common exit code conventions
   * in Linux include:
   *
   * <pre>
   * 1: General errors
   * 2: Invalid command line parameters
   * 126: Cannot execute the command
   * 127: Command not found
   * 128 + N: Exit due to receiving signal N (e.g., 128 + 2 indicates exit on receiving SIGINT signal)
   * </pre>
   * <p>
   * In Windows, the exit code of a process is represented by a 32-bit unsigned integer. Unlike
   * Linux, Windows exit codes typically correspond to predefined system error codes, indicating the
   * execution result of the process. These error codes can be found in the WinError.h header file.
   * For example:
   *
   * <pre>
   * 0: Normal process termination
   * 1: General errors
   * 2: File not found
   * 3: Path not found
   * Other system error codes: Specific error codes indicating the reason for process execution failure
   * </pre>
   * <p>
   * <p>
   * Note:
   * <p>
   *
   * @see ProcessCommand
   */
  public int exitCode;

  public String failure;

  private CommandResult(Builder builder) {
    success = builder.success;
    results = builder.results;
    exitCode = builder.exitCode;
    failure = builder.failure;
  }

  public boolean getSuccess() {
    return success;
  }

  public List<String> getResults() {
    return results;
  }

  public int getExitCode() {
    return exitCode;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {

    private boolean success;
    private List<String> results;
    private int exitCode;
    public String failure;

    private Builder() {
    }

    public Builder setSuccess(boolean success) {
      this.success = success;
      return this;
    }

    public Builder setResults(List<String> results) {
      this.results = results;
      return this;
    }

    public Builder setExitCode(int exitCode) {
      this.exitCode = exitCode;
      return this;
    }

    public Builder failure(String failure) {
      this.failure = failure;
      return this;
    }

    public CommandResult build() {
      return new CommandResult(this);
    }
  }
}
