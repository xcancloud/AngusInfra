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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

/**
 * {@link FileUploader} using {@link HttpURLConnection} and {@code multipart/form-data}.
 */
public class SimpleFileUploader implements FileUploader {

  private static final String CRLF = "\r\n";

  private static final String OCTET_STREAM = "application/octet-stream";

  private final String boundary = UUID.randomUUID().toString();

  @Override
  public Response uploadFiles(String uploadUrl, String fileParamName, File[] files)
      throws IOException {
    return uploadFiles(uploadUrl, fileParamName, files, null, null);
  }

  @Override
  public Response uploadFiles(String uploadUrl, String fileParamName, File[] files,
      Map<String, String> headParams,
      Map<String, String> textFormFields) throws IOException {
    if (isEmpty(uploadUrl)) {
      throw new IllegalArgumentException("Parameter uploadUrl cannot be empty");
    }
    if (isEmpty(fileParamName)) {
      throw new IllegalArgumentException("Parameter fileParamName cannot be empty");
    }
    if (isEmpty(files)) {
      throw new IllegalArgumentException("Parameter files cannot be empty");
    }
    for (File file : files) {
      Objects.requireNonNull(file, "files");
      if (!file.isFile()) {
        throw new IllegalArgumentException("Not a regular file: " + file);
      }
    }

    final URL url;
    try {
      url = new URI(uploadUrl).toURL();
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid upload URL: " + uploadUrl, e);
    }
    URLConnection conn = url.openConnection();
    if (!(conn instanceof HttpURLConnection)) {
      throw new IllegalArgumentException("uploadUrl must use http or https: " + uploadUrl);
    }
    HttpURLConnection httpConn = (HttpURLConnection) conn;
    httpConn.setRequestMethod("POST");

    if (headParams != null) {
      for (Entry<String, String> e : headParams.entrySet()) {
        if (e.getKey() != null && e.getValue() != null) {
          httpConn.setRequestProperty(e.getKey(), e.getValue());
        }
      }
    }

    httpConn.setDoOutput(true);
    httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

    try (OutputStream output = httpConn.getOutputStream();
        PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(output, StandardCharsets.UTF_8), true)) {
      writer.append("--").append(boundary).append(CRLF);
      writer.append("Content-Disposition: form-data; name=\"param\"").append(CRLF);
      writer.append("Content-Type: text/plain; charset=")
          .append(StandardCharsets.UTF_8.name()).append(CRLF);
      writer.append(CRLF).append(StandardCharsets.UTF_8.name()).append(CRLF).flush();

      if (isNotEmpty(textFormFields)) {
        for (Entry<String, String> entry : textFormFields.entrySet()) {
          if (entry.getKey() == null) {
            continue;
          }
          writer.append("--").append(boundary).append(CRLF);
          writer.append("Content-Disposition: form-data; name=\"").append(entry.getKey())
              .append("\"").append(CRLF);
          writer.append("Content-Type: text/plain; charset=")
              .append(StandardCharsets.UTF_8.name()).append(CRLF);
          String value = entry.getValue() != null ? entry.getValue() : "";
          writer.append(CRLF).append(value).append(CRLF).flush();
        }
      }

      for (File file : files) {
        writer.append("--").append(boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"").append(fileParamName)
            .append("\"; filename=\"").append(file.getName()).append("\"").append(CRLF);
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if (contentType == null || contentType.isEmpty()) {
          contentType = OCTET_STREAM;
        }
        writer.append("Content-Type: ").append(contentType).append(CRLF);
        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(file.toPath(), output);
        output.flush();
        writer.append(CRLF).flush();
      }

      writer.append("--").append(boundary).append("--").append(CRLF).flush();

      int status = httpConn.getResponseCode();
      String body = readResponseBodyUtf8(httpConn, status);
      return new Response(status, null, body, null);
    } finally {
      httpConn.disconnect();
    }
  }

  private static String readResponseBodyUtf8(HttpURLConnection conn, int status) {
    InputStream in;
    if (status >= HttpURLConnection.HTTP_BAD_REQUEST) {
      in = conn.getErrorStream();
    } else {
      try {
        in = conn.getInputStream();
      } catch (IOException ex) {
        in = conn.getErrorStream();
      }
    }
    if (in == null) {
      in = conn.getErrorStream();
    }
    if (in == null) {
      return null;
    }
    return IOUtils.toString(in, StandardCharsets.UTF_8);
  }
}
