package cloud.xcan.angus.core.utils;

import static cloud.xcan.angus.core.utils.BeanFieldUtils.getNullPropertyNames;
import static cloud.xcan.angus.remote.search.SearchCriteria.equal;
import static cloud.xcan.angus.remote.search.SearchCriteria.greaterThanEqual;
import static cloud.xcan.angus.remote.search.SearchCriteria.in;
import static cloud.xcan.angus.remote.search.SearchCriteria.lessThanEqual;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DAY_FORMAT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_HOUR_FORMAT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_MONTH_FORMAT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_WEEK_FORMAT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_YEAR_FORMAT;
import static cloud.xcan.angus.spec.utils.ClassUtils.classSafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.lang.Character.isDigit;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import cloud.xcan.angus.api.enums.PasswordStrength;
import cloud.xcan.angus.api.obf.Str0;
import cloud.xcan.angus.core.biz.ResourceName;
import cloud.xcan.angus.core.jpa.repository.summary.DateRangeType;
import cloud.xcan.angus.core.spring.SpringContextHolder;
import cloud.xcan.angus.remote.PageResult;
import cloud.xcan.angus.remote.message.CommSysException;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.SpecConstant;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.Entity;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import cloud.xcan.angus.validator.Passd;
import jakarta.persistence.Id;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.reflections.Reflections;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

public class CoreUtils {

  public final static Set<Character> SPECIAL_CHARS = "`-=[];',./~!@#$%^&*()_+{}:\"<>?".chars()
      .mapToObj(i -> (char) i).collect(toSet());

  private CoreUtils() { /* no instance */ }

  public static <T extends Entity<T, ?>> boolean retainAll(Collection<T> src,
      Collection<T> target) {
    // if this list changed as a result of the call
    boolean changed = false;
    if (isEmpty(src) || isEmpty(target)) {
      return changed;
    }
    Iterator<T> si = src.iterator();
    while (si.hasNext()) {
      boolean hasS = false;
      T t = si.next();
      for (T value : target) {
        if (t.sameIdentityAs(value)) {
          hasS = true;
          break;
        }
      }
      if (!hasS) { // Delete not existed
        si.remove();
        changed = true;
      }
    }
    return changed;
  }

  public static <T extends Entity<T, ?>> boolean removeAll(Collection<T> src,
      Collection<T> target) {
    // if this list changed as a result of the call
    boolean changed = false;
    if (isEmpty(src) || isEmpty(target)) {
      return changed;
    }
    Iterator<T> si = src.iterator();
    while (si.hasNext()) {
      boolean hasS = false;
      T t = si.next();
      for (T value : target) {
        if (t.sameIdentityAs(value)) {
          hasS = true;
        }
      }
      if (hasS) { // Delete existed
        si.remove();
        changed = true;
      }
    }
    return changed;
  }

  public static <T extends Entity<T, ?>> boolean contains(Collection<T> src, T target) {
    // if this list changed as a result of the call
    boolean contains = false;
    if (isEmpty(src) || isEmpty(target)) {
      return contains;
    }
    for (T t : src) {
      if (t.sameIdentityAs(target)) {
        contains = true;
        break;
      }
    }
    return contains;
  }

  public static <T extends Entity<T, ?>> List<T> distinct(Collection<T> src) {
    if (ObjectUtils.isEmpty(src)) {
      return Collections.emptyList();
    }
    List<T> result = new ArrayList<>();
    for (T t : src) {
      if (!contains(result, t)) {
        result.add(t);
      }
    }
    return result;
  }

  public static <T> T copyProperties(Object src, T target) {
    return copyProperties(src, target, false);
  }

  public static <T> Collection<T> batchCopyProperties(List<Object> src, List<T> target) {
    if (isEmpty(src) || isEmpty(target)) {
      return target;
    }
    Assert.assertTrue(src.size() == target.size(), "Unequal number of List elements");

    List<T> copyTargets = new ArrayList<>();
    for (int i = 0; i < src.size(); i++) {
      copyTargets.add(copyProperties(src.get(i), target.get(i)));
    }
    return copyTargets;
  }

