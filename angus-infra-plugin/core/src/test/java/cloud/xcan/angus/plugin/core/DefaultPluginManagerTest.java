package cloud.xcan.angus.plugin.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cloud.xcan.angus.plugin.autoconfigure.PluginProperties;
import cloud.xcan.angus.plugin.event.PluginFailedEvent;
import cloud.xcan.angus.plugin.event.PluginLoadedEvent;
import cloud.xcan.angus.plugin.exception.PluginException;
import cloud.xcan.angus.plugin.model.PluginDescriptor;
import cloud.xcan.angus.plugin.model.PluginInfo;
import cloud.xcan.angus.plugin.model.PluginState;
import cloud.xcan.angus.plugin.store.PluginStore;
import cloud.xcan.angus.plugin.support.PluginJarBuilder;
import cloud.xcan.angus.plugin.support.SamplePluginController;
import cloud.xcan.angus.plugin.support.TestMinimalPlugin;
import cloud.xcan.angus.plugin.support.TestRestfulPlugin;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

class DefaultPluginManagerTest {

  @TempDir
  Path tempDir;

  private ApplicationContext applicationContext;
  private PluginProperties properties;
  private DynamicRestEndpointManager restEndpointManager;
  private DefaultPluginManager manager;

  @BeforeEach
  void setUp() {
    applicationContext = mock(ApplicationContext.class);
    properties = PluginProperties.forTesting(
        tempDir.resolve("plugins").toString(),
        tempDir.resolve("data").toString());
    properties.setAutoLoad(false);
    Map<String, Object> defaults = new HashMap<>();
    defaults.put("system.version", "1.0.0");
    properties.setDefaultConfiguration(defaults);
    restEndpointManager = mock(DynamicRestEndpointManager.class);
    when(restEndpointManager.registerPlugin(any(), any(), any())).thenReturn(true);
    when(restEndpointManager.getPluginEndpoints(any())).thenReturn(List.of());
    manager = new DefaultPluginManager(applicationContext, properties, restEndpointManager);
  }

  @AfterEach
  void tearDown() {
    TestMinimalPlugin.clearFlags();
  }

  @Test
  void initializeCreatesDirectoriesAndSkipsAutoLoad() {
    manager.initialize();
    assertTrue(Files.isDirectory(Path.of(properties.getDirectory())));
    assertTrue(Files.isDirectory(Path.of(properties.getDataDirectory())));
  }

  @Test
  void loadAllWhenJarDirectoryMissingDoesNothing() {
    properties.replacePathsForTesting(tempDir.resolve("no-such-dir").toString(),
        tempDir.resolve("data").toString());
    manager.loadAllPlugins();
    assertTrue(manager.getAllPlugins().isEmpty());
  }

  @Test
  void initializeWithAutoLoadScansDirectory() throws Exception {
    properties.setAutoLoad(true);
    buildMinimalJar("auto.plugin", TestMinimalPlugin.class);
    manager.initialize();
    assertTrue(manager.hasPlugin("auto.plugin"));
  }

  @Test
  void loadPluginNullPathReturnsFalse() {
    assertFalse(manager.loadPlugin(null));
  }

  @Test
  void loadPluginReadsMetaInfDescriptor() throws Exception {
    PluginDescriptor d = baseDescriptor("meta.plugin", TestMinimalPlugin.class.getName());
    Path jar = tempDir.resolve("meta.jar");
    PluginJarBuilder.buildWithMetaInfPluginJson(jar, d, List.of(TestMinimalPlugin.class));
    assertTrue(manager.loadPlugin(jar));
    assertNotNull(manager.getPluginInfo("meta.plugin"));
  }

  @Test
  void loadPluginDuplicateReturnsFalse() throws Exception {
    Path jar = buildMinimalJar("dup.plugin", TestMinimalPlugin.class);
    assertTrue(manager.loadPlugin(jar));
    assertFalse(manager.loadPlugin(jar));
  }

  @Test
  void loadPluginInvalidDescriptorRejected() throws Exception {
    PluginDescriptor d = new PluginDescriptor();
    d.setId("");
    d.setPluginClass(TestMinimalPlugin.class.getName());
    Path jar = tempDir.resolve("bad.jar");
    PluginJarBuilder.build(jar, d, List.of(TestMinimalPlugin.class));
    assertFalse(manager.loadPlugin(jar));
  }

  @Test
  void loadPluginNonPluginClassRejected() throws Exception {
    PluginDescriptor d = baseDescriptor("badclass.plugin", "java.lang.String");
    Path jar = tempDir.resolve("str.jar");
    PluginJarBuilder.build(jar, d, List.of());
    assertFalse(manager.loadPlugin(jar));
  }

