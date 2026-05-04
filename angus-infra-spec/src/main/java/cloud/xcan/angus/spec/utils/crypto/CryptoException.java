package cloud.xcan.angus.spec.utils.crypto;

/**
 * Unchecked exception for cryptographic operation failures.
 */
public final class CryptoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public CryptoException(String message) {
    super(message);
  }

  public CryptoException(String message, Throwable cause) {
    super(message, cause);
  }

  public CryptoException(Throwable cause) {
    super(cause);
  }
}
