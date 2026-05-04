package cloud.xcan.angus.spec.utils.file.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe buffered writer using UTF-8. Honors the {@code append} flag (fixed in this revision:
 * previously {@link java.io.FileWriter} was constructed without {@code append}).
 */
public final class BufferedFileWriter implements BaseFileWriter {

  private final File file;
  private final boolean append;
  private final BufferedWriter writer;
  private final ReentrantLock lock = new ReentrantLock();

  public BufferedFileWriter(File file, boolean append) throws IOException {
    this.file = Objects.requireNonNull(file, "file");
    this.append = append;
    File parent = file.getParentFile();
    if (parent != null) {
      Files.createDirectories(parent.toPath());
    }
    this.writer = new BufferedWriter(
        new OutputStreamWriter(new FileOutputStream(file, append), StandardCharsets.UTF_8));
  }

  @Override
  public void write(String row) throws IOException {
    lock.lock();
    try {
      writer.write(row);
      writer.newLine();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void write(String[] rows) throws IOException {
    for (String row : rows) {
      write(row);
    }
  }

  @Override
  public void writeAndFlush(String row) throws IOException {
    lock.lock();
    try {
      writer.write(row);
      writer.newLine();
      writer.flush();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void writeAndFlush(String[] rows) throws IOException {
    lock.lock();
    try {
      for (String row : rows) {
        writer.write(row);
        writer.newLine();
      }
      writer.flush();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void flush() throws IOException {
    lock.lock();
    try {
      writer.flush();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void close() throws IOException {
    lock.lock();
    try {
      writer.flush();
      writer.close();
    } finally {
      lock.unlock();
    }
  }

  public File getFile() {
    return file;
  }

  public boolean isAppend() {
    return append;
  }

  /**
   * Exposes the underlying writer; external writes bypass this class’s lock — use only when no
   * concurrent calls to other methods occur.
   */
  public BufferedWriter getWriter() {
    return writer;
  }
}