  @Test
  void loadPluginPublishesLoadedEvent() throws Exception {
    Path jar = buildMinimalJar("evt.plugin", TestMinimalPlugin.class);
    assertTrue(manager.loadPlugin(jar));
    ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
    verify(applicationContext, atLeastOnce()).publishEvent(captor.capture());
    assertTrue(captor.getAllValues().stream().anyMatch(PluginLoadedEvent.class::isInstance));
  }

  @Test
  void loadPluginInitFailurePublishesFailedEvent() throws Exception {
    TestMinimalPlugin.FAIL_INIT.set(true);
    Path jar = buildMinimalJar("fail.plugin", TestMinimalPlugin.class);
    assertFalse(manager.loadPlugin(jar));
    ArgumentCaptor<ApplicationEvent> captor = ArgumentCaptor.forClass(ApplicationEvent.class);
    verify(applicationContext, atLeastOnce()).publishEvent(captor.capture());
    assertTrue(captor.getAllValues().stream().anyMatch(PluginFailedEvent.class::isInstance));
  }

  @Test
  void loadRestfulPluginRegistersEndpoints() throws Exception {
    Path jar = buildRestfulJar("rest.plugin");
    assertTrue(manager.loadPlugin(jar));
    verify(restEndpointManager).registerPlugin(eq("rest.plugin"), any(), any());
  }

  @Test
  void startStopReloadUnload() throws Exception {
    Path jar = buildMinimalJar("life.plugin", TestMinimalPlugin.class);
    assertTrue(manager.loadPlugin(jar));
    assertTrue(manager.startPlugin("life.plugin"));
    assertEquals(PluginState.STARTED, manager.getPluginInfo("life.plugin").getState());
    assertTrue(manager.stopPlugin("life.plugin"));
    assertTrue(manager.reloadPlugin("life.plugin"));
    assertTrue(manager.unloadPlugin("life.plugin"));
    assertFalse(manager.hasPlugin("life.plugin"));
  }

  @Test
  void startUnknownReturnsFalse() throws PluginException {
    assertFalse(manager.startPlugin("nope"));
  }

  @Test
  void startAlreadyStartedIdempotent() throws Exception {
    Path jar = buildMinimalJar("started.plugin", TestMinimalPlugin.class);
    manager.loadPlugin(jar);
    manager.startPlugin("started.plugin");
    assertTrue(manager.startPlugin("started.plugin"));
  }

  @Test
  void startFailureSetsError() throws Exception {
    Path jar = buildMinimalJar("bstart.plugin", TestMinimalPlugin.class);
    manager.loadPlugin(jar);
    TestMinimalPlugin.FAIL_START.set(true);
    assertThrows(PluginException.class, () -> manager.startPlugin("bstart.plugin"));
    assertEquals(PluginState.ERROR, manager.getPluginInfo("bstart.plugin").getState());
  }

  @Test
  void stopUnknownReturnsFalse() throws PluginException {
    assertFalse(manager.stopPlugin("nope"));
  }

  @Test
  void stopFailureSetsError() throws Exception {
    Path jar = buildMinimalJar("bstop.plugin", TestMinimalPlugin.class);
    manager.loadPlugin(jar);
    manager.startPlugin("bstop.plugin");
    TestMinimalPlugin.FAIL_STOP.set(true);
    assertThrows(PluginException.class, () -> manager.stopPlugin("bstop.plugin"));
    assertEquals(PluginState.ERROR, manager.getPluginInfo("bstop.plugin").getState());
  }

  @Test
  void reloadUnknownReturnsFalse() {
    assertFalse(manager.reloadPlugin("missing"));
  }

  @Test
  void getPluginInfoNullWhenMissing() {
    assertNull(manager.getPluginInfo("missing"));
  }

  @Test
  void getAllPluginsIncludesFileMeta() throws Exception {
    Path jar = buildMinimalJar("filemeta.plugin", TestMinimalPlugin.class);
    manager.loadPlugin(jar);
    List<PluginInfo> all = manager.getAllPlugins();
    assertEquals(1, all.size());
    assertNotNull(all.get(0).getFilePath());
    assertNotNull(all.get(0).getFileSize());
  }

  @Test
  void installWithoutStoreWritesJarAndLoads() throws Exception {
    byte[] bytes = Files.readAllBytes(buildMinimalJar("ins.plugin", TestMinimalPlugin.class));
    manager.setPluginStore(null);
    assertTrue(manager.installPlugin("ins.plugin", bytes));
    assertTrue(manager.hasPlugin("ins.plugin"));
  }