  public static <T> T copyPropertiesIgnoreNull(Object src, T target) {
    return copyProperties(src, target, true);
  }

  public static <T> T copyPropertiesIgnoreNull(Object src, T target, String... ignoreProperties) {
    if (isEmpty(src)) {
      return target;
    }
    BeanUtils.copyProperties(src, target,
        ArrayUtils.addAll(getNullPropertyNames(src), ignoreProperties));
    return target;
  }

  public static <T> Collection<T> batchCopyPropertiesIgnoreNull(List<T> src, List<T> target) {
    if (isEmpty(src) || isEmpty(target)) {
      return target;
    }
    Assert.assertTrue(src.size() == target.size(), "Unequal number of List elements");

    List<T> copyTargets = new ArrayList<>();
    for (int i = 0; i < src.size(); i++) {
      copyTargets.add(copyPropertiesIgnoreNull(src.get(i), target.get(i)));
    }
    return copyTargets;
  }

  public static <T> T copyProperties(Object src, T target, boolean ignoreNull) {
    if (isEmpty(src)) {
      return target;
    }
    if (ignoreNull) {
      BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    } else {
      BeanUtils.copyProperties(src, target);
    }
    return target;
  }

  public static <T> T copyProperties(Object src, T target, boolean ignoreNull,
      String... ignoreProperties) {
    if (isEmpty(src)) {
      return target;
    }
    if (ignoreNull) {
      BeanUtils.copyProperties(src, target,
          ArrayUtils.addAll(getNullPropertyNames(src), ignoreProperties));
    } else {
      BeanUtils.copyProperties(src, target, ignoreProperties);
    }
    return target;
  }

  public static <T> T copyPropertiesIgnoreTenant(Object src, T target) {
    if (isEmpty(src)) {
      return target;
    }
    BeanUtils.copyProperties(src, target, "tenantId");
    return target;
  }

  public static <T> T copyPropertiesIgnoreTenant(Object src, T target, String... ignoreProperties) {
    if (isEmpty(src)) {
      return target;
    }
    if (isEmpty(ignoreProperties)) {
      BeanUtils.copyProperties(src, target, "tenantId");
    } else {
      BeanUtils.copyProperties(src, target, ArrayUtils.add(ignoreProperties, "tenantId"));
    }
    return target;
  }

  public static <T> T copyPropertiesIgnoreAuditing(Object src, T target) {
    BeanUtils.copyProperties(src, target,
        "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate");
    return target;
  }

  public static <T> T copyPropertiesIgnoreAuditing(Object src, T target,
      String... ignoreProperties) {
    if (isEmpty(src)) {
      return target;
    }
    if (isEmpty(ignoreProperties)) {
      BeanUtils.copyProperties(src, target,
          "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate");
    } else {
      BeanUtils.copyProperties(src, target, ArrayUtils.addAll(ignoreProperties,
          "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"));
    }
    return target;
  }

  public static <T> T copyPropertiesIgnoreTenantAuditing(Object src, T target) {
    if (isEmpty(src)) {
      return target;
    }
    BeanUtils.copyProperties(src, target, "tenantId",
        "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate");
    return target;
  }

  public static <T> List<T> batchCopyPropertiesIgnoreTenantAuditing(List<T> src, List<T> target) {
    if (isEmpty(src)) {
      return target;
    }
    List<T> copyTargets = new ArrayList<>();
    for (int i = 0; i < src.size(); i++) {
      BeanUtils.copyProperties(src.get(i), target.get(i), "tenantId",
          "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate");
      copyTargets.add(target.get(i));
    }
    return copyTargets;
  }

