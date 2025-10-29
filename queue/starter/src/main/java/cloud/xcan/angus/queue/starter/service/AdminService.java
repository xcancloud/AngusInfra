package cloud.xcan.angus.queue.starter.service;

import cloud.xcan.angus.queue.core.entity.DeadLetterEntity;
import cloud.xcan.angus.queue.core.entity.MessageEntity;
import cloud.xcan.angus.queue.starter.repository.DeadLetterRepository;
import cloud.xcan.angus.queue.starter.repository.MessageRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminService {

  private final MessageRepository messageRepository;
  private final DeadLetterRepository deadLetterRepository;

  public AdminService(MessageRepository messageRepository,
      DeadLetterRepository deadLetterRepository) {
    this.messageRepository = messageRepository;
    this.deadLetterRepository = deadLetterRepository;
  }

  public Map<String, Object> topicStats(String topic) {
    Map<String, Object> stats = new HashMap<>();
    Map<Integer, Long> statusCounts = new HashMap<>();
    for (Object[] row : messageRepository.countByStatus(topic)) {
      Integer status = ((Number) row[0]).intValue();
      Long cnt = ((Number) row[1]).longValue();
      statusCounts.put(status, cnt);
    }
    stats.put("statusCounts", statusCounts);
    stats.put("dlqCount", deadLetterRepository.countByTopic(topic));

    Map<Integer, Long> perPartition = new HashMap<>();
    for (Object[] row : messageRepository.readyCountPerPartition(topic)) {
      Integer p = ((Number) row[0]).intValue();
      Long cnt = ((Number) row[1]).longValue();
      perPartition.put(p, cnt);
    }
    stats.put("readyPerPartition", perPartition);
    return stats;
  }

  public int reclaimExpired(int limit) {
    return messageRepository.reclaimExpiredLeases(limit);
  }

  public int purgeDone(String topic, Instant before) {
    return messageRepository.purgeDoneBefore(topic, before);
  }

  public int purgeDeadLetters(String topic) {
    return deadLetterRepository.hardDeleteByTopic(topic);
  }

  public int replayFromDeadLetter(String topic, int limit) {
    List<DeadLetterEntity> list = deadLetterRepository.findByTopicLimit(topic, limit);
    int moved = 0;
    for (DeadLetterEntity d : list) {
      MessageEntity m = new MessageEntity();
      m.setTopic(d.getTopic());
      m.setPartitionId(d.getPartitionId());
      m.setPriority(0);
      m.setPayload(d.getPayload());
      m.setHeaders(d.getHeaders());
      m.setStatus(0);
      m.setVisibleAt(Instant.now());
      m.setLeaseOwner(null);
      m.setLeaseUntil(null);
      m.setAttempts(0);
      m.setMaxAttempts(16);
      m.setCreatedAt(Instant.now());
      m.setUpdatedAt(Instant.now());
      messageRepository.save(m);
      deadLetterRepository.deleteById(d.getId());
      moved++;
    }
    return moved;
  }
}
