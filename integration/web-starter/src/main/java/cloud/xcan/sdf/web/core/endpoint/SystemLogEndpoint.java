package cloud.xcan.sdf.web.core.endpoint;

import static cloud.xcan.sdf.spec.SpecConstant.UTF8;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_READ_LINE_NUM;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.spec.experimental.StandardCharsets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Slf4j
@Endpoint(id = "systemlog")
public class SystemLogEndpoint {

  public static final String FILE_PATH_PROPERTY = "logging.file.path";

  /**
   * The name of the Spring property that contains the directory where log files are written.
   */
  private final String path;

  public SystemLogEndpoint(String path) {
    this.path = path;
  }

  @ReadOperation
  public List<String> fileList() {
    List<String> filenames = new ArrayList<>();
    Resource logFileResource = getLogFileResource();
    if (logFileResource == null) {
      return filenames;
    }
    try {
      File logPath = logFileResource.getFile();
      if (logPath.exists()) {
        File[] all = logPath.listFiles();
        if (nonNull(all)) {
          for (File file : all) {
            if (file.isFile()) {
              filenames.add(file.getName());
            }
          }
        }
      }
      filenames.sort(Comparator.reverseOrder());
    } catch (IOException e) {
      log.error(e.getMessage());
    }
    return filenames;
  }

  @ReadOperation(produces = "text/plain; charset=UTF-8")
  public String fileDetail(@Selector String name, Integer linesNum, Boolean tail) {
    if (isNull(linesNum) || linesNum <= 0) {
      linesNum = DEFAULT_READ_LINE_NUM;
    }
    File file = new File(this.path.concat(File.separator).concat(name));
    if (!file.exists()) {
      log.warn("System log request file {} does not exist", name);
      return "";
    }
    if (file.isFile()) {
      if (nonNull(tail) && tail) {
        return readFromLast(file, linesNum);
      } else {
        return readFromHead(file, linesNum);
      }
    }
    return "";
  }

  @WriteOperation
  public void fileClear(@Selector String name) {
    File file = new File(path.concat(File.separator).concat(name));
    if (file.exists() && file.isFile()) {
      try {
        if (isRollingLog(name)) {
          if (!file.delete()) {
            emptyFile(file);
          }
        } else {
          emptyFile(file);
        }
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    }
  }

  private void emptyFile(File file) throws IOException {
    FileWriter writer = new FileWriter(file);
    writer.write("");
    writer.flush();
    writer.close();
  }

  /**
   * Read n lines from the head of the file
   */
  private String readFromHead(File file, int linesNum) {
    StringBuilder lineBuilder = new StringBuilder();
    RandomAccessFile randomAccessFile = null;
    try {
      randomAccessFile = new RandomAccessFile(file, "r");
      String line;
      for (int i = 0; i <= linesNum; i++) {
        line = randomAccessFile.readLine();
        if (null != line) {
          lineBuilder.append(new String(line.getBytes(StandardCharsets.ISO_8859_1), UTF8))
              .append("\n");
        }
      }
    } catch (IOException e) {
      log.error(e.getMessage());
    } finally {
      if (randomAccessFile != null) {
        try {
          randomAccessFile.close();
        } catch (IOException e) {
          log.error(e.getMessage());
        }
      }
    }
    return lineBuilder.toString();
  }

  /**
   * Read n lines from the end of the file
   */
  private String readFromLast(File file, int linesNum) {
    RandomAccessFile rf = null;
    try {
      rf = new RandomAccessFile(file, "r");
      byte[] c = new byte[1];
      for (long pointer = rf.length(), lineSeparatorNum = 0;
          pointer >= 0 && lineSeparatorNum < linesNum; ) {
        rf.seek(pointer--);
        int readLength = rf.read(c);
        if (readLength != -1 && c[0] == 10) {
          lineSeparatorNum++;
        }
        if (pointer == -1 && lineSeparatorNum < linesNum) {
          rf.seek(0);
        }
      }
      byte[] tempbytes = new byte[(int) (rf.length() - rf.getFilePointer())];
      rf.readFully(tempbytes);
      return new String(tempbytes, UTF8);
    } catch (Exception e) {
      log.error(e.getMessage());
    } finally {
      if (rf != null) {
        try {
          rf.close();
        } catch (IOException e) {
          log.error(e.getMessage());
        }
      }
    }
    return "";
  }

  private Resource getLogFileResource() {
    if (this.path == null) {
      log.warn("Missing 'logging.file.name' properties");
      return null;
    }
    return new FileSystemResource(this.path);
  }

  private boolean isRollingLog(String fileName) {
    return fileName.contains("gc.log.") || fileName.contains("-20");
  }
}
