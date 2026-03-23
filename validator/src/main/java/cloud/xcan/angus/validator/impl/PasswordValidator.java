package cloud.xcan.angus.validator.impl;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

import cloud.xcan.angus.validator.Password;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class PasswordValidator implements ConstraintValidator<Password, String> {

  private static final String SPECIAL_CHAR_SOURCE = "`-=[];',./~!@#$%^&*()_+{}:\"<>?";
  private static final boolean[] SPECIAL_CHAR_ASCII = new boolean[128];

  static {
    for (int i = 0; i < SPECIAL_CHAR_SOURCE.length(); i++) {
      char c = SPECIAL_CHAR_SOURCE.charAt(i);
      if (c < 128) {
        SPECIAL_CHAR_ASCII[c] = true;
      }
    }
  }

  private Password annotation;

  private static boolean isAllowedSpecialChar(char c) {
    return c < 128 && SPECIAL_CHAR_ASCII[c];
  }

  private static double calcRepeatRate(String value) {
    int len = value.length();
    if (len <= 1) {
      return 0;
    }
    Set<Character> distinct = new HashSet<>(Math.min(len, 256));
    for (int i = 0; i < len; i++) {
      distinct.add(value.charAt(i));
    }
    return 1 - ((double) distinct.size() / len);
  }

  @Override
  public void initialize(Password constraintAnnotation) {
    annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (null == value) {
      return annotation.allowNull();
    }

    if (value.length() < annotation.minSize() || value.length() > annotation.maxSize()) {
      return false;
    }

    // Contains at least two types of char
    int typesNum = calcTypesNum(value);
    if (typesNum == -1 || typesNum < annotation.allowMinTypeNum()) {
      return false;
    }

    // The repetition rate cannot exceed 0.5, Such as 6 characters at least 3 characters are not the same
    return !(calcRepeatRate(value) > annotation.allowMaxRepeatRate());
  }

  private int calcTypesNum(String value) {
    boolean hasUpperCase = false;
    boolean hasLowerCase = false;
    boolean hasDigits = false;
    boolean hasSpecialChar = false;

    for (int i = 0; i < value.length(); i++) {
      char chr = value.charAt(i);
      boolean supported = false;
      if (annotation.allowUpperCase() && isUpperCase(chr)) {
        hasUpperCase = true;
        supported = true;
      }
      if (annotation.allowLowerCase() && isLowerCase(chr)) {
        hasLowerCase = true;
        supported = true;
      }
      if (annotation.allowDigits() && isDigit(chr)) {
        hasDigits = true;
        supported = true;
      }
      if (annotation.allowSpecialChar() && isAllowedSpecialChar(chr)) {
        hasSpecialChar = true;
        supported = true;
      }
      if (!supported) {
        return -1;
      }
    }
    int typesNum = 0;
    if (hasUpperCase) {
      typesNum++;
    }
    if (hasLowerCase) {
      typesNum++;
    }
    if (hasDigits) {
      typesNum++;
    }
    if (hasSpecialChar) {
      typesNum++;
    }
    return typesNum;
  }
}
