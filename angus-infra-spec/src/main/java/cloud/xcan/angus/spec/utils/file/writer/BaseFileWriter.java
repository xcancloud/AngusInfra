package cloud.xcan.angus.spec.utils.file.writer;

import java.io.IOException;

/**
 * Line-oriented text output; {@link #close()} should be used in try-with-resources.
 */
public interface BaseFileWriter extends AutoCloseable {

  void write(String row) throws IOException;

  void write(String[] rows) throws IOException;

  void writeAndFlush(String row) throws IOException;

  void writeAndFlush(String[] rows) throws IOException;

  void flush() throws IOException;

  @Override
  void close() throws IOException;
}
