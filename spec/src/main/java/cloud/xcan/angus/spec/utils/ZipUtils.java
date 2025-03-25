package cloud.xcan.angus.spec.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ZipUtils {

  public ZipUtils() {
  }

  private static final Logger log = LoggerFactory.getLogger(ZipUtils.class);

  /**
   * Holds the destination directory. File will be zipped into the destination directory.
   */
  private File destination;

  /**
   * Holds path to compressed file.
   */
  private File[] source;

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
   * Compress the content of zip file ({@code source}) to destination directory. If destination
   * directory already exists it will be deleted before.
   */
  public void compress() throws IOException {
    log.debug("Compress content of {} to {}", getFilenames(), destination);

    // delete destination directory if exists
    if (destination.exists() && destination.isDirectory()) {
      FileUtils.deleteRecursive(destination.toPath());
    }
    FileOutputStream fos = new FileOutputStream(destination);
    ZipOutputStream zipOut = new ZipOutputStream(fos);
    for (File fileToZip : source) {
      FileInputStream fis = new FileInputStream(fileToZip);
      ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
      zipOut.putNextEntry(zipEntry);
      byte[] bytes = new byte[4096];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
        zipOut.write(bytes, 0, length);
      }
      fis.close();
    }
    zipOut.close();
    fos.close();
  }

  private String getFilenames() {
    return this.source == null ? ""
        : Arrays.stream(this.source).map(File::getName).collect(Collectors.joining());
  }
}
