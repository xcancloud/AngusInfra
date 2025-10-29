package cloud.xcan.angus.plugin.examples.jenkins;

import cloud.xcan.angus.plugin.api.RestfulPlugin;
import cloud.xcan.angus.plugin.model.PluginState;

import java.util.List;

public class JenkinsPlugin implements RestfulPlugin {

    private PluginState state = PluginState.INITIALIZED;

    @Override
    public String getId() {
        return "angus-plugin-jenkins";
    }

    @Override
    public String getName() {
        return "Angus Jenkins Example Plugin";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Example plugin to trigger and query Jenkins jobs via REST.";
    }

    @Override
    public String getAuthor() {
        return "Angus Team";
    }

    @Override
    public void initialize(cloud.xcan.angus.plugin.api.PluginContext context) {
        this.state = PluginState.INITIALIZED;
    }

    @Override
    public void start() {
        this.state = PluginState.STARTED;
    }

    @Override
    public void stop() {
        this.state = PluginState.STOPPED;
    }

    @Override
    public void destroy() {
        this.state = PluginState.UNLOADING;
    }

    @Override
    public PluginState getState() {
        return this.state;
    }

    @Override
    public List<Class<?>> getControllerClasses() {
        return List.of(JenkinsController.class);
    }
}

