package cloud.xcan.angus.queue.core.service;

import cloud.xcan.angus.queue.core.model.DeadLetterData;
import cloud.xcan.angus.queue.core.spi.RepositoryAdapter;
import cloud.xcan.angus.queue.core.spi.SoftDeleteDlqSupport;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultQueueAdminService implements QueueAdminService {

  private final RepositoryAdapter adapter;
  private final AuditLogger auditLogger; // optional
  private final boolean softDeleteDlq;

  public DefaultQueueAdminService(RepositoryAdapter adapter) {
    this(adapter, null, false);
  }

  public DefaultQueueAdminService(RepositoryAdapter adapter, AuditLogger auditLogger,
      boolean softDeleteDlq) {
    this.adapter = adapter;
    this.auditLogger = auditLogger;
    this.softDeleteDlq = softDeleteDlq;
  }

  private void log(String action, String topic, int affected, String detail) {
    if (auditLogger != null) {
      auditLogger.adminAction(action, topic, affected, detail);
    }
  }

  @Override
  public Map<String, Object> topicStats(String topic) {
    Map<String, Object> stats = new HashMap<>();
    Map<Integer, Long> statusCounts = new HashMap<>();
    for (Object[] row : adapter.countByStatus(topic)) {
      Integer status = ((Number) row[0]).intValue();
      Long cnt = ((Number) row[1]).longValue();
      statusCounts.put(status, cnt);
    }
    stats.put("statusCounts", statusCounts);
    stats.put("dlqCount", adapter.deadLetterCountByTopic(topic));

    Map<Integer, Long> perPartition = new HashMap<>();
    for (Object[] row : adapter.readyCountPerPartition(topic)) {
      Integer p = ((Number) row[0]).intValue();
      Long cnt = ((Number) row[1]).longValue();
      perPartition.put(p, cnt);
    }
    stats.put("readyPerPartition", perPartition);
    log("stats", topic, 0, "");
    return stats;
  }

  @Override
  public int reclaimExpired(int limit) {
    int n = adapter.reclaimExpiredLeases(limit);
    log("reclaimExpired", null, n, "limit=" + limit);
    return n;
  }

  @Override
  public int purgeDone(String topic, Instant before) {
    int n = adapter.purgeDoneBefore(topic, before);
    log("purgeDone", topic, n, "before=" + before);
    return n;
  }

  @Override
  public int purgeDeadLetters(String topic) {
    int n;
    if (softDeleteDlq && adapter instanceof SoftDeleteDlqSupport s) {
      n = s.softDeleteDeadLettersByTopic(topic);
      log("softDeleteDLQ", topic, n, "");
    } else {
      n = adapter.purgeDeadLettersByTopic(topic);
      log("purgeDLQ", topic, n, "");
    }
    return n;
  }

  @Override
  public int replayFromDeadLetter(String topic, int limit) {
    List<DeadLetterData> list = adapter.findDeadLettersByTopicLimit(topic, limit);
    int moved = 0;
    for (DeadLetterData d : list) {
      adapter.saveRecoveredMessage(d);
      adapter.deleteDeadLetterById(d.getId());
      moved++;
    }
    log("replayDLQ", topic, moved, "limit=" + limit);
    return moved;
  }
}
