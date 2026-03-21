package cloud.xcan.angus.persistence.jpa.repository.app;

import java.time.LocalDateTime;

public interface AppAuth {

  Long getAppId();

  String getAppCode();

  String getVersion();

  LocalDateTime getOpenDate();

  LocalDateTime getExpirationDate();

}
