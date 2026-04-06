package cloud.xcan.angus.queue.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface QueueAdminService {

  List<String> listTopics();

  Map<String, Object> topicStats(String topic);

  int reclaimExpired(int limit);

  int purgeDone(String topic, Instant before);

  int purgeDeadLetters(String topic);

  int replayFromDeadLetter(String topic, int limit);
}

