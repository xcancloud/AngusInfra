package cloud.xcan.angus.idgen;

import static cloud.xcan.angus.idgen.utils.NetUtils.getLocalAddress;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.RandomUtils.nextInt;

import cloud.xcan.angus.api.pojo.instance.InstanceType;
import cloud.xcan.angus.idgen.dao.InstanceRepo;
import cloud.xcan.angus.idgen.entity.Instance;
import cloud.xcan.angus.idgen.uid.InstanceIdAssigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents an implementation of {@link InstanceIdAssigner}, the worker pk will be discarded after
 * assigned to the UidGenerator
 *
 * @author XiaoLong Liu
 */
public class DisposableInstanceIdAssigner implements InstanceIdAssigner {

  private static final Logger LOGGER = LoggerFactory.getLogger(DisposableInstanceIdAssigner.class);

  private static final String ENV_KEY_HOST = "HOST";
  private static final String ENV_KEY_PORT = "HTTP_PORT";
  private static final String ENV_KEY_ENV = "RUNTIME_ENV";

  /**
   * Container host & port
   */
  public static String HOST = "";
  public static String HTTP_PORT = "";
  public static String RUNTIME_ENV = "";

  /**
   * Whether is docker
   */
  private static boolean IS_DOCKER;

  static {
    retrieveFromEnv();
  }

  private InstanceRepo instanceRepository;

  public DisposableInstanceIdAssigner(InstanceRepo workerNodeRepository) {
    this.instanceRepository = workerNodeRepository;
  }

  /**
   * Whether a docker
   */
  public static boolean isDocker() {
    return isNotEmpty(RUNTIME_ENV) && RUNTIME_ENV.equalsIgnoreCase(InstanceType.CONTAINER.name());
  }

  /**
   * Retrieve host & port from environment
   */
  private static void retrieveFromEnv() {
    // retrieve host & port from environment
    RUNTIME_ENV = System.getenv(ENV_KEY_ENV);
    HOST = System.getenv(ENV_KEY_HOST);
    HTTP_PORT = System.getenv(ENV_KEY_PORT);

    LOGGER.debug("IdGen configuration runtime env: {} ", RUNTIME_ENV);
    LOGGER.debug("IdGen configuration host: {} ", HOST);
    LOGGER.debug("IdGen configuration http port: {} ", HOST);

    // Find both host & port from environment
    if (isEmpty(RUNTIME_ENV) || isEmpty(HOST) || isEmpty(HTTP_PORT)) {
      LOGGER.info("IdGen instance is not configured in system environment variables");
    }
  }

  /**
   * Assign worker pk base on database.<p> If there is host name & port in the environment, we
   * considered that the node runs in Docker container<br> Otherwise, the node runs on an actual
   * machine.
   *
   * @return assigned worker pk
   */
  @Override
  @Transactional
  public Long assignInstanceIdByParam(String host, String port, InstanceType type) {
    Instance inst = instanceRepository.findByHostAndPort(host, port);
    if (nonNull(inst)) {
      if (instanceRepository.incrementId(inst.getPk(), inst.getId()) <= 0) {
        LOGGER.warn("Assign instance incr Id fail");
        return null;
      }
      return inst.getId() + 1;
    }

    // add instance for new
    Instance newInst = buildInstance(host, port, type);
    instanceRepository.save(newInst);
    return newInst.getId();
  }

  @Override
  @Transactional
  public Long assignInstanceIdByEnv() {
    String host = isEmpty(HOST) ? getLocalAddress() : HOST;
    String port = isEmpty(HTTP_PORT) ? currentTimeMillis() + "-" + nextInt(0, 100000) : HTTP_PORT;
    InstanceType type = isDocker() ? InstanceType.CONTAINER : InstanceType.HOST;
    return assignInstanceIdByParam(host, port, type);
  }

  /**
   * Build instance entity by HOST and PORT
   */
  private Instance buildInstance(String host, String port, InstanceType type) {
    return new Instance().setId(1L)
        .setHost(host)
        .setPort(port)
        .setInstanceType(type);
  }

}
