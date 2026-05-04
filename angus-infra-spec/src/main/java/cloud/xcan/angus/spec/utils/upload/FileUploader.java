package cloud.xcan.angus.spec.utils.upload;

import cloud.xcan.angus.spec.http.HttpSender.Response;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Multipart file upload over HTTP(S).
 */
public interface FileUploader {

  /**
   * Uploads files as {@code multipart/form-data} using {@code fileParamName} for each part.
   */
  Response uploadFiles(String uploadUrl, String fileParamName, File[] files) throws IOException;

  /**
   * @param uploadUrl     destination URL (must be {@code http} or {@code https})
   * @param fileParamName multipart field name for file parts
   * @param files         files to send (non-null, non-empty; each entity must be a readable file)
   * @param headParams    optional request headers (null skips)
   * @param uploadFiles   optional extra <em>text</em> multipart fields (map key = field name, value
   *                      = field body); despite the name, these are not file parts
   */
  Response uploadFiles(String uploadUrl, String fileParamName, File[] files,
      Map<String, String> headParams, Map<String, String> uploadFiles) throws IOException;

}
