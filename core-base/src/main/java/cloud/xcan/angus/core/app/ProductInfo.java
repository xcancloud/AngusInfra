package cloud.xcan.angus.core.app;

import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ProductInfo {

  private Long id;

  private Long appId;

  private String code;

  private String name;

  private String type;

  private String version;

  private String editionType;

  private String base64Icon;

  // @DoInFuture("I18n support")
  private LinkedHashSet<String> tags;

  // @DoInFuture("I18n support")
  private String introduction;

  // @DoInFuture("I18n support")
  private String information;

  // @DoInFuture("I18n support")
  private LinkedHashSet<String> features;

  private Boolean charge;

}
