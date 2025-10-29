package cloud.xcan.angus.plugin.api;

import java.util.List;

/**
 * Extension of {@link Plugin} for plugins that expose Spring MVC controllers.
 * <p>
 * Plugins implementing this interface should return the controller classes that
 * will be registered dynamically by the host when the plugin is loaded.
 */
public interface RestfulPlugin extends Plugin {

    /**
     * Return controller classes (Spring MVC controllers) that should be registered
     * under the host application context for this plugin.
     *
     * @return list of controller classes
     */
    List<Class<?>> getControllerClasses();

    /**
     * Return the API prefix used for this plugin's controllers. Default implementation
     * uses /api/plugins/{pluginId} so endpoints are namespaced by plugin id.
     *
     * @return API prefix path
     */
    default String getApiPrefix() {
        return "/api/plugins/" + getId();
    }

    /**
     * When true, plugin API documentation may be exposed (by default true). Hosts can
     * respect this flag when aggregating OpenAPI docs.
     *
     * @return true to enable API doc exposure
     */
    default boolean enableApiDoc() {
        return true;
    }
}
