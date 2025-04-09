package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.ValueObject;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumValueMessage;
import java.util.Map;

@EndpointRegister
public enum ApiType implements ValueObject<ApiType>, EnumValueMessage<String> {

  API, OPEN_API, OPEN_API_2P, DOOR_API, PUB_API, VIEW, PUB_VIEW;

  public static final Map<ApiType, String> API_TYPE_MAP =
      Map.of(API, "/api/", OPEN_API, "/openapi/", OPEN_API_2P, "/openapi2p/", DOOR_API, "/innerapi/",
          PUB_API, "/pubapi/", VIEW, "/view/", PUB_VIEW, "/pubview/");

  public static ApiType findByUri(String uri) {
    if (uri.startsWith(API_TYPE_MAP.get(API))) {
      return API;
    } else if (uri.startsWith(API_TYPE_MAP.get(OPEN_API))) {
      return OPEN_API;
    } else if (uri.startsWith(API_TYPE_MAP.get(OPEN_API_2P))) {
      return OPEN_API_2P;
    } else if (uri.startsWith(API_TYPE_MAP.get(DOOR_API))) {
      return DOOR_API;
    } else if (uri.startsWith(API_TYPE_MAP.get(PUB_API))) {
      return PUB_API;
    } else if (uri.startsWith(API_TYPE_MAP.get(VIEW))) {
      return VIEW;
    } else if (uri.startsWith(API_TYPE_MAP.get(PUB_VIEW))) {
      return PUB_VIEW;
    }
    return API;
  }

  public boolean isDoorTypeApi() {
    return this.equals(DOOR_API);
  }

  public boolean isUserTypeApi() {
    return this.equals(API) || this.equals(VIEW);
  }

  public boolean isPubTypeApi() {
    return this.equals(PUB_API) || this.equals(PUB_VIEW);
  }

  public boolean isAuthApi() {
    return this.equals(API) || this.equals(VIEW) || this.equals(OPEN_API)
        || this.equals(OPEN_API_2P);
  }

  @Override
  public String getValue() {
    return this.name();
  }

  @Override
  public String getMessage() {
    return API_TYPE_MAP.get(this).substring(0, API_TYPE_MAP.get(this).length() - 1);
  }

}
