package cloud.xcan.sdf.spec.utils;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.sdf.spec.utils.StringUtils.containsText;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.sdf.spec.experimental.Assert;
import cloud.xcan.sdf.spec.utils.file.filter.DirectoryFileFilter;
import cloud.xcan.sdf.spec.utils.file.filter.JarFileFilter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.net.ssl.SSLHandshakeException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileUtils extends org.apache.commons.io.FileUtils {

  private FileUtils() { /* no instance */ }

  private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

  public static String readContentFromLocation(String location, List<SimpleHttpAuth> auth,
      String encoding) {
    String adjustedLocation = location;
    try {
      if (adjustedLocation.toLowerCase().startsWith("http")) {
        return NetworkUtils.urlToString(adjustedLocation, auth);
      } else if (adjustedLocation.toLowerCase().startsWith("jar:")) {
        final InputStream in = new URI(adjustedLocation).toURL().openStream();
        return IOUtils.toString(in, encoding);
      } else {
        adjustedLocation = location.replaceAll("file:", "");
        File localFile = new File(adjustedLocation);
        if (localFile.exists()) {
          return org.apache.commons.io.FileUtils.readFileToString(localFile, encoding);
        } else {
          adjustedLocation = location.replaceAll("\\\\", "/");
          return ClasspathUtils.loadFileFromClasspath(adjustedLocation);
        }
      }
    } catch (SSLHandshakeException e) {
      final String message = String.format(
          "Unable to read location `%s` due to a SSL configuration error. It is possible that the server SSL certificate is invalid, self-signed, or has an untrusted Certificate Authority.",
          adjustedLocation);
      throw new IllegalStateException(message, e);
    } catch (Throwable e) {
      throw new IllegalStateException(
          String.format("Unable to read location `%s`", adjustedLocation), e);
    }
  }

  public static byte[] readBytesFromLocation(String location, List<SimpleHttpAuth> auth) {
    String adjustedLocation = location;
    try {
      if (adjustedLocation.toLowerCase().startsWith("http")) {
        return NetworkUtils.urlToBytes(adjustedLocation, auth);
      } else if (adjustedLocation.toLowerCase().startsWith("jar:")) {
        try (InputStream is = new URI(adjustedLocation).toURL().openStream()) {
          return is.readAllBytes();
        }
      } else {
        adjustedLocation = location.replaceAll("file:", "");
        File localFile = new File(adjustedLocation);
        if (localFile.exists()) {
          try (InputStream is = new FileInputStream(localFile)) {
            return is.readAllBytes();
          }
        } else {
          adjustedLocation = location.replaceAll("\\\\", "/");
          try (InputStream is = ClasspathUtils.loadFileInputStreamFromClasspath(adjustedLocation)) {
            return is.readAllBytes();
          }
        }
      }
    } catch (SSLHandshakeException e) {
      final String message = String.format(
          "Unable to read location `%s` due to a SSL configuration error. It is possible that the server SSL certificate is invalid, self-signed, or has an untrusted Certificate Authority.",
          adjustedLocation);
      throw new IllegalStateException(message, e);
    } catch (Throwable e) {
      throw new IllegalStateException(
          String.format("Unable to read location `%s`", adjustedLocation), e);
    }
  }

  public static InputStream readInputStreamFromLocation(String location,
      List<SimpleHttpAuth> auth) {
    String adjustedLocation = location;
    try {
      if (adjustedLocation.toLowerCase().startsWith("http")) {
        return NetworkUtils.urlToInputStream(adjustedLocation, auth);
      } else if (adjustedLocation.toLowerCase().startsWith("jar:")) {
        return new URI(adjustedLocation).toURL().openStream();
      } else {
        adjustedLocation = location.replaceAll("file:", "");
        File localFile = new File(adjustedLocation);
        if (localFile.exists()) {
          return new FileInputStream(localFile);
        } else {
          adjustedLocation = location.replaceAll("\\\\", "/");
          return ClasspathUtils.loadFileInputStreamFromClasspath(adjustedLocation);
        }
      }
    } catch (SSLHandshakeException e) {
      final String message = String.format(
          "Unable to read location `%s` due to a SSL configuration error. It is possible that the server SSL certificate is invalid, self-signed, or has an untrusted Certificate Authority.",
          adjustedLocation);
      throw new IllegalStateException(message, e);
    } catch (Throwable e) {
      throw new IllegalStateException(
          String.format("Unable to read location `%s`", adjustedLocation), e);
    }
  }

  public static boolean isDirectoryEmpty(File directory) throws IOException {
    DirectoryStream<Path> stream = Files.newDirectoryStream(directory.toPath());
    return !stream.iterator().hasNext();
  }

  public static List<String> readLines(Path path, boolean ignoreComments) throws IOException {
    File file = path.toFile();
    if (!file.isFile()) {
      return new ArrayList<>();
    }

    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (ignoreComments && !line.startsWith("#") && !lines.contains(line)) {
          lines.add(line);
        }
      }
    }
    return lines;
  }

  public static List<String> readLines(String filePath, int startLine, int numLines)
      throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      int currentLine = 0;
      String line;
      while ((line = reader.readLine()) != null && currentLine < startLine + numLines) {
        currentLine++;
        if (currentLine >= startLine) {
          lines.add(line);
        }
      }
    }
    return lines;
  }

  public static List<String> readLines(String filePath, int startPosition)
      throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      for (int i = 0; i < startPosition; i++) {
        reader.readLine();
      }
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line);
      }
    }
    return lines;
  }

  /**
   * Use {@link #writeLines(Collection, Path)} instead.
   */
  @Deprecated
  public static void writeLines(Collection<String> lines, File file) throws IOException {
    writeLines(lines, file.toPath());
  }

  public static void writeLines(Collection<String> lines, Path path) throws IOException {
    Files.write(path, lines, StandardCharsets.UTF_8);
  }

  public static void clearFile(String filePath) throws IOException {
    if (isExisted(filePath)) {
      FileWriter fw = new FileWriter(filePath, false);
      fw.write("");
      fw.close();
    }
  }

  public static void clearFile(File filePath) throws IOException {
    if (isExisted(filePath)) {
      FileWriter fw = new FileWriter(filePath, false);
      fw.write("");
      fw.close();
    }
  }

  /**
   * Delete a file or recursively delete a folder, do not follow symlinks.
   *
   * @param path the file or folder to delete
   * @throws IOException if something goes wrong
   */
  public static void deleteRecursive(Path path) throws IOException {
    Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (!attrs.isSymbolicLink()) {
          Files.delete(path);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  public static void deleteRecursive(Path directory, boolean ignoreSymbolicLink)
      throws IOException {
    if (Files.isDirectory(directory)) {
      Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (ignoreSymbolicLink && attrs.isSymbolicLink()) {
            return FileVisitResult.CONTINUE;
          }
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  public static String readFile(Class<?> testClass, String fileName) {
    URL res = testClass.getResource(fileName);
    return readFile(res);
  }

  public static String readFile(URL url) {
    try {
      return readFile(url.openStream());
    } catch (IOException var2) {
      throw new RuntimeException(var2);
    }
  }

  public static String readFile(InputStream file) {
    try {
      BufferedInputStream stream = new BufferedInputStream(file);
      Throwable var2 = null;
      try {
        byte[] buff = new byte[1024];
        StringBuilder builder = new StringBuilder();

        int read;
        while ((read = stream.read(buff)) != -1) {
          builder.append(new String(buff, 0, read, StandardCharsets.UTF_8));
        }
        return builder.toString();
      } catch (Throwable var16) {
        var2 = var16;
        throw var16;
      } finally {
        if (var2 != null) {
          try {
            stream.close();
          } catch (Throwable var15) {
            var2.addSuppressed(var15);
          }
        } else {
          stream.close();
        }

      }
    } catch (IOException var18) {
      throw new RuntimeException(var18);
    }
  }

  public static List<File> getJars(Path folder) {
    List<File> bucket = new ArrayList<>();
    getJars(bucket, folder);
    return bucket;
  }

  private static void getJars(final List<File> bucket, Path folder) {
    FileFilter jarFilter = new JarFileFilter();
    FileFilter directoryFilter = new DirectoryFileFilter();

    if (Files.isDirectory(folder)) {
      File[] jars = folder.toFile().listFiles(jarFilter);
      for (int i = 0; (jars != null) && (i < jars.length); ++i) {
        bucket.add(jars[i]);
      }

      File[] directories = folder.toFile().listFiles(directoryFilter);
      for (int i = 0; (directories != null) && (i < directories.length); ++i) {
        File directory = directories[i];
        getJars(bucket, directory.toPath());
      }
    }
  }

  /**
   * Delete a file (not recursively) and ignore any errors.
   *
   * @param path the path to delete
   */
  public static void optimisticDelete(Path path) {
    if (path == null) {
      return;
    }

    try {
      Files.delete(path);
    } catch (IOException ignored) {
      // ignored
    }
  }

  /**
   * Unzip a zip file in a directory that has the same name as the zip file. For example if the zip
   * file is {@code my-plugin.zip} then the resulted directory is {@code my-plugin}.
   *
   * @param filePath the file to evaluate
   * @return Path of unzipped folder or original path if this was not a zip file
   * @throws IOException on error
   */
  public static Path expandIfZip(Path filePath) throws IOException {
    if (!isZipFile(filePath)) {
      return filePath;
    }

    FileTime pluginZipDate = Files.getLastModifiedTime(filePath);
    String fileName = filePath.getFileName().toString();
    String directoryName = fileName.substring(0, fileName.lastIndexOf("."));
    Path pluginDir = filePath.resolveSibling(directoryName);

    if (!Files.exists(pluginDir)
        || pluginZipDate.compareTo(Files.getLastModifiedTime(pluginDir)) > 0) {
      // expand '.zip' file
      UnzipUtils unzip = new UnzipUtils();
      unzip.setSource(filePath.toFile());
      unzip.setDestination(pluginDir.toFile());
      unzip.extract();
      log.debug("Expanded plugin zip {} in {}", filePath.getFileName(), pluginDir.getFileName());
    }
    return pluginDir;
  }

  /**
   * Return true only if path is a zip file.
   *
   * @param path to a file/dir
   * @return true if file with {@code .zip} ending
   */
  public static boolean isZipFile(Path path) {
    return Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".zip");
  }

  /**
   * Return true only if path is a jar file.
   *
   * @param path to a file/dir
   * @return true if file with {@code .jar} ending
   */
  public static boolean isJarFile(Path path) {
    return Files.isRegularFile(path) && path.toString().toLowerCase().endsWith(".jar");
  }

  public static Path getPath(Path path, String first, String... more) throws IOException {
    URI uri = path.toUri();
    if (isJarFile(path)) {
      String pathString = path.toAbsolutePath().toString();
      // transformation for Windows OS
      pathString = StringUtils.addStart(pathString.replace("\\", "/"), "/");
      // space is replaced with %20
      pathString = pathString.replace(" ", "%20");
      uri = URI.create("jar:file:" + pathString);
    }

    return getPath(uri, first, more);
  }

  public static Path getPath(URI uri, String first, String... more) throws IOException {
    return getFileSystem(uri).getPath(first, more);
  }

  public static void closePath(Path path) {
    if (path != null) {
      try {
        path.getFileSystem().close();
      } catch (Exception e) {
        // close silently
      }
    }
  }

  /**
   * Finds a path with various endings or null if not found.
   *
   * @param basePath the base name
   * @param endings  a list of endings to search for
   * @return new path or null if not found
   */
  public static Path findWithEnding(Path basePath, String... endings) {
    for (String ending : endings) {
      Path newPath = basePath.resolveSibling(basePath.getFileName() + ending);
      if (Files.exists(newPath)) {
        return newPath;
      }
    }
    return null;
  }

  public static Path findFile(Path directoryPath, String fileName) {
    File[] files = directoryPath.toFile().listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          if (file.getName().equals(fileName)) {
            return file.toPath();
          }
        } else if (file.isDirectory()) {
          Path foundFile = findFile(file.toPath(), fileName);
          if (foundFile != null) {
            return foundFile;
          }
        }
      }
    }
    return null;
  }

  public static Path findWithLike(Path directoryPath, String fileName) {
    File[] files = directoryPath.toFile().listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          if (file.getName().contains(fileName)) {
            return file.toPath();
          }
        } else if (file.isDirectory()) {
          Path foundFile = findWithLike(file.toPath(), fileName);
          if (foundFile != null) {
            return foundFile;
          }
        }
      }
    }
    return null;
  }

  public static Path findWithLike(Path directoryPath, String... likes) {
    File[] files = directoryPath.toFile().listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          if (containsText(file.getName(), likes)) {
            return file.toPath();
          }
        } else if (file.isDirectory()) {
          Path foundFile = findWithLike(file.toPath(), likes);
          if (foundFile != null) {
            return foundFile;
          }
        }
      }
    }
    return null;
  }

  public static void compress(String[] sources, String output) throws Exception {
    log.debug("Extract content of {} to {}", String.join(",", sources), output);
    FileOutputStream fos = new FileOutputStream(output);
    ZipOutputStream zos = new ZipOutputStream(fos);
    for (String file : sources) {
      FileInputStream fis = new FileInputStream(file);
      byte[] buffer = new byte[2048];
      int len;
      zos.putNextEntry(new ZipEntry(file));
      while ((len = fis.read(buffer)) > 0) {
        zos.write(buffer, 0, len);
      }
      fis.close();
      zos.closeEntry();
    }
    zos.close();
  }

  /**
   * Compress by relative path.
   *
   * @param sources Full path files
   * @param targets Compressed relative path files, parameter must correspond to sources
   * @param output  Output file path
   */
  public static void compress(String[] sources, String[] targets, String output) throws Exception {
    log.debug("Compress content of {} to {}", String.join(",", sources), output);
    Assert.assertTrue(isNotEmpty(sources) && isNotEmpty(targets)
        && sources.length == targets.length, "Parameter is empty or inconsistent");
    FileOutputStream fos = new FileOutputStream(output);
    ZipOutputStream zos = new ZipOutputStream(fos);
    for (int i = 0; i < targets.length; i++) {
      String target = targets[i];
      File file = new File(sources[i]);
      FileInputStream fis = new FileInputStream(file);
      byte[] buffer = new byte[2048];
      int len;
      zos.putNextEntry(new ZipEntry(target));
      while ((len = fis.read(buffer)) > 0) {
        zos.write(buffer, 0, len);
      }
      fis.close();
      zos.closeEntry();
    }
    zos.close();
  }

  /**
   * Extract the content of zip file ({@code source}) to destination directory. If destination
   * directory already exists it will be deleted before.
   *
   * @param source Holds path to zip file.
   * @param output Holds the destination directory. File will be unzipped into the destination
   *               directory.
   */
  public static void extract(String source, String output) throws Exception {
    log.debug("Extract content of {} to {}", source, output);

    File destDir = new File(output);
    if (destDir.exists()) {
      // Delete the directory extracted before
      String sourceBasename = FilenameUtils.getBaseName(source);
      File destination = new File(output.endsWith(File.separator) ? output + sourceBasename
          : output + File.separator + sourceBasename);
      if (destination.exists() && destination.isDirectory()) {
        FileUtils.deleteRecursive(destination.toPath());
      }
    } else {
      // Create the destination directory
      destDir.mkdir();
    }

    ZipInputStream zipIn = new ZipInputStream(new FileInputStream(source));
    ZipEntry entry = zipIn.getNextEntry();
    while (entry != null) {
      String filePath = output + File.separator + entry.getName();
      if (!entry.isDirectory()) {
        writeFile(zipIn, filePath);
      } else {
        File dir = new File(filePath);
        dir.mkdir();
      }
      zipIn.closeEntry();
      entry = zipIn.getNextEntry();
    }
    zipIn.close();
  }

  private static void writeFile(ZipInputStream zipIn, String filePath) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
    byte[] bytesIn = new byte[4096];
    int read = 0;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }

  private static FileSystem getFileSystem(URI uri) throws IOException {
    try {
      return FileSystems.getFileSystem(uri);
    } catch (FileSystemNotFoundException e) {
      return FileSystems.newFileSystem(uri, Collections.<String, String>emptyMap());
    }
  }

  public static String formatRoundingSize(long size) {
    if (size <= 0) {
      return "0B";
    }
    final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("###0").format(size / Math.pow(1024, digitGroups))
        + units[digitGroups];
  }

  public static String formatSize(long size) {
    if (size <= 0) {
      return "0B";
    }
    final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("###0.#").format(size / Math.pow(1024, digitGroups))
        + units[digitGroups];
  }

  public static boolean isExisted(String file) {
    return isNotEmpty(file) && isExisted(new File(file));
  }

  public static boolean isExisted(File file) {
    return nonNull(file) && file.exists();
  }

  public static String getParentPath(String filePath) {
    File file = new File(filePath);
    return file.getParent();
  }
}
