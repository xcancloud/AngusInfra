package cloud.xcan.angus.spec.utils.download;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.spec.http.HttpResponseHeader;
import cloud.xcan.angus.spec.utils.AppDirUtils;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a file from HTTP(S), FTP, or copies a {@code file:} URL via the filesystem.
 */
public final class SimpleFileDownloader implements FileDownloader {

  /** Subdirectory under the application tmp root for downloader-created temp folders. */
  public static final String TEMP_DIRECTORY = "angus-downloader";

  private static final int BUFFER_SIZE = 8192;

  private static final Logger log = LoggerFactory.getLogger(SimpleFileDownloader.class);

  @Override
  public Path downloadFile(URL fileUrl) throws Exception {
    return downloadFile(fileUrl, null, null);
  }

  @Override
  public Path downloadFile(URL fileUrl, String destinationPath) throws Exception {
    return downloadFile(fileUrl, null, destinationPath);
  }

  @Override
  public Path downloadFile(URL fileUrl, Map<String, String> headParams) throws Exception {
    return downloadFile(fileUrl, headParams, null);
  }

  @Override
  public Path downloadFile(URL fileUrl, Map<String, String> headParams, String destinationPath)
      throws Exception {
    String protocol = fileUrl.getProtocol();
    return switch (protocol) {
      case "http", "https", "ftp" -> downloadFileHttp(fileUrl, headParams, destinationPath);
      case "file" -> copyLocalFile(fileUrl);
      default -> throw new UnsupportedOperationException(
          "URL protocol '" + protocol + "' is not supported");
    };
  }

  @Override
  public byte[] downloadBytes(URL fileUrl) throws Exception {
    return downloadBytes(fileUrl, null);
  }

  @Override
  public byte[] downloadBytes(URL fileUrl, Map<String, String> headParams) throws Exception {
    String protocol = fileUrl.getProtocol();
    return switch (protocol) {
      case "http", "https", "ftp" -> downloadBytesHttp(fileUrl, headParams);
      case "file" -> copyLocalFileBytes(fileUrl);
      default -> throw new UnsupportedOperationException(
          "URL protocol '" + protocol + "' is not supported");
    };
  }

  /**
   * Copies a local {@code file:} URL into a new temp directory and returns the destination file.
   */
  Path copyLocalFile(URL fileUrl) throws IOException {
    Path tempParent = Files.createTempDirectory(initDownloadTempDir(), "local-");
    tempParent.toFile().deleteOnExit();
    try {
      Path fromFile = Paths.get(fileUrl.toURI());
      Path toFile = resolveOutputFileName(fileUrl, tempParent);
      Files.copy(fromFile, toFile, COPY_ATTRIBUTES, REPLACE_EXISTING);
      return toFile;
    } catch (Exception e) {
      throw new IOException("Invalid file URL: " + fileUrl, e);
    }
  }

  byte[] copyLocalFileBytes(URL fileUrl) throws IOException {
    try {
      Path fromFile = Paths.get(fileUrl.toURI());
      return Files.readAllBytes(fromFile);
    } catch (Exception e) {
      throw new IOException("Invalid file URL: " + fileUrl, e);
    }
  }

  Path downloadFileHttp(URL fileUrl, Map<String, String> headers, String destinationPath)
      throws Exception {
    Path destination = resolveDestination(destinationPath);
    if (!isNotEmpty(destinationPath)) {
      destination.toFile().deleteOnExit();
    }
    Path file = resolveOutputFileName(fileUrl, destination);

    URLConnection connection = openConnection(fileUrl, headers);
    connection.connect();

    long lastModified = System.currentTimeMillis();
    if (connection instanceof HttpURLConnection http) {
      if (http.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
        throw new ConnectException("HTTP Authorization failure");
      }
      lastModified = http.getHeaderFieldDate(HttpResponseHeader.Last_Modified.value, lastModified);
    }

    try (InputStream is = getDownloadInputStream(fileUrl, connection);
        FileOutputStream fos = new FileOutputStream(file.toFile())) {
      byte[] buffer = new byte[BUFFER_SIZE];
      int length;
      while ((length = is.read(buffer)) >= 0) {
        fos.write(buffer, 0, length);
      }
    }

    log.debug("Set last modified of {} to {}", file, lastModified);
    Files.setLastModifiedTime(file, FileTime.fromMillis(lastModified));
    return file;
  }

  byte[] downloadBytesHttp(URL fileUrl, Map<String, String> headers) throws Exception {
    URLConnection connection = openConnection(fileUrl, headers);
    connection.connect();

    if (connection instanceof HttpURLConnection http) {
      if (http.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
        throw new ConnectException("HTTP Authorization failure");
      }
    }

    try (InputStream is = getDownloadInputStream(fileUrl, connection)) {
      return is.readAllBytes();
    }
  }

  private static URLConnection openConnection(URL fileUrl, Map<String, String> headers)
      throws IOException {
    URLConnection connection = fileUrl.openConnection();
    if (isNotEmpty(headers)) {
      for (Map.Entry<String, String> e : headers.entrySet()) {
        if (e.getKey() != null && e.getValue() != null) {
          connection.setRequestProperty(e.getKey(), e.getValue());
        }
      }
    }
    return connection;
  }

  private InputStream getDownloadInputStream(URL fileUrl, URLConnection connection)
      throws ConnectException {
    IOException last = null;
    for (int i = 0; i < 3; i++) {
      try {
        return connection.getInputStream();
      } catch (IOException e) {
        last = e;
        log.warn("Open stream attempt {} failed for {}: {}", i + 1, fileUrl, e.getMessage());
      }
    }
    throw new ConnectException(
        "Can't download from '" + fileUrl + "'" + (last != null ? ": " + last.getMessage() : ""));
  }

  private static Path resolveOutputFileName(URL fileUrl, Path destination) {
    String path = fileUrl.getPath();
    int slash = path.lastIndexOf('/');
    String fileName = slash >= 0 ? path.substring(slash + 1) : path;
    if (fileName.isEmpty()) {
      fileName = "download";
    }
    fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
    return destination.resolve(fileName);
  }

  private static Path initDownloadTempDir() throws IOException {
    Path dir = Paths.get(new AppDirUtils().getTmpDir(), TEMP_DIRECTORY);
    Files.createDirectories(dir);
    return dir;
  }

  /**
   * When {@code destinationPath} is blank, creates a temp directory under the downloader tmp root.
   * Otherwise uses an existing path or creates the directory (including parents).
   */
  private static Path resolveDestination(String destinationPath) throws IOException {
    if (!isNotEmpty(destinationPath)) {
      return Files.createTempDirectory(initDownloadTempDir(), "dl-");
    }
    Path p = Path.of(destinationPath);
    if (Files.exists(p)) {
      return p;
    }
    return Files.createDirectories(p);
  }
}
