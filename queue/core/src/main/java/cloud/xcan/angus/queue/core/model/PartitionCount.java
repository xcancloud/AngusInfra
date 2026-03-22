package cloud.xcan.angus.queue.core.model;

/**
 * Typed projection for per-partition ready-message counts returned by
 * {@code RepositoryAdapter#readyCountPerPartition}.
 */
public record PartitionCount(int partitionId, long count) {

}
