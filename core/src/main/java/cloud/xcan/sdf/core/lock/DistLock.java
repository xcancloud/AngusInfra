
package cloud.xcan.sdf.core.lock;

/**
 * 可靠性 - 为了确保分布式锁可用，至少要确保锁的实现同时满足以下四个条件：
 * <pre>
 * 1、互斥性：在任意时刻，只有一个客户端能持有锁。
 * 2、不会发生死锁：即使有一个客户端在持有锁的期间崩溃而没有主动解锁，也能保证后续其他客户端能加锁。
 * 3、具有容错性：只要大部分的服务节点正常运行，客户端就可以加锁和解锁。
 * 4、解铃还须系铃人：加锁和解锁必须是同一个客户端，客户端自己不能把别人加的锁给解了。
 * </pre>
 */
public interface DistLock {

  /**
   * 尝试获取分布式锁
   *
   * @param lockKey    锁
   * @param ownerId    持有者标识
   * @param expireTime 超期时间、单位毫秒（需要根据业务合理设置，防止业务未处理完锁超时被自动清除）
   * @return 是否获取成功
   */
  boolean tryGetLock(String lockKey, String ownerId, int expireTime);

  /**
   * 释放分布式锁
   *
   * @param lockKey 锁
   * @param ownerId 持有者标识
   * @return 是否释放成功
   */
  boolean releaseLock(String lockKey, String ownerId);

}