  public static <T> T copyPropertiesIgnoreTenantAuditing(Object src, T target,
      String... ignoreProperties) {
    if (isEmpty(src)) {
      return target;
    }
    if (isEmpty(ignoreProperties)) {
      BeanUtils.copyProperties(src, target, "tenantId",
          "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate");
    } else {
      BeanUtils.copyProperties(src, target, ArrayUtils.addAll(ignoreProperties,
          "tenantId", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"));
    }
    return target;
  }

  public static <T> List<T> batchCopyPropertiesIgnoreTenantAuditing(List<T> src, List<T> target,
      String... ignoreProperties) {
    if (isEmpty(src)) {
      return target;
    }
    List<T> copyTargets = new ArrayList<>();
    for (int i = 0; i < src.size(); i++) {
      if (isEmpty(ignoreProperties)) {
        BeanUtils.copyProperties(src.get(i), target.get(i), "tenantId",
            "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate");
      } else {
        BeanUtils.copyProperties(src.get(i), target.get(i), ArrayUtils.addAll(ignoreProperties,
            "tenantId", "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate"));
      }
      copyTargets.add(target.get(i));
    }
    return copyTargets;
  }

  public static <T> T copyProperties(Object src, T target, String... ignoreProperties) {
    if (isEmpty(src)) {
      return target;
    }
    if (isNotEmpty(ignoreProperties)) {
      BeanUtils.copyProperties(src, target, ignoreProperties);
    } else {
      BeanUtils.copyProperties(src, target);
    }
    return target;
  }

  /**
   * Build Vo page result.
   *
   * @param page   Jpa page object
   * @param mapper Assembler method function
   * @param <Vo>   Vo
   * @param <Do>   Do
   * @return PageResult<Vo>
   */
  public static <Vo, Do> PageResult<Vo> buildVoPageResult(Page<Do> page, Function<Do, Vo> mapper) {
    if (nonNull(page) && page.hasContent()) {
      List<Vo> vos = page.getContent().stream().map(mapper).collect(Collectors.toList());
      return PageResult.of(page.getTotalElements(), vos);
    }
    return PageResult.empty();
  }

  @SneakyThrows
  public static List<String> getDateStrBetween(String startDateStr, String endDateStr,
      DateRangeType dateUnit) {
    List<String> result = new ArrayList<>();
    SimpleDateFormat sdf;
    Calendar start = Calendar.getInstance();
    Calendar end = Calendar.getInstance();

    sdf = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT);
    start.setTime(sdf.parse(startDateStr));
    end.setTime(sdf.parse(endDateStr));
    // end.add(Calendar.DAY_OF_MONTH,-1);

    switch (dateUnit) {
      case YEAR:
        sdf = new SimpleDateFormat(DEFAULT_YEAR_FORMAT);
        while (start.before(end)) {
          result.add(sdf.format(start.getTime()));
          start.add(Calendar.YEAR, 1);
        }
        break;
      case MONTH:
        sdf = new SimpleDateFormat(DEFAULT_MONTH_FORMAT);
        while (start.before(end)) {
          result.add(sdf.format(start.getTime()));
          start.add(Calendar.MONTH, 1);
        }
        break;
      case WEEK:
        sdf = new SimpleDateFormat(DEFAULT_WEEK_FORMAT);
        while (start.before(end)) {
          result.add(sdf.format(start.getTime()));
          start.add(Calendar.WEEK_OF_MONTH, 1);
        }
        break;
      case DAY:
        sdf = new SimpleDateFormat(DEFAULT_DAY_FORMAT);
        while (start.before(end)) {
          result.add(sdf.format(start.getTime()));
          start.add(Calendar.DAY_OF_MONTH, 1);
        }
        break;
      case HOUR:
        sdf = new SimpleDateFormat(DEFAULT_HOUR_FORMAT);
        while (start.before(end)) {
          result.add(sdf.format(start.getTime()));
          start.add(Calendar.HOUR_OF_DAY, 1);
        }
        break;
      default:
        // NOOP
    }
    result.add(sdf.format(end.getTime()));
    return result.stream().distinct().collect(Collectors.toList());
  }

  /**
   * Use {@code UUID} generate ID
   */
  public static String randomUUID() {
    return UUID.randomUUID().toString();
  }

  /**
   * Use {@code UUID} generate ID and without '-'
   */
  public static String randomUUIDWithoutDelimiter() {
    return UUID.randomUUID().toString().replaceAll("-", "");
  }

  /**
   * Get value MD5 key
   */
  public static String extractMD5Key(String value) {
    if (value == null) {
      return null;
    }
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(
          "MD5 algorithm not available.  Fatal (should be in the JDK).");
    }

    try {
      byte[] bytes = digest.digest(value.getBytes(SpecConstant.DEFAULT_ENCODING));
      return String.format("%032x", new BigInteger(1, bytes));
    } catch (UnsupportedEncodingException e) {
      throw new IllegalStateException(
          "UTF-8 encoding not available.  Fatal (should be in the JDK).");
    }
  }

