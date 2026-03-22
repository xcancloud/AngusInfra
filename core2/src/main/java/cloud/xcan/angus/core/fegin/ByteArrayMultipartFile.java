package cloud.xcan.angus.core.fegin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

public class ByteArrayMultipartFile implements MultipartFile {

  private final String name;
  private final String originalFilename;
  private final String contentType;
  @NonNull
  private final byte[] bytes;

  @Override
  public boolean isEmpty() {
    return this.bytes.length == 0;
  }

  @Override
  public long getSize() {
    return (long) this.bytes.length;
  }

  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(this.bytes);
  }

  @Override
  public void transferTo(File destination) throws IOException {
    FileOutputStream outputStream = null;
    try {
      outputStream = new FileOutputStream(destination);
      outputStream.write(this.bytes);
    } finally {
      if (outputStream != null) {
        outputStream.close();
      }
    }
  }

  public ByteArrayMultipartFile(String name, String originalFilename, String contentType,
      @NonNull byte[] bytes) {
    if (bytes == null) {
      throw new NullPointerException("bytes is marked @NonNull but is null");
    } else {
      this.name = name;
      this.originalFilename = originalFilename;
      this.contentType = contentType;
      this.bytes = bytes;
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getOriginalFilename() {
    return this.originalFilename;
  }

  @Override
  public String getContentType() {
    return this.contentType;
  }

  @Override
  public byte[] getBytes() {
    return this.bytes;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ByteArrayMultipartFile)) {
      return false;
    } else {
      ByteArrayMultipartFile other = (ByteArrayMultipartFile) o;
      Object this$name = this.getName();
      Object other$name = other.getName();
      if (this$name == null) {
        if (other$name != null) {
          return false;
        }
      } else if (!this$name.equals(other$name)) {
        return false;
      }

      label41:
      {
        Object this$originalFilename = this.getOriginalFilename();
        Object other$originalFilename = other.getOriginalFilename();
        if (this$originalFilename == null) {
          if (other$originalFilename == null) {
            break label41;
          }
        } else if (this$originalFilename.equals(other$originalFilename)) {
          break label41;
        }

        return false;
      }

      Object this$contentType = this.getContentType();
      Object other$contentType = other.getContentType();
      if (this$contentType == null) {
        if (other$contentType != null) {
          return false;
        }
      } else if (!this$contentType.equals(other$contentType)) {
        return false;
      }

      if (!Arrays.equals(this.getBytes(), other.getBytes())) {
        return false;
      } else {
        return true;
      }
    }
  }

}
