package cloud.xcan.angus.spec.utils;

public class ExcelUtils {

  public static final int DEFAULT_CHARACTER_WIDTH = 256;

  /**
   * Calculate column width based on string content
   *
   * @param content Content
   * @return Column width
   */
  public static int calculateColumnWidth(String content) {
    if (content == null) {
      return 8 * DEFAULT_CHARACTER_WIDTH;  // Default 8 character width
    }

    int width = 0;
    for (char c : content.toCharArray()) {
      if (isChinese(c)) {
        width += 2;  // Chinese character takes 2 width units
      } else {
        width += 1;  // English character takes 1 width unit
      }
    }

    // Add some margin to prevent text cutoff
    width += 2;

    return width * DEFAULT_CHARACTER_WIDTH;
  }

  /**
   * Check if a character is Chinese
   */
  private static boolean isChinese(char c) {
    return String.valueOf(c).matches("[\\u4E00-\\u9FA5]");
  }
}