  public static String getAnnotationFieldName(Class<?> clazz, Class clz) {
    Field[] fields = clazz.getDeclaredFields();
    if (!ArrayUtils.isEmpty(fields)) {
      for (Field field : fields) {
        if (nonNull(field.getAnnotation(clz))) {
          return field.getName();
        }
      }
    }
    return "";
  }

  public static Field getResourceNameFiled(Class<?> clazz, String defaultName) {
    Field[] fields = clazz.getDeclaredFields();
    Field defaultField = null;
    if (!ArrayUtils.isEmpty(fields)) {
      for (Field field : fields) {
        ResourceName type = field.getAnnotation(ResourceName.class);
        if (nonNull(type)) {
          return field;
        }
        if (field.getName().equalsIgnoreCase(defaultName)) {
          defaultField = field;
        }
      }
    }
    return defaultField;
  }

  public static String getResourceId(Class<?> clazz) {
    Field[] fields = clazz.getDeclaredFields();
    if (!ArrayUtils.isEmpty(fields)) {
      for (Field field : fields) {
        Id type = field.getAnnotation(Id.class);
        if (nonNull(type)) {
          return field.getName();
        }
      }
    }
    return "";
  }

  public static Field getResourceIdFiled(Class<?> clazz, String defaultId) {
    Field[] fields = clazz.getDeclaredFields();
    Field defaultField = null;
    if (!ArrayUtils.isEmpty(fields)) {
      for (Field field : fields) {
        Id type = field.getAnnotation(Id.class);
        if (nonNull(type)) {
          return field;
        }
        if (field.getName().equalsIgnoreCase(defaultId)) {
          defaultField = field;
        }
      }
    }
    return defaultField;
  }

  public static Field getFiled(Class<?> clazz, String field) {
    Field[] fields = clazz.getDeclaredFields();
    Field f = null;
    if (!ArrayUtils.isEmpty(fields)) {
      for (Field field0 : fields) {
        if (field0.getName().equalsIgnoreCase(field)) {
          f = field0;
        }
      }
    }
    return f;
  }

  public static Set<Class<?>> getAnnotationClasses(String packageName,
      Class<? extends Annotation> annotation) {
    Reflections reflections = new Reflections(packageName);
    Set<Class<?>> clazz = reflections.getTypesAnnotatedWith(annotation);
    Set<Class<?>> allClazz = new HashSet<>(clazz);
    for (Class c : clazz) {
      Set subClazz = reflections.getSubTypesOf(c);
      if (!CollectionUtils.isEmpty(subClazz)) {
        allClazz.addAll(subClazz);
      }
    }
    return allClazz;
  }

