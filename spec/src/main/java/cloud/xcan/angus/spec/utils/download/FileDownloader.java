package cloud.xcan.angus.spec.utils.download;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;

/**
 * Interface to download a file.
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
