
package cloud.xcan.sdf.idgen;


import cloud.xcan.sdf.idgen.exception.IdGenerateException;

/**
 * Represents a unique pk generator.
 *
 */
public interface UidGenerator {

  /**
   * Get a unique ID
   *
   * @return UID
   */
  long getUID() throws IdGenerateException;

  /**
   * Parse the UID into elements which are used to generate the UID. <br> Such as datetime &
   * instanceId & sequence...
   *
   * @return Parsed info
   */
  String parseUID(long uid);

}
