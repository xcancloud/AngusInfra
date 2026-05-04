package cloud.xcan.angus.spec.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public final class DeflaterUtils {

  private DeflaterUtils() {
  }

  public static byte[] compress(String input) throws IOException {
    return compress(input.getBytes(StandardCharsets.UTF_8));
  }

  public static byte[] compress(byte[] inputData) throws IOException {
    Deflater deflater = new Deflater();
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream,
            deflater)) {
      deflaterOutputStream.write(inputData);
      deflaterOutputStream.finish();
      return outputStream.toByteArray();
    }
  }

  public static String decompress(byte[] compressedData) throws IOException {
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream,
            new Inflater());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inflaterInputStream.read(buffer)) > 0) {
        outputStream.write(buffer, 0, length);
      }
      return outputStream.toString(StandardCharsets.UTF_8);
    }
  }

  public static void compress(File inputFile, File outputFile) throws IOException {
    try (FileInputStream fileInputStream = new FileInputStream(inputFile);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fileOutputStream,
            new Deflater())) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = fileInputStream.read(buffer)) > 0) {
        deflaterOutputStream.write(buffer, 0, length);
      }
      deflaterOutputStream.finish();
    }
  }

  public static void decompress(File inputFile, File outputFile) throws IOException {
    try (FileInputStream fileInputStream = new FileInputStream(inputFile);
        InflaterInputStream inflaterInputStream = new InflaterInputStream(fileInputStream,
            new Inflater());
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = inflaterInputStream.read(buffer)) > 0) {
        fileOutputStream.write(buffer, 0, length);
      }
    }
  }
}
