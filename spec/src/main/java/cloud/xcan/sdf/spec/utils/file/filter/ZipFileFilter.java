package cloud.xcan.sdf.spec.utils.file.filter;

/**
 * File filter that accepts all files ending with .ZIP. This filter is case insensitive.
 */
public class ZipFileFilter extends ExtensionFileFilter {

  /**
   * The extension that this filter will search for.
   */
  private static final String ZIP_EXTENSION = ".ZIP";

  public ZipFileFilter() {
    super(ZIP_EXTENSION);
  }

}
