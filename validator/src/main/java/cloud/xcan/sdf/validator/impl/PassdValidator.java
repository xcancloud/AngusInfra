package cloud.xcan.sdf.validator.impl;

import static java.lang.Character.isDigit;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.util.stream.Collectors.toSet;

import cloud.xcan.sdf.validator.Passd;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PassdValidator implements ConstraintValidator<Passd, String> {

  public final static Set<Character> SPECIAL_CHARS = "`-=[];',./~!@#$%^&*()_+{}:\"<>?".chars()
      .mapToObj(i -> (char) i).collect(toSet());

  private Passd annotation;

  private static double calcRepeatRate(String value) {
    StringBuilder noRepeat = new StringBuilder();
    List<Character> list = new ArrayList<>();
    char[] cs = value.toCharArray();
    for (char c : cs) {
      if (!list.contains(c)) {
        noRepeat.append(c);
        list.add(c);
      }
    }
    return 1 - ((double) noRepeat.length() / value.length());
  }

  @Override
  public void initialize(Passd constraintAnnotation) {
    annotation = constraintAnnotation;
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (null == value) {
      return annotation.allowNull();
    }

    if (value.length() < annotation.minSize() || value.length() >= annotation.maxSize()) {
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
    int typesNum = 0;
    boolean hasUpperCase = false;
    boolean hasLowerCase = false;
    boolean hasDigits = false;
    boolean hasSpecialChar = false;
    boolean isSupportChar = false;

    for (int i = 0; i < value.length(); ++i) {
      char chr = value.charAt(i);
      if (annotation.allowUpperCase() && !hasUpperCase && isUpperCase(chr)) {
        hasUpperCase = true;
        typesNum++;
      } else if (annotation.allowLowerCase() && !hasLowerCase && isLowerCase(chr)) {
        hasLowerCase = true;
        typesNum++;
      } else if (annotation.allowDigits() && !hasDigits && isDigit(chr)) {
        hasDigits = true;
        typesNum++;
      } else if (annotation.allowSpecialChar() && !hasSpecialChar && SPECIAL_CHARS.contains(chr)) {
        hasSpecialChar = true;
        typesNum++;
      }

      if (annotation.allowUpperCase() && isUpperCase(chr)) {
        isSupportChar = true;
      } else if (annotation.allowLowerCase() && isLowerCase(chr)) {
        isSupportChar = true;
      } else if (annotation.allowDigits() && isDigit(chr)) {
        isSupportChar = true;
      } else if (annotation.allowSpecialChar() && SPECIAL_CHARS.contains(chr)) {
        isSupportChar = true;
      }

      if (!isSupportChar) {
        return -1;
      }
      isSupportChar = false;
    }
    return typesNum;
  }
}
