package cloud.xcan.sdf.spec.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UnzipUtils {

  public UnzipUtils() {
  }

  private static final Logger log = LoggerFactory.getLogger(UnzipUtils.class);

  /**
   * Holds the destination directory. File will be unzipped into the destination directory.
   */
  private File destination;

  /**
   * Holds path to zip file.
   */
  private File source;

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

    // delete destination directory if exists
    if (destination.exists() && destination.isDirectory()) {
      FileUtils.deleteRecursive(destination.toPath());
    }

    try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(source))) {
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        File file = new File(destination, zipEntry.getName());

        // create intermediary directories - sometimes zip don't add them
        File dir = new File(file.getParent());

        mkdirsOrThrow(dir);

        if (zipEntry.isDirectory()) {
          mkdirsOrThrow(file);
        } else {
          byte[] buffer = new byte[4096];
          int length;
          try (FileOutputStream fos = new FileOutputStream(file)) {
            while ((length = zipInputStream.read(buffer)) >= 0) {
              fos.write(buffer, 0, length);
            }
          }
        }
      }
    }
  }

  private static void mkdirsOrThrow(File dir) throws IOException {
    if (!dir.exists() && !dir.mkdirs()) {
      throw new IOException("Failed to create directory " + dir);
    }
  }

}
