package cloud.xcan.angus.validator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;

/**
 * @author liuxiaolong
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "xcan.validator", ignoreUnknownFields = false)
@SuppressWarnings("squid:S1068")
public class ValidatorProperties {

  private Boolean enabled = false;

  private Set<String> defaultMessages = new HashSet<>(
      Arrays.asList("classpath:/i18n/messages-infra-validator",
          "classpath:/i18n/messages-infra-spec", "classpath:/i18n/messages-infra-remote"));

  private Set<String> extraMessages;

  @NotNull
  public String[] getAllI18ns() {
    Set<String> i18ns = getDefaultMessages();
    if (!CollectionUtils.isEmpty(getExtraMessages())) {
      i18ns.addAll(getExtraMessages());
    }
    return i18ns.toArray(new String[0]);
  }

}
