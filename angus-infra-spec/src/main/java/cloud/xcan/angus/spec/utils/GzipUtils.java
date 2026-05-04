package cloud.xcan.angus.spec.utils;


import static cloud.xcan.angus.spec.SpecConstant.UTF8;
import static cloud.xcan.angus.spec.experimental.StandardCharsets.ISO_8859_1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class GzipUtils {

  private static final int BUFFER = 8 * 1024;

  private GzipUtils() {
  }

  public static void compress(File orgFile, File compressFile) throws IOException {
    if (orgFile == null || !orgFile.isFile() || compressFile == null) {
      return;
    }
    try (FileInputStream is = new FileInputStream(orgFile);
        FileOutputStream os = new FileOutputStream(compressFile)) {
      compress(is, os);
    }
  }

  public static void compress(String orgFile, String compressFile) throws IOException {
    if (orgFile == null || compressFile == null) {
      return;
    }
    compress(new File(orgFile), new File(compressFile));
  }

  public static byte[] compress(byte[] bytes) throws IOException {
    if (bytes == null) {
      return null;
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (GZIPOutputStream gos = new GZIPOutputStream(baos)) {
      gos.write(bytes);
      gos.finish();
    }
    return baos.toByteArray();
  }

  /**
   * Gzip-compresses bytes from {@code is} into {@code os}. Does not close {@code is} or {@code os};
   * closes only the wrapping {@link GZIPOutputStream} (which typically flushes the underlying
   * {@code os}).
   */
  public static void compress(InputStream is, OutputStream os) throws IOException {
    try (GZIPOutputStream gos = new GZIPOutputStream(os)) {
      byte[] buffer = new byte[BUFFER];
      int len;
      while ((len = is.read(buffer)) != -1) {
        gos.write(buffer, 0, len);
      }
      gos.finish();
    }
  }

  public static void decompress(File orgFile, File compressFile) throws IOException {
    if (orgFile == null || compressFile == null || !compressFile.isFile()) {
      return;
    }
    try (FileInputStream is = new FileInputStream(compressFile);
        FileOutputStream os = new FileOutputStream(orgFile)) {
      decompress(is, os);
    }
  }

  public static void decompress(String orgFile, String compressFile) throws IOException {
    if (orgFile == null || compressFile == null) {
      return;
    }
    decompress(new File(orgFile), new File(compressFile));
  }

  public static byte[] decompress(byte[] bytes) throws IOException {
    if (bytes == null) {
      return null;
    }
    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      decompress(bais, baos);
      return baos.toByteArray();
    }
  }

  /**
   * Gunzip from {@code is} into {@code os}. Does not close {@code is} or {@code os}.
   */
  public static void decompress(InputStream is, OutputStream os) throws IOException {
    try (GZIPInputStream gis = new GZIPInputStream(is)) {
      byte[] buffer = new byte[BUFFER];
      int len;
      while ((len = gis.read(buffer)) != -1) {
        os.write(buffer, 0, len);
      }
      os.flush();
    }
  }

  public static String compress(String str) throws IOException {
    if (str == null || str.isEmpty()) {
      return str;
    }
    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(out)) {
      gzip.write(str.getBytes(UTF8));
      gzip.finish();
      return out.toString(ISO_8859_1);
    }
  }

  public static String decompress(String compressedStr) throws IOException {
    if (compressedStr == null || compressedStr.isEmpty()) {
      return compressedStr;
    }
    try (ByteArrayInputStream in = new ByteArrayInputStream(compressedStr.getBytes(ISO_8859_1));
        GZIPInputStream gunzip = new GZIPInputStream(in);
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[256];
      int n;
      while ((n = gunzip.read(buffer)) >= 0) {
        out.write(buffer, 0, n);
      }
      return out.toString(UTF8);
    }
  }
}
