package cloud.xcan.angus.spec.utils.file.filter;

/**
 * File filter that accepts all files ending with .JAR. This filter is case insensitive.
 */
public final class JarFileFilter extends ExtensionFileFilter {

  /**
   * The extension that this filter will search for.
   */
  private static final String JAR_EXTENSION = ".JAR";

  public JarFileFilter() {
    super(JAR_EXTENSION);
  }

}
