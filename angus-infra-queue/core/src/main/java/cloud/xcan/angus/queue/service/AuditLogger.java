package cloud.xcan.angus.queue.service;

public interface AuditLogger {

  void adminAction(String action, String topic, int affected, String detail);
}

