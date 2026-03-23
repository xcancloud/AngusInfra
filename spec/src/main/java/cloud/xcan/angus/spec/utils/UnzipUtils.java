package cloud.xcan.angus.spec.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnzipUtils {

  private static final Logger log = LoggerFactory.getLogger(UnzipUtils.class);

  private File destination;

  private File source;

  public UnzipUtils() {
  }

  public UnzipUtils(File source, File destination) {
    this.source = source;
    this.destination = destination;
  }

  public void setSource(File source) {
    this.source = source;
  }

  public void setDestination(File destination) {
    this.destination = destination;
  }

  public File getDestination() {
    return destination;
  }

  public File getSource() {
    return source;
  }

  /**
   * Extract the content of zip file ({@code source}) to destination directory. If destination
   * directory already exists it will be deleted before.
   */
  public void extract() throws IOException {
    log.debug("Extract content of {} to {}", source, destination);

    if (destination.exists() && destination.isDirectory()) {
      FileUtils.deleteRecursive(destination.toPath());
    }

    Path destDir = destination.toPath().toAbsolutePath().normalize();
    Files.createDirectories(destDir);

    try (InputStream fin = Files.newInputStream(source.toPath());
        ZipInputStream zipInputStream = new ZipInputStream(fin)) {
      ZipEntry zipEntry;
      byte[] buffer = new byte[4096];
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        Path outPath = destDir.resolve(zipEntry.getName()).normalize();
        if (!outPath.startsWith(destDir)) {
          throw new IOException("Zip entry outside destination: " + zipEntry.getName());
        }

        if (zipEntry.isDirectory()) {
          Files.createDirectories(outPath);
        } else {
          Files.createDirectories(outPath.getParent());
          try (OutputStream fos = Files.newOutputStream(outPath)) {
            int length;
            while ((length = zipInputStream.read(buffer)) >= 0) {
              fos.write(buffer, 0, length);
            }
          }
        }
        zipInputStream.closeEntry();
      }
    }
  }
}
