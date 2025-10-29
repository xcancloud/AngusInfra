package cloud.xcan.angus.plugin.api;

import java.util.List;

public interface RestfulPlugin extends Plugin {
    List<Class<?>> getControllerClasses();

    default String getApiPrefix() {
        return "/api/plugins/" + getId();
    }

    default boolean enableApiDoc() {
        return true;
    }
}

