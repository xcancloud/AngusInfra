package cloud.xcan.angus.spec.utils.upload;


import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.spec.http.HttpSender.Response;
import cloud.xcan.angus.spec.utils.IOUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

public class SimpleFileUploader implements FileUploader {

  /**
   * Just generate some unique random value.
   */
  String boundary = java.util.UUID.randomUUID().toString();

  /**
   * Line separator required by multipart/form-data.
   */
  static String CRLF = "\r\n";

  @Override
  public Response uploadFiles(String uploadUrl, String fileParamName, File[] files)
      throws IOException {
    return uploadFiles(uploadUrl, fileParamName, files, null, null);
  }

  @Override
  public Response uploadFiles(String uploadUrl, String fileParamName, File[] files,
      Map<String, String> headParams,
      Map<String, String> params) throws IOException {
    if (isEmpty(uploadUrl)) {
      throw new IllegalArgumentException("Parameter uploadUrl cannot be empty");
    }
    if (isEmpty(files)) {
      throw new IllegalArgumentException("Parameter files cannot be empty");
    }

    // set up the URL connection
    URLConnection conn = new URL(uploadUrl).openConnection();

    // set headers
    if (headParams != null) {
      for (String key : headParams.keySet()) {
        conn.setRequestProperty(key, headParams.get(key));
      }
    }

    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    try (OutputStream output = conn.getOutputStream(); PrintWriter writer = new PrintWriter(
        new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
      // Send normal param.
      writer.append("--").append(boundary).append(CRLF);
      writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
      writer.append("Content-Type: text/plain; charset=")
          .append(String.valueOf(StandardCharsets.UTF_8)).append(CRLF);
      writer.append(CRLF).append(StandardCharsets.UTF_8.name()).append(CRLF).flush();

      if (isNotEmpty(params)) {
        for (Entry<String, String> entry : params.entrySet()) {
          writer.append("--").append(boundary).append(CRLF);
          writer.append("Content-Disposition: form-data; name=\"").append(entry.getKey())
              .append("\"").append(CRLF);
          writer.append("Content-Type: text/plain; charset=")
              .append(String.valueOf(StandardCharsets.UTF_8)).append(CRLF);
          writer.append(CRLF).append(entry.getValue()).append(CRLF).flush();
        }
      }

      for (File file : files) {
        // Send binary file.
        writer.append("--").append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"").append(fileParamName)
            .append("\"; filename=\"").append(file.getName()).append("\"").append(CRLF);
        writer.append("Content-Type: ")
            .append(URLConnection.guessContentTypeFromName(file.getName())).append(CRLF);
        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(file.toPath(), output);
        // Important before continuing with writer!
        output.flush();
        // CRLF is important! It indicates end of boundary.
        writer.append(CRLF).flush();
      }

      // End of multipart/form-data.
      writer.append("--").append(boundary).append("--").append(CRLF).flush();

      // Request is lazily fired whenever you need to obtain information about response.
      int status = ((HttpURLConnection) conn).getResponseCode();
      String body = null;
      InputStream bodyIS = null;
      try {
        if (((HttpURLConnection) conn).getErrorStream() != null) {
          bodyIS = ((HttpURLConnection) conn).getErrorStream();
          body = IOUtils.toString((bodyIS));
        } else if (conn.getInputStream() != null) {
          bodyIS = conn.getInputStream();
          body = IOUtils.toString(bodyIS);
        }
      } catch (IOException ignored) {
      }
      return new Response(status, null, body, bodyIS);
    }
  }
}
