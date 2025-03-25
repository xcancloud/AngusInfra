package cloud.xcan.angus.spec.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class DeflaterUtils {

  public static byte[] compress(String input) throws IOException {
    return compress(input.getBytes());
  }

  public static byte[] compress(byte[] inputData) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Deflater deflater = new Deflater();
    DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream, deflater);
    deflaterOutputStream.write(inputData);
    deflaterOutputStream.close();
    return outputStream.toByteArray();
  }

  public static String decompress(byte[] compressedData) throws IOException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
    Inflater inflater = new Inflater();
    InflaterInputStream inflaterInputStream = new InflaterInputStream(inputStream, inflater);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inflaterInputStream.read(buffer)) > 0) {
      outputStream.write(buffer, 0, length);
    }
    inflaterInputStream.close();
    return outputStream.toString();
  }

  public static void compress(File inputFile, File outputFile) throws IOException {
    FileInputStream fileInputStream = new FileInputStream(inputFile);
    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
    Deflater deflater = new Deflater();
    DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fileOutputStream,
        deflater);
    byte[] buffer = new byte[1024];
    int length;
    while ((length = fileInputStream.read(buffer)) > 0) {
      deflaterOutputStream.write(buffer, 0, length);
    }
    fileInputStream.close();
    deflaterOutputStream.finish();
    deflaterOutputStream.close();
  }

  public static void decompress(File inputFile, File outputFile) throws IOException {
    FileInputStream fileInputStream = new FileInputStream(inputFile);
    FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
    Inflater inflater = new Inflater();
    InflaterInputStream inflaterInputStream = new InflaterInputStream(fileInputStream, inflater);
    byte[] buffer = new byte[1024];
    int length;
    while ((length = inflaterInputStream.read(buffer)) > 0) {
      fileOutputStream.write(buffer, 0, length);
    }
    inflaterInputStream.close();
    fileOutputStream.close();
  }

}
