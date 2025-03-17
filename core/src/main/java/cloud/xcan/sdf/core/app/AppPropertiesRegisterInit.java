package cloud.xcan.sdf.core.app;


import cloud.xcan.sdf.core.spring.SpringContextHolder;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppPropertiesRegisterInit implements ApplicationInit {

  @Override
  public void init() throws Exception {
    Map<String, AppPropertiesRegister> propertieRegisters = SpringContextHolder.getCtx()
        .getBeansOfType(AppPropertiesRegister.class);
    if (ObjectUtils.isNotEmpty(propertieRegisters)) {
      for (AppPropertiesRegister register : propertieRegisters.values()) {
        register.register0();
      }
    }
  }

  @Override
  public int getOrder() {
    return -100;
  }

}
