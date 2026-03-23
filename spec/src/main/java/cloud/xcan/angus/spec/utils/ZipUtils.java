package cloud.xcan.angus.spec.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ZipUtils {

  private static final Logger log = LoggerFactory.getLogger(ZipUtils.class);

  private File destination;

  private File[] source;

  public ZipUtils() {
  }

  public ZipUtils(File[] source, File destination) {
    this.source = source;
    this.destination = destination;
  }

  public void setSource(File[] source) {
    this.source = source;
  }

  public void setDestination(File destination) {
    this.destination = destination;
  }

  public File getDestination() {
    return destination;
  }

  public File[] getSource() {
    return source;
  }

  /**
   * Writes a zip archive to {@code destination} (a file path, typically {@code .zip}). Existing
   * regular files at that path are replaced; if {@code destination} is a directory it is removed
   * first.
   */
  public void compress() throws IOException {
    Objects.requireNonNull(destination, "destination");
    File[] files = Objects.requireNonNull(source, "source");
    log.debug("Compress content of {} to {}", getFilenames(), destination);

    if (destination.exists()) {
      if (destination.isDirectory()) {
        FileUtils.deleteRecursive(destination.toPath());
      } else {
        Files.deleteIfExists(destination.toPath());
      }
    }

    try (OutputStream fos = Files.newOutputStream(destination.toPath());
        ZipOutputStream zipOut = new ZipOutputStream(fos)) {
      for (File fileToZip : files) {
        if (fileToZip == null || !fileToZip.isFile()) {
          continue;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
          ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
          zipOut.putNextEntry(zipEntry);
          byte[] bytes = new byte[4096];
          int length;
          while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
          }
          zipOut.closeEntry();
        }
      }
    }
  }

  private String getFilenames() {
    return this.source == null ? ""
        : Arrays.stream(this.source).filter(Objects::nonNull).map(File::getName)
            .collect(Collectors.joining(", "));
  }
}
