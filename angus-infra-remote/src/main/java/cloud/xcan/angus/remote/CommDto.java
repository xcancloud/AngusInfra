package cloud.xcan.angus.remote;

import cloud.xcan.angus.remote.search.SearchCriteria;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CommDto implements Serializable {

  /**
   * @see SearchCriteria#INFO_SCOPE_KEY
   */
  @Schema(description = "Query information scope", hidden = true)
  public InfoScope infoScope;

  //  /**
  //   * Your tenant ID.
  //   */
  //  @ID
  //  @Schema(description = "Your tenant ID")
  //  private Long tenantId;

  //  /**
  //   * Locale:{language}-{country}.
  //   *
  //   * <p>eg: zh-CN</p>
  //   *
  //   * @see java.util.Locale
  //   */
  //  @Length(max = 16)
  //  @CharConstant(array = {"zh-CN","en"})
  //  @Schema(description = "Locale. eg:zh-CN", allowableValues = "zh-CN,en")
  //  private String locale;

  //  /**
  //   * Time zone.
  //   *
  //   * <p>e,g. GMT+8</p>
  //   */
  //  @Length(max = 16, message = "{sdf.timezone.invalid}")
  //  ("Time zone. eg:GMT+8")
  //  private String timezone;

  //  /**
  //   * Reserved extended attributes.
  //   *
  //   * <p>The content of the extended attribute is agreed upon by the consumer/provider on its
  //   * own</p>
  //   */
  //  @MapCharLength
  //  @Size(max = MAX_QUERY_EXT_SIZE)
  //  ("Extended attributes, Max 200 key/value")
  //  private Map<String, Object> ext = new HashMap<>();

}
