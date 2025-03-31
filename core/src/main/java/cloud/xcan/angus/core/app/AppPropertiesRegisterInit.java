package cloud.xcan.angus.core.app;


import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.spring.SpringContextHolder;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppPropertiesRegisterInit implements ApplicationInit {

  @Override
  public void init() throws Exception {
    Map<String, AppPropertiesRegister> propertiesRegisters = SpringContextHolder.getCtx()
        .getBeansOfType(AppPropertiesRegister.class);
    if (isNotEmpty(propertiesRegisters)) {
      for (AppPropertiesRegister register : propertiesRegisters.values()) {
        register.register0();
      }
    }
  }

  @Override
  public int getOrder() {
    return -100;
  }

}
