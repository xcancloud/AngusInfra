package cloud.xcan.angus.queue.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jAuditLogger implements AuditLogger {

  private static final Logger log = LoggerFactory.getLogger(Slf4jAuditLogger.class);

  @Override
  public void adminAction(String action, String topic, int affected, String detail) {
    if (log.isInfoEnabled()) {
      log.info("queue-admin action={}, topic={}, affected={}, detail={}", action, topic, affected,
          detail);
    }
  }
}

