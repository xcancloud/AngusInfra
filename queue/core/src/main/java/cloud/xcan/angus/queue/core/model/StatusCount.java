package cloud.xcan.angus.queue.core.model;

/**
 * Typed projection for per-status message counts returned by
 * {@code RepositoryAdapter#countByStatus}.
 */
public record StatusCount(int status, long count) {

}
