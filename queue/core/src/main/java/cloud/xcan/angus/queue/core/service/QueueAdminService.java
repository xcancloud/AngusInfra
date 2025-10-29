package cloud.xcan.angus.queue.core.service;

import java.time.Instant;
import java.util.Map;

public interface QueueAdminService {

  Map<String, Object> topicStats(String topic);

  int reclaimExpired(int limit);

  int purgeDone(String topic, Instant before);

  int purgeDeadLetters(String topic);

  int replayFromDeadLetter(String topic, int limit);
}

