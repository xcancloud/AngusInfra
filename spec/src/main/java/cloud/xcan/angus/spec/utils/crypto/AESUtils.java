package cloud.xcan.angus.spec.utils.crypto;

import static cloud.xcan.angus.spec.SpecConstant.DEFAULT_ENCODING;
import static java.nio.charset.StandardCharsets.UTF_8;

import cloud.xcan.angus.api.pojo.Pair;
import cloud.xcan.angus.spec.experimental.Assert;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AESUtils {

  private static final Logger log = LoggerFactory.getLogger(AESUtils.class);

  public static final String ALGORITHM = "AES";
  public static final String ALGORITHM_TRANSFORMATION = "AES/CBC/PKCS5Padding";
  /** Legacy PRNG name used only when a non-null seed is supplied (stable derivation vs older releases). */
  public static final String KEY_ALGORITHM = "SHA1PRNG";
  public static final int KEY_SIZE = 256;

  private AESUtils() {
  }

  private static SecureRandom newSecureRandom() {
    try {
      return SecureRandom.getInstanceStrong();
    } catch (NoSuchAlgorithmException e) {
      return new SecureRandom();
    }
  }

  public static String generateKey(int keySize) {
    return generateKey(null, keySize, DEFAULT_ENCODING);
  }

  public static String generateKey(byte[] seed) {
    return generateKey(seed, KEY_SIZE, DEFAULT_ENCODING);
  }

  public static String generateKey(byte[] seed, int keySize) {
    return generateKey(seed, keySize, DEFAULT_ENCODING);
  }

  public static String generateKey(int keySize, String encodingType) {
    return generateKey(null, keySize, encodingType);
  }

  private static String generateKey(byte[] seed, int keySize, String encodingType) {
    Charset cs = Charset.forName(encodingType);
    byte[] encoded = Base64.getEncoder().encode(generateKey0(seed, keySize).getEncoded());
    return new String(encoded, cs);
  }

  public static SecretKey generateKey0(byte[] seed, int keySize) {
    try {
      KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
      if (seed != null && seed.length > 0) {
        SecureRandom sr = SecureRandom.getInstance(KEY_ALGORITHM);
        sr.setSeed(seed);
        keyGen.init(keySize, sr);
      } else {
        keyGen.init(keySize, newSecureRandom());
      }
      return keyGen.generateKey();
    } catch (NoSuchAlgorithmException e) {
      log.error("KeyGenerator algorithm not available: {}", ALGORITHM, e);
      throw new CryptoException("No such algorithm: " + ALGORITHM, e);
    }
  }

  public static byte[] encrypt(byte[] encryptKey, byte[] initVector, byte[] content) {
    IvParameterSpec zeroIv = new IvParameterSpec(initVector);
    SecretKeySpec key = new SecretKeySpec(encryptKey, ALGORITHM);
    return encrypt(key, zeroIv, content);
  }

  public static byte[] decrypt(byte[] decryptKey, byte[] initVector, byte[] content) {
    IvParameterSpec zeroIv = new IvParameterSpec(initVector);
    SecretKeySpec key = new SecretKeySpec(decryptKey, ALGORITHM);
    return decrypt(key, zeroIv, content);
  }

  public static byte[] decrypt(SecretKeySpec keySpec, IvParameterSpec initVector, byte[] content) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_TRANSFORMATION);
      cipher.init(Cipher.DECRYPT_MODE, keySpec, initVector);
      return cipher.doFinal(content);
    } catch (Exception e) {
      log.error("AES decrypt failed (iv length={}, key length={})",
          initVector.getIV().length, keySpec.getEncoded().length, e);
      throw new CryptoException("AES decrypt failed", e);
    }
  }

  public static byte[] encrypt(SecretKeySpec keySpec, IvParameterSpec initVector, byte[] content) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_TRANSFORMATION);
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, initVector);
      return cipher.doFinal(content);
    } catch (Exception e) {
      log.error("AES encrypt failed (iv length={}, key length={})",
          initVector.getIV().length, keySpec.getEncoded().length, e);
      throw new CryptoException("AES encrypt failed", e);
    }
  }

  public static String encrypt(String encryptKey, String initVector, String transformation,
      String content) {
    Objects.requireNonNull(content, "content");
    try {
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(Charset.forName(DEFAULT_ENCODING)));
      SecretKeySpec skeySpec = new SecretKeySpec(Base64Utils.decode(encryptKey), ALGORITHM);

      Cipher cipher = Cipher.getInstance(transformation);
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

      byte[] encrypted = cipher.doFinal(content.getBytes(UTF_8));
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception e) {
      log.error("AES string encrypt failed (transformation={})", transformation, e);
      throw new CryptoException("AES encrypt failed", e);
    }
  }

  public static String decrypt(String decryptKey, String initVector, String transformation,
      String content) {
    Objects.requireNonNull(content, "content");
    try {
      IvParameterSpec iv = new IvParameterSpec(initVector.getBytes(Charset.forName(DEFAULT_ENCODING)));
      SecretKeySpec skeySpec = new SecretKeySpec(Base64Utils.decode(decryptKey), ALGORITHM);

      Cipher cipher = Cipher.getInstance(transformation);
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

      byte[] original = cipher.doFinal(Base64.getDecoder().decode(content));

      return new String(original, UTF_8);
    } catch (Exception e) {
      log.error("AES string decrypt failed (transformation={})", transformation, e);
      throw new CryptoException("AES decrypt failed", e);
    }
  }

  /**
   * Derives a key from {@code pair.first()} and encrypts {@code pair.second()}. IV is the first 16
   * characters of the Base64 key string (16 UTF-8 bytes for ASCII), matching historical behavior.
   */
  public static String encrypt(Pair<String, String> pair) {
    Assert.assertNotNull(pair.first(), "Encrypt key is required");
    Assert.assertNotNull(pair.second(), "Encrypt content is required");
    String encryptKey = generateKey(pair.first().getBytes(UTF_8));
    String initVector = encryptKey.substring(0, 16);
    return encrypt(encryptKey, initVector, ALGORITHM_TRANSFORMATION, pair.second());
  }

  public static String decrypt(Pair<String, String> pair) {
    Assert.assertNotNull(pair.first(), "Decrypt key is required");
    Assert.assertNotNull(pair.second(), "Decrypt content is required");
    String decryptKey = generateKey(pair.first().getBytes(UTF_8));
    String initVector = decryptKey.substring(0, 16);
    return decrypt(decryptKey, initVector, ALGORITHM_TRANSFORMATION, pair.second());
  }
}
