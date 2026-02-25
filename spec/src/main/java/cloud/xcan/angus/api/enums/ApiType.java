package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;
import java.util.Map;

public enum ApiType implements Value<String> {

  API, OPEN_API, OPEN_API_2P, INNER_API, PUB_API, VIEW, PUB_VIEW;

  public static final Map<ApiType, String> API_TYPE_MAP =
      Map.of(
          API, "/api/", OPEN_API, "/openapi/", OPEN_API_2P, "/openapi2p/",
          INNER_API, "/innerapi/", PUB_API, "/pubapi/",
          VIEW, "/view/", PUB_VIEW, "/pubview/"
      );

  public static ApiType findByUri(String uri) {
    if (uri.startsWith(API_TYPE_MAP.get(API))) {
      return API;
    } else if (uri.startsWith(API_TYPE_MAP.get(OPEN_API))) {
      return OPEN_API;
    } else if (uri.startsWith(API_TYPE_MAP.get(OPEN_API_2P))) {
      return OPEN_API_2P;
    } else if (uri.startsWith(API_TYPE_MAP.get(INNER_API))) {
      return INNER_API;
    } else if (uri.startsWith(API_TYPE_MAP.get(PUB_API))) {
      return PUB_API;
    } else if (uri.startsWith(API_TYPE_MAP.get(VIEW))) {
      return VIEW;
    } else if (uri.startsWith(API_TYPE_MAP.get(PUB_VIEW))) {
      return PUB_VIEW;
    }
    return API;
  }

  public boolean isInnerTypeApi() {
    return this.equals(INNER_API);
  }

  public boolean isUserTypeApi() {
    return this.equals(API) || this.equals(VIEW);
  }

  public boolean isPubTypeApi() {
    return this.equals(PUB_API) || this.equals(PUB_VIEW);
  }

  public boolean isViewTypeApi() {
    return this.equals(VIEW) || this.equals(PUB_VIEW);
  }

  public boolean isAuthApi() {
    return this.equals(API) || this.equals(OPEN_API) || this.equals(OPEN_API_2P);
  }

  @Override
  public String getValue() {
    return this.name();
  }

}
