/*
 * Copyright (c) 2021   XCan Company
 *
 *        http://www.xcan.cloud
 *
 * The product is based on the open source project org.asynchttpclient
 * modified or rewritten by the XCan team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * On the basis of Apache License 2.0, other terms need to comply with
 * XCBL License restriction requirements. Detail XCBL license at:
 *
 * http://www.xcan.cloud/licenses/XCBL-1.0
 */
package cloud.xcan.sdf.spec.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DeflateUtils {

  private static final Logger log = LoggerFactory.getLogger(IOUtils.class);

  public static void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception e) {
        log.error("close closeable ex", e);
      }
    }
  }

  public static byte[] compress(byte[] data) {

    ByteArrayOutputStream out = new ByteArrayOutputStream(data.length / 4);
    DeflaterOutputStream zipOut = new DeflaterOutputStream(out);
    try {
      zipOut.write(data);
      zipOut.finish();
      zipOut.close();
    } catch (IOException e) {
      log.error("compress ex", e);
      return null;
    } finally {
      close(zipOut);
    }
    return out.toByteArray();
  }

  public static byte[] decompress(byte[] data) {
    InflaterInputStream zipIn = new InflaterInputStream(new ByteArrayInputStream(data));
    ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
    byte[] buffer = new byte[1024];
    int length;
    try {
      while ((length = zipIn.read(buffer)) != -1) {
        out.write(buffer, 0, length);
      }
    } catch (IOException e) {
      log.error("decompress ex", e);
      return null;
    } finally {
      close(zipIn);
    }
    return out.toByteArray();
  }
}
