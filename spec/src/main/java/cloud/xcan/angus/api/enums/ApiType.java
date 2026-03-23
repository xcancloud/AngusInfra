package cloud.xcan.angus.api.enums;

import cloud.xcan.angus.spec.experimental.Value;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public enum ApiType implements Value<String> {

  API, OPEN_API, OPEN_API_2P, DOOR_API, PUB_API, VIEW, PUB_VIEW;

  public static final Map<ApiType, String> API_TYPE_MAP =
      Map.of(API, "/api/", OPEN_API, "/openapi/", OPEN_API_2P, "/openapi2p/", DOOR_API,
          "/innerapi/",
          PUB_API, "/pubapi/", VIEW, "/view/", PUB_VIEW, "/pubview/");

  /**
   * Prefix entries ordered by path length descending so that a longer path (e.g. {@code /openapi2p/})
   * wins over a shorter shared prefix when both could match.
   */
  private static final List<Map.Entry<ApiType, String>> API_TYPE_PREFIXES_SORTED;

  static {
    List<Map.Entry<ApiType, String>> list = new ArrayList<>(API_TYPE_MAP.entrySet());
    list.sort(Comparator.<Map.Entry<ApiType, String>>comparingInt(e -> e.getValue().length())
        .reversed());
    API_TYPE_PREFIXES_SORTED = List.copyOf(list);
  }

  /**
   * Resolves the API type from the request URI prefix.
   *
   * @param uri request path or full URI, non-null
   * @return matching type, or {@link #API} when no configured prefix matches
   */
  public static ApiType findByUri(String uri) {
    Objects.requireNonNull(uri, "uri");
    for (Map.Entry<ApiType, String> e : API_TYPE_PREFIXES_SORTED) {
      if (uri.startsWith(e.getValue())) {
        return e.getKey();
      }
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
    return this.equals(API) || this.equals(OPEN_API) || this.equals(OPEN_API_2P);
  }

  @Override
  public String getValue() {
    return this.name();
  }

}