  @Test
  void installInvalidIdThrows() {
    assertThrows(PluginException.class, () -> manager.installPlugin("bad/id", new byte[]{1}));
  }

  @Test
  void removeWithoutStoreDeletesJar() throws Exception {
    manager.setPluginStore(null);
    manager.installPlugin("rm.plugin", Files.readAllBytes(
        buildMinimalJar("rm.plugin", TestMinimalPlugin.class)));
    assertTrue(manager.removePlugin("rm.plugin", true));
    assertFalse(Files.exists(Path.of(properties.getDirectory(), "rm.plugin.jar")));
  }

  @Test
  void removeWhenNotLoadedReturnsFalse() throws Exception {
    assertFalse(manager.removePlugin("ghost", true));
  }

  @Test
  void loadAllFromPluginStore() throws Exception {
    PluginStore store = mock(PluginStore.class);
    Path jar = buildMinimalJar("stored.plugin", TestMinimalPlugin.class);
    when(store.listPluginIds()).thenReturn(List.of("stored.plugin"));
    when(store.getPluginPath("stored.plugin")).thenReturn(jar);
    manager.setPluginStore(store);
    manager.loadAllPlugins();
    assertTrue(manager.hasPlugin("stored.plugin"));
  }

  @Test
  void loadAllFromPluginStoreSwallowsPerPluginErrors() throws Exception {
    PluginStore store = mock(PluginStore.class);
    when(store.listPluginIds()).thenReturn(List.of("a", "b"));
    when(store.getPluginPath("a")).thenThrow(new RuntimeException("boom"));
    when(store.getPluginPath("b")).thenReturn(
        buildMinimalJar("b", TestMinimalPlugin.class));
    manager.setPluginStore(store);
    manager.loadAllPlugins();
    assertFalse(manager.hasPlugin("a"));
    assertTrue(manager.hasPlugin("b"));
  }

  @Test
  void descriptorValidationBranches() throws Exception {
    PluginDescriptor d = baseDescriptor("rich.plugin", TestMinimalPlugin.class.getName());
    d.setDependencies(List.of("missing-dep"));
    d.setMinSystemVersion("0.0.1");
    d.setRequiredPermissions(List.of("perm.a"));
    properties.setEnableSecurityCheck(true);
    properties.setValidateOnStartup(true);
    Path jar = tempDir.resolve("rich.jar");
    PluginJarBuilder.build(jar, d, List.of(TestMinimalPlugin.class));
    assertTrue(manager.loadPlugin(jar));
  }

  @Test
  void loadPluginWithLibraryDir() throws Exception {
    PluginDescriptor d = baseDescriptor("lib.plugin", TestMinimalPlugin.class.getName());
    d.setLibraries(List.of("x"));
    Path jar = tempDir.resolve("plugins/lib.plugin.jar");
    Files.createDirectories(jar.getParent());
    Path libDir = jar.getParent().resolve("lib.plugin-lib");
    Files.createDirectories(libDir);
    Path extraJar = libDir.resolve("extra.jar");
    try (var os = Files.newOutputStream(extraJar)) {
      os.write(new byte[]{0x50, 0x4b, 0x03, 0x04});
    }
    PluginJarBuilder.build(jar, d, List.of(TestMinimalPlugin.class));
    assertTrue(manager.loadPlugin(jar));
  }

  private Path buildMinimalJar(String pluginId, Class<?> pluginClass) throws Exception {
    PluginDescriptor d = baseDescriptor(pluginId, pluginClass.getName());
    Path jar = tempDir.resolve("plugins").resolve(pluginId + ".jar");
    Files.createDirectories(jar.getParent());
    PluginJarBuilder.build(jar, d, List.of(pluginClass));
    return jar;
  }

  private Path buildRestfulJar(String pluginId) throws Exception {
    PluginDescriptor d = baseDescriptor(pluginId, TestRestfulPlugin.class.getName());
    Path jar = tempDir.resolve("plugins").resolve(pluginId + ".jar");
    Files.createDirectories(jar.getParent());
    PluginJarBuilder.build(jar, d,
        List.of(TestRestfulPlugin.class, SamplePluginController.class));
    return jar;
  }

  private static PluginDescriptor baseDescriptor(String id, String pluginClass) {
    PluginDescriptor d = new PluginDescriptor();
    d.setId(id);
    d.setName("N");
    d.setVersion("1.0.0");
    d.setPluginClass(pluginClass);
    return d;
  }
}
