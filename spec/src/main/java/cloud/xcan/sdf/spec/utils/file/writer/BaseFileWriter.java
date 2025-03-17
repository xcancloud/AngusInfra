package cloud.xcan.sdf.spec.utils.file.writer;

import java.io.IOException;

public interface BaseFileWriter {

  void write(String row) throws IOException;

  void write(String[] rows) throws IOException;

  void writeAndFlush(String row) throws IOException;

  void writeAndFlush(String[] rows) throws IOException;

  void flush() throws IOException;

  void close() throws IOException;

}
