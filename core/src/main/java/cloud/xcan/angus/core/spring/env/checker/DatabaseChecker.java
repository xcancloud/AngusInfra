package cloud.xcan.angus.core.spring.env.checker;

import static cloud.xcan.angus.api.enums.SupportedDbType.MYSQL;
import static cloud.xcan.angus.api.enums.SupportedDbType.POSTGRES;
import static cloud.xcan.angus.spec.utils.ObjectUtils.getCauseMessage;

import cloud.xcan.angus.api.enums.SupportedDbType;
import cloud.xcan.angus.core.jdbc.ConnectionFactory;
import cloud.xcan.angus.remote.message.SysException;
import java.sql.Connection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatabaseChecker {

  public static Connection checkConnection(SupportedDbType type, String host, int port, String name,
      String user, String password) {
    try {
      if (MYSQL.equals(type)) {
        return ConnectionFactory.mysql(host, port, name, user, password);
      } else if (POSTGRES.equals(type)) {
        return ConnectionFactory.postgres(host, port, name, user, password);
      } else {
        throw new IllegalArgumentException("Unsupported database type: " + type);
      }
    } catch (Exception e) {
      String message = getCauseMessage(e);
      log.error("Configure and get database connection error: {}", message);
      throw SysException.of("Configure and get database connection error: " + message);
    }
  }

}
