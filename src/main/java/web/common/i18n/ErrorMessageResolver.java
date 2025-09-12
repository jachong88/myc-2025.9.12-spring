package web.common.i18n;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ErrorMessageResolver {

  private final MessageSource messageSource;

  public ErrorMessageResolver(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  // Resolve using current request locale (Accept-Language); fallback to the key
  public String resolve(String key, Object... args) {
    Locale locale = LocaleContextHolder.getLocale();
    return messageSource.getMessage(key, args, key, locale);
  }

  // Explicit locale variant
  public String resolveWithLocale(String key, Locale locale, Object... args) {
    Locale effective = (locale == null ? Locale.ENGLISH : locale);
    return messageSource.getMessage(key, args, key, effective);
  }

  // Note: Once an AppException type exists, we can add an overload like:
  // public String resolve(AppException ex) { ... }
}
