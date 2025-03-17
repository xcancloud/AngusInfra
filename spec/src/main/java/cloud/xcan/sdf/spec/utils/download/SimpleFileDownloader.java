package cloud.xcan.sdf.spec.utils.download;

import static cloud.xcan.sdf.spec.SpecConstant.DEFAULT_ENCODING;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import cloud.xcan.sdf.spec.http.HttpResponseHeader;
import cloud.xcan.sdf.spec.utils.AppDirUtils;
import cloud.xcan.sdf.spec.utils.FileUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Downloads a file from a URL.
 */
public class SimpleFileDownloader implements FileDownloader {

  public static final String TEMP_DIRECTORY = "sdf-downloader";

  private static final Logger log = LoggerFactory.getLogger(SimpleFileDownloader.class);

  /**
   * Downloads a file. If HTTP(S) or FTP, stream content, if local file:/ do a simple filesystem
   * copy to tmp folder. Other protocols not supported.
   *
   * @param fileUrl the URI representing the file to download
   * @return the path of downloaded/copied file
   * @throws IOException      in case of network or IO problems
   * @throws RuntimeException in case of other problems
   */
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
    switch (fileUrl.getProtocol()) {
      case "http":
      case "https":
      case "ftp":
        return downloadFileHttp(fileUrl, headParams, destinationPath);
      case "file":
        return copyLocalFile(fileUrl);
      default:
        throw new RuntimeException("URL protocol " + fileUrl.getProtocol() + " not supported");
    }
  }

  @Override
  public byte[] downloadBytes(URL fileUrl) throws Exception {
    return downloadBytes(fileUrl, null);
  }

  @Override
  public byte[] downloadBytes(URL fileUrl, Map<String, String> headParams)
      throws Exception {
    switch (fileUrl.getProtocol()) {
      case "http":
      case "https":
      case "ftp":
        return downloadBytesHttp(fileUrl, headParams);
      case "file":
        return copyLocalFileBytes(fileUrl);
      default:
        throw new RuntimeException("URL protocol " + fileUrl.getProtocol() + " not supported");
    }
  }

  /**
   * Efficient copy of file in case of local file system.
   *
   * @param fileUrl source file
   * @return path of target file
   * @throws IOException      if problems during copy
   * @throws RuntimeException in case of other problems
   */
  protected Path copyLocalFile(URL fileUrl) throws IOException {
    Path destination = Files.createTempDirectory(initDownloadTempDir().toPath(), null);
    destination.toFile().deleteOnExit();
    try {
      Path fromFile = Paths.get(fileUrl.toURI());
      Path toFile = getFileUrlPath(fileUrl, destination);
      Files.copy(fromFile, toFile, COPY_ATTRIBUTES, REPLACE_EXISTING);
      return toFile;
    } catch (URISyntaxException e) {
      throw new RuntimeException("Something wrong with given URL", e);
    }
  }

  protected byte[] copyLocalFileBytes(URL fileUrl) throws IOException {
    try {
      Path fromFile = Paths.get(fileUrl.toURI());
      return FileUtils.readFileToByteArray(fromFile.toFile());
    } catch (URISyntaxException e) {
      throw new RuntimeException("Something wrong with given URL", e);
    }
  }

  /**
   * Downloads file from HTTP or FTP.
   *
   * @param fileUrl source file
   * @return path of downloaded file
   * @throws Exception        if IO problems
   * @throws RuntimeException if validation fails or any other problems
   */
  protected Path downloadFileHttp(URL fileUrl, Map<String, String> headers,
      String destinationPath) throws Exception {
    Path destination = isNotEmpty(destinationPath) ? Path.of(destinationPath).toFile().exists()
        ? Path.of(destinationPath) : Files.createDirectory(Path.of(destinationPath))
        : Files.createTempDirectory(initDownloadTempDir().toPath(), null);
    destination.toFile().deleteOnExit();
    Path file = getFileUrlPath(fileUrl, destination);

    // set up the URL connection
    URLConnection connection = fileUrl.openConnection();

    // set headers
    if (org.apache.commons.lang3.ObjectUtils.isNotEmpty(headers)) {
      for (String key : headers.keySet()) {
        connection.setRequestProperty(key, headers.get(key));
      }
    }

    // connect to the remote site (may takes some time)
    connection.connect();

    // check for http authorization
    HttpURLConnection httpConnection = (HttpURLConnection) connection;
    if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
      throw new ConnectException("HTTP Authorization failure");
    }

    // try to get the server-specified last-modified date of this artifact
    long lastModified = httpConnection.getHeaderFieldDate(HttpResponseHeader.Last_Modified.value,
        System.currentTimeMillis());

    // try to get the input stream (three times)
    try (InputStream is = getDownloadInputStream(fileUrl,
        connection); FileOutputStream fos = new FileOutputStream(file.toFile())) {
      // reade from remote resource and write to the local file
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) >= 0) {
        fos.write(buffer, 0, length);
      }
    }

    log.debug("Set last modified of {} to {}", file, lastModified);
    Files.setLastModifiedTime(file, FileTime.fromMillis(lastModified));
    return file;
  }

  /**
   * Downloads bytes from HTTP or FTP.
   *
   * @param fileUrl source file
   * @return bytes of downloaded file
   * @throws IOException      if IO problems
   * @throws RuntimeException if validation fails or any other problems
   */
  protected byte[] downloadBytesHttp(URL fileUrl, Map<String, String> headers) throws Exception {
    // set up the URL connection
    URLConnection connection = fileUrl.openConnection();

    // set headers
    if (ObjectUtils.isNotEmpty(headers)) {
      for (String key : headers.keySet()) {
        connection.setRequestProperty(key, headers.get(key));
      }
    }

    // connect to the remote site (may takes some time)
    connection.connect();

    // check for http authorization
    HttpURLConnection httpConnection = (HttpURLConnection) connection;
    if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
      throw new ConnectException("HTTP Authorization failure");
    }

    // Try to get the input stream (three times)
    // try to get the input stream (three times)
    try (InputStream is = getDownloadInputStream(fileUrl,
        connection); ByteArrayOutputStream fos = new ByteArrayOutputStream()) {
      // reade from remote resource and write to the local file
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) >= 0) {
        fos.write(buffer, 0, length);
      }
      return fos.toByteArray();
    }
  }

  private InputStream getDownloadInputStream(URL fileUrl, URLConnection connection)
      throws ConnectException {
    InputStream is = null;
    for (int i = 0; i < 3; i++) {
      try {
        is = connection.getInputStream();
        break;
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
    if (is == null) {
      throw new ConnectException("Can't download from '" + fileUrl.toString());
    }
    return is;
  }

  private Path getFileUrlPath(URL fileUrl, Path destination) throws UnsupportedEncodingException {
    String path = fileUrl.getPath();
    String fileName = path.substring(path.lastIndexOf('/') + 1);
    fileName = URLDecoder.decode(fileName, DEFAULT_ENCODING);
    return destination.resolve(fileName);
  }

  private File initDownloadTempDir() throws IOException {
    String tempDir = new AppDirUtils().getTmpDir() + TEMP_DIRECTORY;
    File tempFile = new File(tempDir);
    if (tempFile.mkdirs()) {
      return tempFile;
    }
    return tempFile;
  }
}
