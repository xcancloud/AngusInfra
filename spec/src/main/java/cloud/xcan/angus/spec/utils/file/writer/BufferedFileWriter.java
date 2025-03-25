package cloud.xcan.angus.spec.utils.file.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class BufferedFileWriter implements BaseFileWriter {

  private File file;

  private boolean append;

  private final BufferedWriter writer;

  private final ReentrantLock lock = new ReentrantLock();

  public BufferedFileWriter(File file, boolean append) throws IOException {
    this.initFile(file, append);
    this.writer = new BufferedWriter(new FileWriter(file));
  }

  private void initFile(File file, boolean append) throws IOException {
    if (file.exists()) {
      if (!append) {
        file.delete();
        file.createNewFile();
      }
    } else {
      file.createNewFile();
    }
    this.append = append;
    this.file = file;
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
    flush();
    writer.close();
  }

  public File getFile() {
    return file;
  }

  public boolean isAppend() {
    return append;
  }

  public BufferedWriter getWriter() {
    return writer;
  }
}
