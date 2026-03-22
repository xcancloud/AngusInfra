package cloud.xcan.angus.core.job;

@FunctionalInterface
public interface SyncTaskCallback {

  /**
   * Business logic execution method.
   */
  void doInSync() throws Exception;
}
