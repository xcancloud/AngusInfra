package cloud.xcan.angus.core.app.check;

import java.time.LocalDateTime;
import org.springframework.lang.Nullable;

/**
 * View of an application subscription / license used for expiry checks.
 */
public interface AppAuth {

  Long getAppId();

  String getAppCode();

  String getVersion();

  /** When non-null and in the future, the app is not yet open for use. */
  @Nullable
  LocalDateTime getOpenDate();

  /** When non-null and in the past, the app license has expired. */
  @Nullable
  LocalDateTime getExpirationDate();
}
