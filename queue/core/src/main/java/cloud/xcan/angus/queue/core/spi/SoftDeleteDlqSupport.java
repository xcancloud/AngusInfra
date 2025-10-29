package cloud.xcan.angus.queue.core.spi;

/**
 * Optional capability for adapters that support soft-deleting DLQ records by topic.
 */
public interface SoftDeleteDlqSupport {

  int softDeleteDeadLettersByTopic(String topic);
}

