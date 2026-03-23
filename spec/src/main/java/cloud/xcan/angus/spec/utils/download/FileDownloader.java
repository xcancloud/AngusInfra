package cloud.xcan.angus.spec.utils.download;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

/**
 * Downloads remote or local resources to a path or in-memory bytes.
 * <p>
 * Implementations may throw {@link IOException} for I/O failures; other runtime failures use
 * unchecked exceptions. The {@code throws Exception} signatures are retained for backward
 * compatibility with existing callers.
 */
public interface FileDownloader {

  Path downloadFile(URL fileUrl) throws Exception;

  Path downloadFile(URL fileUrl, String destinationPath) throws Exception;

  Path downloadFile(URL fileUrl, Map<String, String> headParams) throws Exception;

  Path downloadFile(URL fileUrl, Map<String, String> headParams, String destinationPath)
      throws Exception;

  byte[] downloadBytes(URL fileUrl) throws Exception;

  byte[] downloadBytes(URL fileUrl, Map<String, String> headParams) throws Exception;
}
