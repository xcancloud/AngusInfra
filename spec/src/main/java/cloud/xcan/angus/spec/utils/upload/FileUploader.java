package cloud.xcan.angus.spec.utils.upload;

import cloud.xcan.angus.spec.http.HttpSender.Response;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Interface to upload files.
 */
public interface FileUploader {

  Response uploadFiles(String uploadUrl, String fileParamName, File[] files) throws IOException;

  Response uploadFiles(String uploadUrl, String fileParamName, File[] files,
      Map<String, String> headParams, Map<String, String> uploadFiles) throws IOException;

}