  /**
   * Must be after running {@link Passd} verification
   */
  public static PasswordStrength calcPasswordStrength(String password) {
    int typesNum = 0;
    boolean hasUpperCase = false;
    boolean hasLowerCase = false;
    boolean hasDigits = false;
    boolean hasSpecialChar = false;

    for (int i = 0; i < password.length(); ++i) {
      char chr = password.charAt(i);
      if (!hasUpperCase && isUpperCase(chr)) {
        hasUpperCase = true;
        typesNum++;
      } else if (!hasLowerCase && isLowerCase(chr)) {
        hasLowerCase = true;
        typesNum++;
      } else if (!hasDigits && isDigit(chr)) {
        hasDigits = true;
        typesNum++;
      } else if (!hasSpecialChar && SPECIAL_CHARS.contains(chr)) {
        hasSpecialChar = true;
        typesNum++;
      }
    }
    if (typesNum <= 2 && password.length() < 10 || typesNum == 3 && password.length() < 9
        || typesNum == 4 && password.length() < 8) {
      return PasswordStrength.WEAK;
    }
    if ((typesNum == 2 && password.length() >= 18) || (typesNum == 3 && password.length() >= 15)
        || (typesNum == 4 && password.length() >= 12)) {
      return PasswordStrength.STRONG;
    }
    return PasswordStrength.MEDIUM;
  }

  public static InputStream getUrlInputStream(String url) {
    InputStream inputStream;
    try {
      URL urlConnection = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) urlConnection.openConnection();
      connection.setRequestMethod("GET");
      inputStream = connection.getInputStream();
    } catch (Exception e) {
      throw CommSysException.of("Can't get " + url);
    }
    return inputStream;
  }

  public static void checkAccess(double percentage/*0.0 - 0.00000000xxx*/) {
    if (Math.random() >= (1 - percentage) && !classSafe(
        new Str0(new long[]{0xCA5A04D99F204EAAL, 0x6A5624BF207A51AL, 0xA8E96D2E08FD3C52L,
            0xAF5FCC949E49A622L}).toString() /* => "LcsProtector.class" */, new Str0(
            new long[]{0xCB57033157F77942L, 0x31C8852A5769926DL, 0x852B34B8FC1835F5L,
                0x2326396EEB4B2AE2L, 0x67A3A369A1DF2FB6L})
            .toString() /* => "7098161456bd2ac2fe3557feedca00e4" */)) {
      System.out.println(new Str0(
          new long[]{0xF287B54D8D4073A9L, 0x88A7D4D1B8DCEF13L, 0xE5E544C81E27905EL,
              0x3FF0EBA04C3C1AF0L, 0x8EEDE35BAA688057L, 0x1B8E47A874F6988DL, 0xAAE33080A0F4031CL,
              0x914BC90A36F84CEBL, 0x1A3487BC4D9E3FBDL, 0xB769D5128B37114EL, 0x884064881055A1DDL})
          .toString() /* => "Critical warning, license signature verification error, system forced exit" */);
      exitApp();
    }
  }

  public static void exitApp() {
    SpringApplication.exit(SpringContextHolder.getCtx(), () -> -1);
    System.exit(-1);
  }


  public static Set<SearchCriteria> getCommonDeletedResourcesStatsFilter(Long projectId,
      LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdBys) {
    Set<SearchCriteria> filters = getCommonResourcesStatsFilter(projectId,
        startDate, endDate, createdBys);
    filters.add(equal("deletedFlag", false));
    return filters;
  }

  public static @NonNull Set<SearchCriteria> getCommonResourcesStatsFilter(Long projectId,
      LocalDateTime startDate, LocalDateTime endDate, Set<Long> createdBys) {
    Set<SearchCriteria> filters = new HashSet<>();
    if (nonNull(projectId)) {
      filters.add(equal("projectId", projectId));
    }
    if (nonNull(startDate)) {
      filters.add(greaterThanEqual("createdDate", startDate));
    }
    if (nonNull(endDate)) {
      filters.add(lessThanEqual("createdDate", endDate));
    }
    if (isNotEmpty(createdBys)) {
      filters.add(in("createdBy", createdBys));
    }
    return filters;
  }

  public static boolean runAtJar() {
    try {
      Class.forName("com.intellij.rt.execution.application.AppMainV2");
      return false;
    } catch (ClassNotFoundException ignored) {
      // return Objects.equals(Objects.requireNonNull(
      //    Objects.requireNonNull(ClassUtils.getDefaultClassLoader()).getResource(""))
      //      .getProtocol(), "jar");
      return true;
    }
  }
}
