package cloud.xcan.angus.idgen.uid;


import cloud.xcan.angus.idgen.uid.impl.DefaultUidGenerator;
import cloud.xcan.angus.api.pojo.instance.InstanceType;

/**
 * Represents a instance pk assigner for {@link DefaultUidGenerator}
 */
public interface InstanceIdAssigner {

  /**
   * Assign instance pk for {@link DefaultUidGenerator}
   *
   * @return assigned instance pk
   */
  Long assignInstanceIdByEnv();


  /**
   * Assign instance pk for {@link DefaultUidGenerator}
   *
   * @return assigned instance pk
   */
  Long assignInstanceIdByParam(String host, String port, InstanceType type);

}
