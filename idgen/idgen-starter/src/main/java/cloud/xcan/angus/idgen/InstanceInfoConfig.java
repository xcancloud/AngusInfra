package cloud.xcan.angus.idgen;

import cloud.xcan.angus.api.pojo.instance.InstanceType;
import cloud.xcan.angus.idgen.utils.NetUtils;
import jakarta.annotation.Resource;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;

public class InstanceInfoConfig {

  @Resource
  private ServerProperties serverProperties;

  /**
   * Type of CONTAINER: HostName, HOST : IP.
   */
  private String host;

  /**
   * Type of CONTAINER: Port, HOST : Timestamp + Random(0-10000)
   */
  private String port;

  /**
   * type of {@link InstanceType}
   */
  @Value("${info.app.runtime}")
  private InstanceType env;

  public InstanceInfoConfig() {
  }

  public ServerProperties getServerProperties() {
    return serverProperties;
  }

  public void setServerProperties(
      ServerProperties serverProperties) {
    this.serverProperties = serverProperties;
  }

  public String getHost() {
    return Objects.isNull(serverProperties.getAddress()) ? NetUtils.getLocalAddress().toString()
        : serverProperties.getAddress().toString();
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getPort() {
    return String.valueOf(serverProperties.getPort());
  }

  public void setPort(String port) {
    this.port = port;
  }

  public InstanceType getEnv() {
    return env;
  }

  public void setEnv(InstanceType env) {
    this.env = env;
  }
}
