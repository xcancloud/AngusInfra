package cloud.xcan.angus.spec.locale;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.spec.experimental.Value;
import java.util.Locale;
import java.util.Objects;
import org.springframework.context.MessageSource;

public class MessageHolder {

  private static MessageSource messageSource;

  private MessageHolder() {
  }

  public static String message(String key) {
    return MessageHolder.message(key, null, SdfLocaleHolder.getLocale());
  }

  public static String message(String key, Object[] args) {
    return MessageHolder.message(key, args, SdfLocaleHolder.getLocale());
  }

  public static String message(String key, Locale locale) {
    return MessageHolder.message(key, null, locale);
  }

  public static String message(String key, Object[] args, Locale locale) {
    if (Objects.isNull(messageSource)) {
      return key;
    }
    return messageSource.getMessage(key, safeArgMessage(args), key, locale);
  }

  public static MessageSource getMessageSource() {
    return messageSource;
  }

  public static void setMessageSource(MessageSource messageSource) {
    MessageHolder.messageSource = messageSource;
  }

  public static Object[] safeArgMessage(Object[] args) {
    if (isEmpty(args)) {
      return new Object[]{};
    }
    Object[] safeEnumArgs = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      if (args[i] == null) {
        safeEnumArgs[i] = null;
      } else if (args[i] instanceof EnumMessage) {
        safeEnumArgs[i] = ((EnumMessage<?>) args[i]).getMessage();
      } else if (args[i] instanceof Value) {
        safeEnumArgs[i] = ((Value<?>) args[i]).getValue();
      } else if (args[i] instanceof Long) {
        safeEnumArgs[i] = String.valueOf(args[i]);
      } else if (args[i] instanceof Integer) {
        safeEnumArgs[i] = String.valueOf(args[i]);
      } else if (args[i].getClass().isPrimitive()) {
        safeEnumArgs[i] = String.valueOf(args[i]);
      } else if (args[i].getClass().isEnum()) {
        safeEnumArgs[i] = ((Enum<?>) args[i]).name();
      } else {
        safeEnumArgs[i] = args[i];
      }
    }
    return safeEnumArgs;
  }
}
