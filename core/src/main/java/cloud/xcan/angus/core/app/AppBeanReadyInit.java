package cloud.xcan.angus.core.app;


import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.core.spring.SpringContextHolder;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppBeanReadyInit implements ApplicationInit {

  @Override
  public void init() throws Exception {
    Map<String, AppBeanReady> beanReadyMap = Objects.requireNonNull(SpringContextHolder.getCtx(),
            "ApplicationContext must be set before ApplicationInit")
        .getBeansOfType(AppBeanReady.class);
    if (isNotEmpty(beanReadyMap)) {
      for (AppBeanReady register : beanReadyMap.values()) {
        register.ready();
      }
    }
  }

  @Override
  public int getOrder() {
    return -200;
  }
}
