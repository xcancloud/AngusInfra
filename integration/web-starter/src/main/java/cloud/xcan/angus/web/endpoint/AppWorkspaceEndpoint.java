package cloud.xcan.angus.web.endpoint;

import cloud.xcan.sdf.core.app.AppWorkspace;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

@Endpoint(id = "appworkspace")
public class AppWorkspaceEndpoint {

  private final AppWorkspace appWorkspace;

  public AppWorkspaceEndpoint(AppWorkspace appWorkspace) {
    this.appWorkspace = appWorkspace;
  }

  @ReadOperation
  public AppWorkspace workspace() {
    return appWorkspace;
  }

}
