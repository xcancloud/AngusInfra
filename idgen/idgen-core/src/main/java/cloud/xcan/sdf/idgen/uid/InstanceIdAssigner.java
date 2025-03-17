
package cloud.xcan.sdf.idgen.uid;


import cloud.xcan.sdf.api.pojo.instance.InstanceType;
import cloud.xcan.sdf.idgen.uid.impl.DefaultUidGenerator;

/**
 * Represents a instance pk assigner for {@link DefaultUidGenerator}
 *
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
