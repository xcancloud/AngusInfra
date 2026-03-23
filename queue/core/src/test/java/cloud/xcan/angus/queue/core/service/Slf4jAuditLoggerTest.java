package cloud.xcan.angus.queue.core.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class Slf4jAuditLoggerTest {

  @BeforeAll
  static void enableSlf4jSimple() {
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
    System.setProperty(
        "org.slf4j.simpleLogger.log.cloud.xcan.angus.queue.core.service.Slf4jAuditLogger",
        "info");
  }

  @Test
  void adminActionDoesNotThrow() {
    Slf4jAuditLogger logger = new Slf4jAuditLogger();
    logger.adminAction("purgeDLQ", "topic-a", 3, "detail");
  }
}
