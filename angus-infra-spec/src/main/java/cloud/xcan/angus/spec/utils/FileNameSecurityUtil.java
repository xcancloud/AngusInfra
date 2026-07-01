package cloud.xcan.angus.spec.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class FileNameSecurityUtil {

  private FileNameSecurityUtil() {
  }

  // 允许 Unicode 语言字符（包括中文、日文、韩文等）、数字、组合标记，以及常见安全文件名分隔符
  private static final Pattern ALLOWED_CHARS = Pattern.compile(
      "[\\p{L}\\p{N}\\p{M}\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}\\p{IsHangul}._\\-]");

  // 危险文件扩展名黑名单
  private static final String[] DANGEROUS_EXTENSIONS = {
      "exe", "bat", "cmd", "sh", "php", "jsp", "asp", "aspx",
      "jar", "war", "html", "htm", "js", "vbs"
  };

  /**
   * 清理文件名，移除危险字符
   */
  public static String sanitizeFileName(String originalFileName) {
    if (originalFileName == null || originalFileName.trim().isEmpty()) {
      return generateSafeFileName();
    }

    // 移除路径信息，只保留文件名
    String fileName = extractFileName(originalFileName);

    // Unicode规范化，防止特殊字符攻击
    fileName = Normalizer.normalize(fileName, Normalizer.Form.NFKC);

    // 移除或替换危险字符
    StringBuilder safeName = new StringBuilder();
    fileName.codePoints().forEach(codePoint -> {
      String character = new String(Character.toChars(codePoint));
      if (ALLOWED_CHARS.matcher(character).matches()) {
        safeName.append(character);
      } else {
        safeName.append('_'); // 用下划线替换危险字符
      }
    });

    String cleanedName = safeName.toString();

    // 确保文件名不为空
    if (cleanedName.isEmpty()) {
      return generateSafeFileName();
    }

    return cleanedName;
  }

  /**
   * 从完整路径中提取文件名
   */
  private static String extractFileName(String filePath) {
    if (filePath == null) {
      return "";
    }

    // 处理不同操作系统的路径分隔符
    String fileName = filePath;
    int lastUnixPos = fileName.lastIndexOf('/');
    int lastWindowsPos = fileName.lastIndexOf('\\');
    int index = Math.max(lastUnixPos, lastWindowsPos);

    if (index != -1) {
      fileName = fileName.substring(index + 1);
    }

    return fileName;
  }

  /**
   * 生成安全的随机文件名
   */
  public static String generateSafeFileName() {
    return UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * 生成带时间戳的安全文件名
   */
  public static String generateTimestampFileName(String originalFileName) {
    String extension = getFileExtension(originalFileName);
    String baseName = generateSafeFileName();
    String timestamp = String.valueOf(System.currentTimeMillis());

    if (extension.isEmpty()) {
      return baseName + "_" + timestamp;
    } else {
      return baseName + "_" + timestamp + "." + extension;
    }
  }

  /**
   * 获取文件扩展名（小写）
   */
  public static String getFileExtension(String fileName) {
    if (fileName == null || fileName.lastIndexOf('.') == -1) {
      return "";
    }

    String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
    return extension.toLowerCase(Locale.ROOT);
  }

  /**
   * 检查文件扩展名是否安全
   */
  public static boolean isExtensionSafe(String fileName) {
    String extension = getFileExtension(fileName);

    // 检查黑名单
    for (String dangerousExt : DANGEROUS_EXTENSIONS) {
      if (dangerousExt.equalsIgnoreCase(extension)) {
        return false;
      }
    }

    return true;
  }

  /**
   * 完整的文件名安全检查和处理
   */
  public static String processUploadFileName(String originalFileName,
      boolean useTimestamp,
      boolean checkExtension) {

    // 检查文件扩展名
    if (checkExtension && !isExtensionSafe(originalFileName)) {
      throw new SecurityException("Unsafe file type: " + originalFileName);
    }

    // 清理文件名
    String safeFileName = sanitizeFileName(originalFileName);

    // 如果需要时间戳，生成带时间戳的文件名
    if (useTimestamp) {
      safeFileName = generateTimestampFileName(safeFileName);
    }

    return safeFileName;
  }
}
