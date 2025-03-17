package cloud.xcan.sdf.api.pojo.node;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_AGENT_PORT;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Used by angusctrl
 */
@Setter
@Getter
@Accessors(chain = true)
public class NodeInfo {

  private Long id;

  private String name;

  private String ip;

  private String publicIp;

  private String regionId;

  private String domain;

  private NodeSpecData spec;

  private int agentPort = DEFAULT_AGENT_PORT;

}
