package cloud.xcan.sdf.spec.utils.crypto;


public class CryptoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CryptoException(String message) {
    super(message);
  }

  public CryptoException(String message, Throwable cause) {
    super(message, cause);
  }

}
