package cloud.xcan.angus.plugin.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cloud.xcan.angus.plugin.autoconfigure.PluginProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DiskPluginStoreTest {

  @Test
  void listEmptyWhenDirectoryHasNoJars(@TempDir Path tmp) throws IOException {
    PluginProperties p = new PluginProperties();
    p.setDirectory(tmp.toString());
    DiskPluginStore store = new DiskPluginStore(p);
    assertTrue(store.listPluginIds().isEmpty());
  }

  @Test
  void storeGetListDelete(@TempDir Path tmp) throws IOException {
    PluginProperties p = new PluginProperties();
    p.setDirectory(tmp.toString());
    DiskPluginStore store = new DiskPluginStore(p);

    assertTrue(store.listPluginIds().isEmpty());

    Path written = store.storePlugin("my.plugin", new byte[]{9, 8, 7});
    assertNotNull(written);
    assertTrue(Files.exists(written));

    List<String> ids = store.listPluginIds();
    assertEquals(1, ids.size());
    assertEquals("my.plugin", ids.get(0));

    Path resolved = store.getPluginPath("my.plugin");
    assertNotNull(resolved);
    assertTrue(Files.exists(resolved));
    assertArrayEquals(new byte[]{9, 8, 7}, Files.readAllBytes(resolved));

    assertTrue(store.deletePlugin("my.plugin"));
    assertFalse(Files.exists(resolved));
    assertNull(store.getPluginPath("my.plugin"));
  }

  @Test
  void invalidPluginIdThrows(@TempDir Path tmp) {
    PluginProperties p = new PluginProperties();
    p.setDirectory(tmp.toString());
    DiskPluginStore store = new DiskPluginStore(p);
    assertThrows(IOException.class, () -> store.getPluginPath("../evil"));
    assertThrows(IOException.class, () -> store.getPluginPath(null));
  }

  @Test
  void pathTraversalRejected(@TempDir Path tmp) {
    PluginProperties p = new PluginProperties();
    p.setDirectory(tmp.toString());
    DiskPluginStore store = new DiskPluginStore(p);
    assertThrows(IOException.class, () -> store.getPluginPath("x/../../../etc/passwd"));
  }

}
