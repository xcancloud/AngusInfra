package cloud.xcan.angus.core.app;


import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppBeanReadyInit implements ApplicationInit {

  @Override
  public void init() throws Exception {
    Map<String, AppBeanReady> beanReadyMap = SpringContextHolder.getCtx()
        .getBeansOfType(AppBeanReady.class);
    if (ObjectUtils.isNotEmpty(beanReadyMap)) {
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
