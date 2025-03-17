package cloud.xcan.sdf.spec.utils;


import static cloud.xcan.sdf.spec.SpecConstant.UTF8;
import static cloud.xcan.sdf.spec.experimental.StandardCharsets.ISO_8859_1;

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


public class GzipUtils {

  private static final int BUFFER = 8 * 1024;

  public static void compress(File orgFile, File compressFile) throws IOException {
    if (orgFile != null && orgFile.isFile() && compressFile != null
        && compressFile.isFile()) {
      FileInputStream is = new FileInputStream(orgFile);
      FileOutputStream os = new FileOutputStream(compressFile);
      compress(is, os);
      is.close();
      os.close();
    }
  }

  public static void compress(String orgFile, String compressFile) throws IOException {
    if (orgFile != null && compressFile != null) {
      FileInputStream is = new FileInputStream(orgFile);
      FileOutputStream os = new FileOutputStream(compressFile);
      compress(is, os);
      is.close();
      os.close();
    }
  }

  public static byte[] compress(byte[] bytes) throws IOException {
    if (bytes != null) {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      compress(bais, baos);
      bytes = baos.toByteArray();
      bais.close();
      baos.close();
    }
    return bytes;
  }

  public static void compress(InputStream is, OutputStream os) throws IOException {
    GZIPOutputStream gos = new GZIPOutputStream(os);
    int len;
    byte[] buffer = new byte[BUFFER];
    while ((len = is.read(buffer)) != -1) {
      gos.write(buffer, 0, len);
    }
    gos.finish();
    gos.flush();
    gos.close();
  }

  public static void decompress(File orgFile, File compressFile) throws IOException {
    if (orgFile != null && orgFile.isFile() && compressFile != null && compressFile
        .isFile()) {
      FileInputStream is = new FileInputStream(compressFile);
      FileOutputStream os = new FileOutputStream(orgFile);
      decompress(is, os);
      is.close();
      os.close();
    }
  }

  public static void decompress(String orgFile, String compressFile) throws IOException {
    if (orgFile != null && compressFile != null) {
      FileInputStream is = new FileInputStream(compressFile);
      FileOutputStream os = new FileOutputStream(orgFile);
      decompress(is, os);
      is.close();
      os.close();
    }
  }

  public static byte[] decompress(byte[] bytes) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    decompress(bais, baos);
    bytes = baos.toByteArray();
    bais.close();
    baos.close();
    return bytes;
  }

  public static void decompress(InputStream is, OutputStream os) throws IOException {
    GZIPInputStream gis = new GZIPInputStream(is);
    int len;
    byte[] buffer = new byte[BUFFER];
    while ((len = gis.read(buffer)) != -1) {
      os.write(buffer, 0, len);
    }
    os.flush();
    gis.close();
  }

  public static String compress(String str) throws IOException {
    if (str == null || str.length() == 0) {
      return str;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(out);
    gzip.write(str.getBytes(UTF8));
    gzip.close();
    return out.toString(ISO_8859_1);
  }

  public static String decompress(String compressedStr) throws IOException {
    if (compressedStr == null || compressedStr.length() == 0) {
      return compressedStr;
    }
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayInputStream in = new ByteArrayInputStream(compressedStr.getBytes(ISO_8859_1));
    GZIPInputStream gunzip = new GZIPInputStream(in);
    byte[] buffer = new byte[256];
    int n;
    while ((n = gunzip.read(buffer)) >= 0) {
      out.write(buffer, 0, n);
    }
    return out.toString(UTF8);
  }
}
