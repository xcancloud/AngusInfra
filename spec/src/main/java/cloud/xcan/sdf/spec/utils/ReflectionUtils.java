package cloud.xcan.sdf.spec.utils;

import static cloud.xcan.sdf.spec.experimental.Assert.assertNotNull;
import static cloud.xcan.sdf.spec.experimental.Assert.assertTrue;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.spec.annotations.Nullable;
import cloud.xcan.sdf.spec.utils.map.ConcurrentReferenceHashMap;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <OK_CODE>ReflectionUtils</OK_CODE> - Reflection utilities holder.
 */
public final class ReflectionUtils {

  private ReflectionUtils() { /* no instance */ }

  private static final String GETTER_PREFIX = "get";

  private static final String SETTER_PREFIX = "set";

  /**
   * Pre-built MethodFilter that matches all non-bridge non-synthetic methods which are not declared
   * on {@code java.lang.Object}.
   */
  public static final MethodFilter USER_DECLARED_METHODS =
      (method -> !method.isBridge() && !method.isSynthetic());

  /**
   * Pre-built FieldFilter that matches all non-static, non-final fields.
   */
  public static final FieldFilter COPYABLE_FIELDS =
      (field -> !(Modifier.isStatic(field.getModifiers()) || Modifier
          .isFinal(field.getModifiers())));

  /**
   * Naming prefix for CGLIB-renamed methods.
   *
   * @see #isCglibRenamedMethod
   */
  private static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

  private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

  private static final Method[] EMPTY_METHOD_ARRAY = new Method[0];

  private static final Field[] EMPTY_FIELD_ARRAY = new Field[0];

  private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];


  /**
   * Cache for {@link Class#getDeclaredMethods()} plus equivalent default methods from Java 8 based
   * interfaces, allowing for fast iteration.
   */
  private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentReferenceHashMap<>(
      256);

  /**
   * Cache for {@link Class#getDeclaredFields()}, allowing for fast iteration.
   */
  private static final Map<Class<?>, Field[]> declaredFieldsCache = new ConcurrentReferenceHashMap<>(
      256);

  /**
   * Is the class abstract? (This includes interfaces.)
   *
   * Utility method used by few other places in the code. Tests if the class has the abstract
   * modifier and is not an array class. For some reason, array classes have the abstract modifier
   * set in HotSpot JVM, and we don't want to treat array classes as abstract.
   *
   * @param clazz the inspected class
   * @return true if the class is abstract and is not an array type.
   */
  public static boolean isAbstractClass(final Class<?> clazz) {
    return Modifier.isAbstract(clazz.getModifiers()) && !clazz.isArray();
  }

  /**
   * Instantiate a new {@link Object} of the provided type.
   *
   * @param className The fully-qualified Java class name of the {@link Object} to instantiate
   * @param <T>       class type
   * @return new Java {@link Object} of the provided type
   */
  public static <T> T newInstance(String className) {
    Class clazz = getClass(className);
    return newInstance(clazz);
  }

  /**
   * Instantiate a new {@link Object} of the provided type.
   *
   * @param clazz The Java {@link Class} of the {@link Object} to instantiate
   * @param <T>   class type
   * @return new Java {@link Object} of the provided type
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class clazz) {
    try {
      return (T) clazz.newInstance();
    } catch (InstantiationException e) {
      throw handleException(e);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    }
  }

  /**
   * Instantiate a new {@link Object} of the provided type.
   *
   * @param clazz     The Java {@link Class} of the {@link Object} to instantiate
   * @param args      The arguments that need to be passed to the constructor
   * @param argsTypes The argument types that need to be passed to the constructor
   * @param <T>       class type
   * @return new Java {@link Object} of the provided type
   */
  @SuppressWarnings("unchecked")
  public static <T> T newInstance(Class clazz, Object[] args, Class[] argsTypes) {
    try {
      Constructor<T> constructor = clazz.getDeclaredConstructor(argsTypes);
      constructor.setAccessible(true);
      return constructor.newInstance(args);
    } catch (InstantiationException e) {
      throw handleException(e);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    } catch (NoSuchMethodException e) {
      throw handleException(e);
    } catch (InvocationTargetException e) {
      throw handleException(e);
    }
  }

  public static Set<String> getGenericSuperclassType(Class clz) {
    Set<String> ret = new HashSet<>();
    Type genericSuperclass = clz.getGenericSuperclass();
    if (nonNull(genericSuperclass) && genericSuperclass instanceof ParameterizedType) {
      Type[] actualTypeArguments = ((ParameterizedType) genericSuperclass).getActualTypeArguments();
      if (isNotEmpty(actualTypeArguments)) {
        for (Type type : actualTypeArguments) {
          ret.add(((Class) type).getName());
        }
      }
    }
    return ret;
  }

  public static Set<String> getGenericInterfacesType(Class clz) {
    Set<String> ret = new HashSet<>();
    Type[] genericInterfaces = clz.getGenericInterfaces();
    if (isNotEmpty(genericInterfaces)) {
      for (Type type : genericInterfaces) {
        if (type instanceof ParameterizedType) {
          Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
          if (isNotEmpty(actualTypeArguments)) {
            for (Type type_ : actualTypeArguments) {
              ret.add(((Class) type_).getName());
            }
          }
        }
      }
    }
    return ret;
  }

  public static Set<String> getAllGenericType(Class clz) {
    Set<String> ret = new HashSet<>();
    ret.addAll(getGenericSuperclassType(clz));
    ret.addAll(getGenericInterfacesType(clz));
    return ret;
  }

  /**
   * Get the {@link Field} with the given name belonging to the provided Java {@link Class}.
   *
   * @param targetClass the provided Java {@link Class} the field belongs to
   * @param fieldName   the {@link Field} name
   * @return the {@link Field} matching the given name
   */
  public static Field getField(Class targetClass, String fieldName) {
    Field field = null;
    try {
      field = targetClass.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      try {
        field = targetClass.getField(fieldName);
      } catch (NoSuchFieldException ignore) {
      }
      if (!targetClass.getSuperclass().equals(Object.class)) {
        return getField(targetClass.getSuperclass(), fieldName);
      } else {
        throw handleException(e);
      }
    } finally {
      if (field != null) {
        field.setAccessible(true);
      }
    }
    return field;
  }

  /**
   * Get the {@link Field} with the given name belonging to the provided Java {@link Class} or
   * {@code null} if no {@link Field} was found.
   *
   * @param targetClass the provided Java {@link Class} the field belongs to
   * @param fieldName   the {@link Field} name
   * @return the {@link Field} matching the given name or {@code null}
   */
  public static Field getFieldOrNull(Class targetClass, String fieldName) {
    try {
      return getField(targetClass, fieldName);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Get the value of the field matching the given name and belonging to target {@link Object}.
   *
   * @param target    target {@link Object} whose field we are retrieving the value from
   * @param fieldName field name
   * @param <T>       field type
   * @return field value
   */
  public static <T> T getFieldValue(Object target, String fieldName) {
    try {
      Field field = getField(target.getClass(), fieldName);
      @SuppressWarnings("unchecked")
      T returnValue = (T) field.get(target);
      return returnValue;
    } catch (IllegalAccessException e) {
      throw handleException(e);
    }
  }

  /**
   * Get the value of the field matching the given name and belonging to target {@link Object} or
   * {@code null} if no {@link Field} was found..
   *
   * @param target    target {@link Object} whose field we are retrieving the value from
   * @param fieldName field name
   * @param <T>       field type
   * @return field value matching the given name or {@code null}
   */
  public static <T> T getFieldValueOrNull(Object target, String fieldName) {
    try {
      Field field = getField(target.getClass(), fieldName);
      @SuppressWarnings("unchecked")
      T returnValue = (T) field.get(target);
      return returnValue;
    } catch (IllegalAccessException e) {
      return null;
    }
  }

  /**
   * Set the value of the field matching the given name and belonging to target {@link Object}.
   *
   * @param target    target object
   * @param fieldName field name
   * @param value     field value
   */
  public static void setFieldValue(Object target, String fieldName, Object value) {
    try {
      Field field = getField(target.getClass(), fieldName);
      field.set(target, value);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    }
  }

  /**
   * Get the {@link Method} with the given signature (name and parameter types) belonging to the
   * provided Java {@link Object}.
   *
   * @param target         target {@link Object}
   * @param methodName     method name
   * @param parameterTypes method parameter types
   * @return return {@link Method} matching the provided signature
   */
  public static Method getMethod(Object target, String methodName, Class... parameterTypes) {
    return getMethod(target.getClass(), methodName, parameterTypes);
  }

  /**
   * Get the {@link Method} with the given signature (name and parameter types) belonging to the
   * provided Java {@link Object} or {@code null} if no {@link Method} was found.
   *
   * @param target         target {@link Object}
   * @param methodName     method name
   * @param parameterTypes method parameter types
   * @return return {@link Method} matching the provided signature or {@code null}
   */
  public static Method getMethodOrNull(Object target, String methodName, Class... parameterTypes) {
    try {
      return getMethod(target.getClass(), methodName, parameterTypes);
    } catch (RuntimeException e) {
      return null;
    }
  }

  /**
   * Get the {@link Method} with the given signature (name and parameter types) belonging to the
   * provided Java {@link Class}.
   *
   * @param targetClass    target {@link Class}
   * @param methodName     method name
   * @param parameterTypes method parameter types
   * @return the {@link Method} matching the provided signature
   */
  @SuppressWarnings("unchecked")
  public static Method getMethod(Class targetClass, String methodName, Class... parameterTypes) {
    try {
      return targetClass.getDeclaredMethod(methodName, parameterTypes);
    } catch (NoSuchMethodException e) {
      try {
        return targetClass.getMethod(methodName, parameterTypes);
      } catch (NoSuchMethodException ignore) {
      }

      if (!targetClass.getSuperclass().equals(Object.class)) {
        return getMethod(targetClass.getSuperclass(), methodName, parameterTypes);
      } else {
        throw handleException(e);
      }
    }
  }

  /**
   * Get the {@link Method} with the given signature (name and parameter types) belonging to the
   * provided Java {@link Object} or {@code null} if no {@link Method} was found.
   *
   * @param targetClass    target {@link Class}
   * @param methodName     method name
   * @param parameterTypes method parameter types
   * @return return {@link Method} matching the provided signature or {@code null}
   */
  public static Method getMethodOrNull(Class targetClass, String methodName,
      Class... parameterTypes) {
    try {
      return getMethod(targetClass, methodName, parameterTypes);
    } catch (RuntimeException e) {
      return null;
    }
  }

  /**
   * Get the {@link Method} with the given signature (name and parameter types) belonging to the
   * provided Java {@link Class}, excluding inherited ones, or {@code null} if no {@link Method} was
   * found.
   *
   * @param targetClass    target {@link Class}
   * @param methodName     method name
   * @param parameterTypes method parameter types
   * @return return {@link Method} matching the provided signature or {@code null}
   */
  public static Method getDeclaredMethodOrNull(Class targetClass, String methodName,
      Class... parameterTypes) {
    try {
      return targetClass.getDeclaredMethod(methodName, parameterTypes);
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * Check if the provided Java {@link Class} contains a method matching the given signature (name
   * and parameter types).
   *
   * @param targetClass    target {@link Class}
   * @param methodName     method name
   * @param parameterTypes method parameter types
   * @return the provided Java {@link Class} contains a method with the given signature
   */
  public static boolean hasMethod(Class<?> targetClass, String methodName,
      Class... parameterTypes) {
    try {
      targetClass.getMethod(methodName, parameterTypes);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  /**
   * Get the property setter {@link Method} with the given signature (name and parameter types)
   * belonging to the provided Java {@link Object}.
   *
   * @param target        target {@link Object}
   * @param propertyName  property name
   * @param parameterType setter property type
   * @return the setter {@link Method} matching the provided signature
   */
  public static Method getSetter(Object target, String propertyName, Class<?> parameterType) {
    String setterMethodName = SETTER_PREFIX + propertyName.substring(0, 1).toUpperCase()
        + propertyName.substring(1);
    Method setter = getMethod(target, setterMethodName, parameterType);
    setter.setAccessible(true);
    return setter;
  }

  /**
   * Get the property getter {@link Method} with the given name belonging to the provided Java
   * {@link Object}.
   *
   * @param target       target {@link Object}
   * @param propertyName property name
   * @return the getter {@link Method} matching the provided name
   */
  public static Method getGetter(Object target, String propertyName) {
    String getterMethodName = GETTER_PREFIX + propertyName.substring(0, 1).toUpperCase()
        + propertyName.substring(1);
    Method getter = getMethod(target, getterMethodName);
    getter.setAccessible(true);
    return getter;
  }

  /**
   * Invoke the provided {@link Method} on the given Java {@link Object}.
   *
   * @param target     target {@link Object} whose method we are invoking
   * @param method     method to invoke
   * @param parameters parameters passed to the method call
   * @param <T>        return value object type
   * @return the value return by the {@link Method} invocation
   */
  public static <T> T invokeMethod(Object target, Method method, Object... parameters) {
    try {
      method.setAccessible(true);
      @SuppressWarnings("unchecked")
      T returnValue = (T) method.invoke(target, parameters);
      return returnValue;
    } catch (InvocationTargetException e) {
      throw handleException(e);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    }
  }

  /**
   * Invoke the method with the provided signature (name and parameter types) on the given Java
   * {@link Object}.
   *
   * @param target     target {@link Object} whose method we are invoking
   * @param methodName method name to invoke
   * @param parameters parameters passed to the method call
   * @param <T>        return value object type
   * @return the value return by the method invocation
   */
  public static <T> T invokeMethod(Object target, String methodName, Object... parameters) {
    try {
      Class[] parameterClasses = new Class[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        parameterClasses[i] = parameters[i].getClass();
      }
      Method method = getMethod(target, methodName, parameterClasses);
      method.setAccessible(true);
      @SuppressWarnings("unchecked")
      T returnValue = (T) method.invoke(target, parameters);
      return returnValue;
    } catch (InvocationTargetException e) {
      throw handleException(e);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    }
  }

  /**
   * Invoke the property getter with the provided name on the given Java {@link Object}.
   *
   * @param target       target {@link Object} whose property getter we are invoking
   * @param propertyName property name whose getter we are invoking
   * @param <T>          return value object type
   * @return the value return by the getter invocation
   */
  public static <T> T invokeGetter(Object target, String propertyName) {
    Method setter = getGetter(target, propertyName);
    try {
      return (T) setter.invoke(target);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    } catch (InvocationTargetException e) {
      throw handleException(e);
    }
  }

  /**
   * Invoke the property setter with the provided signature (name and parameter types) on the given
   * Java {@link Object}.
   *
   * @param target       target {@link Object} whose property setter we are invoking
   * @param propertyName property name whose setter we are invoking
   * @param parameter    parameter passed to the setter call
   */
  public static void invokeSetter(Object target, String propertyName, Object parameter) {
    Method setter = getSetter(target, propertyName, parameter.getClass());
    try {
      setter.invoke(target, parameter);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    } catch (InvocationTargetException e) {
      throw handleException(e);
    }
  }

  /**
   * Invoke the {@link boolean} property setter with the provided name on the given Java
   * {@link Object}.
   *
   * @param target       target {@link Object} whose property setter we are invoking
   * @param propertyName property name whose setter we are invoking
   * @param parameter    {@link boolean} parameter passed to the setter call
   */
  public static void invokeSetter(Object target, String propertyName, boolean parameter) {
    Method setter = getSetter(target, propertyName, boolean.class);
    try {
      setter.invoke(target, parameter);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    } catch (InvocationTargetException e) {
      throw handleException(e);
    }
  }

  /**
   * Invoke the {@link int} property setter with the provided name on the given Java
   * {@link Object}.
   *
   * @param target       target {@link Object} whose property setter we are invoking
   * @param propertyName property name whose setter we are invoking
   * @param parameter    {@link int} parameter passed to the setter call
   */
  public static void invokeSetter(Object target, String propertyName, int parameter) {
    Method setter = getSetter(target, propertyName, int.class);
    try {
      setter.invoke(target, parameter);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    } catch (InvocationTargetException e) {
      throw handleException(e);
    }
  }

  /**
   * Invoke the {@code static} {@link Method} with the provided parameters.
   *
   * @param method     target {@code static} {@link Method} to invoke
   * @param parameters parameters passed to the method call
   * @param <T>        return value object type
   * @return the value return by the method invocation
   */
  public static <T> T invokeStaticMethod(Method method, Object... parameters) {
    try {
      method.setAccessible(true);
      @SuppressWarnings("unchecked")
      T returnValue = (T) method.invoke(null, parameters);
      return returnValue;
    } catch (InvocationTargetException e) {
      throw handleException(e);
    } catch (IllegalAccessException e) {
      throw handleException(e);
    }
  }

  /**
   * Get the Java {@link Class} with the given fully-qualified name.
   *
   * @param className the Java {@link Class} name to be retrieved
   * @param <T>       {@link Class} type
   * @return the Java {@link Class} object
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getClass(String className) {
    try {
      return (Class<T>) Class.forName(className, false,
          Thread.currentThread().getContextClassLoader());
    } catch (ClassNotFoundException e) {
      throw handleException(e);
    }
  }

  /**
   * Get the Java {@link Class} with the given fully-qualified name or or {@code null} if no
   * {@link Class} was found matching the provided name.
   *
   * @param className the Java {@link Class} name to be retrieved
   * @param <T>       {@link Class} type
   * @return the Java {@link Class} object or {@code null}
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<T> getClassOrNull(String className) {
    try {
      return (Class<T>) getClass(className);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Get the Java Wrapper {@link Class} associated to the given primitive type.
   *
   * @param clazz primitive class
   * @return the Java Wrapper {@link Class}
   */
  public static Class<?> getWrapperClass(Class<?> clazz) {
    if (!clazz.isPrimitive()) {
      return clazz;
    }
    if (clazz == Integer.TYPE) {
      return Integer.class;
    }
    if (clazz == Long.TYPE) {
      return Long.class;
    }
    if (clazz == Boolean.TYPE) {
      return Boolean.class;
    }
    if (clazz == Byte.TYPE) {
      return Byte.class;
    }
    if (clazz == Character.TYPE) {
      return Character.class;
    }
    if (clazz == Float.TYPE) {
      return Float.class;
    }
    if (clazz == Double.TYPE) {
      return Double.class;
    }
    if (clazz == Short.TYPE) {
      return Short.class;
    }
    if (clazz == Void.TYPE) {
      return Void.class;
    }
    return clazz;
  }

  /**
   * Get the first super class matching the provided package name.
   *
   * @param clazz       Java class
   * @param packageName package name
   * @param <T>         class generic type
   * @return the first super class matching the provided package name or {@code null}.
   */
  public static <T> Class<T> getFirstSuperClassFromPackage(Class clazz, String packageName) {
    if (clazz.getPackage().getName().equals(packageName)) {
      return clazz;
    } else {
      Class superClass = clazz.getSuperclass();
      return (superClass == null || superClass.equals(Object.class)) ?
          null : (Class<T>) getFirstSuperClassFromPackage(superClass, packageName);
    }
  }

  /**
   * Get the generic types of a given Class.
   *
   * @param parameterizedType parameterized Type
   * @return generic types for the given Class.
   */
  public static Set<Class> getGenericTypes(ParameterizedType parameterizedType) {
    Set<Class> genericTypes = new LinkedHashSet<>();
    for (Type genericType : parameterizedType.getActualTypeArguments()) {
      if (genericType instanceof Class) {
        genericTypes.add((Class) genericType);
      }
    }
    return genericTypes;
  }

  /**
   * Get the {@link Member} with the given name belonging to the provided Java {@link Class} or
   * {@code null} if no {@link Member} was found.
   *
   * @param targetClass the provided Java {@link Class} the field or method belongs to
   * @param memberName  the {@link Field} or {@link Method} name
   * @return the {@link Field} or {@link Method} matching the given name or {@code null}
   */
  public static Member getMemberOrNull(Class targetClass, String memberName) {
    Field field = getFieldOrNull(targetClass, memberName);
    return (field != null) ? field : getMethodOrNull(targetClass, memberName);
  }

  /**
   * Get the generic {@link Type} of the {@link Member} with the given name belonging to the
   * provided Java {@link Class} or {@code null} if no {@link Member} was found.
   *
   * @param targetClass the provided Java {@link Class} the field or method belongs to
   * @param memberName  the {@link Field} or {@link Method} name
   * @return the generic {@link Type} of the {@link Field} or {@link Method} matching the given name
   * or {@code null}
   */
  public static Type getMemberGenericTypeOrNull(Class targetClass, String memberName) {
    Field field = getFieldOrNull(targetClass, memberName);
    return (field != null) ? field.getGenericType()
        : getMethodOrNull(targetClass, memberName).getGenericReturnType();
  }

  /**
   * Handle the {@link NoSuchFieldException} by rethrowing it as an
   * {@link IllegalArgumentException}.
   *
   * @param e the original {@link NoSuchFieldException}
   * @return the {@link IllegalArgumentException} wrapping exception
   */
  private static IllegalArgumentException handleException(NoSuchFieldException e) {
    return new IllegalArgumentException(e);
  }

  /**
   * Handle the {@link NoSuchMethodException} by rethrowing it as an
   * {@link IllegalArgumentException}.
   *
   * @param e the original {@link NoSuchMethodException}
   * @return the {@link IllegalArgumentException} wrapping exception
   */
  private static IllegalArgumentException handleException(NoSuchMethodException e) {
    return new IllegalArgumentException(e);
  }

  /**
   * Handle the {@link IllegalAccessException} by rethrowing it as an
   * {@link IllegalArgumentException}.
   *
   * @param e the original {@link IllegalAccessException}
   * @return the {@link IllegalArgumentException} wrapping exception
   */
  private static IllegalArgumentException handleException(IllegalAccessException e) {
    return new IllegalArgumentException(e);
  }

  /**
   * Handle the {@link InvocationTargetException} by rethrowing it as an
   * {@link IllegalArgumentException}.
   *
   * @param e the original {@link InvocationTargetException}
   * @return the {@link IllegalArgumentException} wrapping exception
   */
  private static IllegalArgumentException handleException(InvocationTargetException e) {
    return new IllegalArgumentException(e);
  }

  /**
   * Handle the {@link ClassNotFoundException} by rethrowing it as an
   * {@link IllegalArgumentException}.
   *
   * @param e the original {@link ClassNotFoundException}
   * @return the {@link IllegalArgumentException} wrapping exception
   */
  private static IllegalArgumentException handleException(ClassNotFoundException e) {
    return new IllegalArgumentException(e);
  }

  /**
   * Handle the {@link InstantiationException} by rethrowing it as an
   * {@link IllegalArgumentException}.
   *
   * @param e the original {@link InstantiationException}
   * @return the {@link IllegalArgumentException} wrapping exception
   */
  private static IllegalArgumentException handleException(InstantiationException e) {
    return new IllegalArgumentException(e);
  }

  // Exception handling

  /**
   * Handle the given reflection exception.
   * <p>Should only be called if no checked exception is expected to be thrown
   * by a target method, or if an error occurs while accessing a method or field.
   * <p>Throws the underlying RuntimeException or Error in case of an
   * InvocationTargetException with such a root cause. Throws an IllegalStateException with an
   * appropriate message or UndeclaredThrowableException otherwise.
   *
   * @param ex the reflection exception to handle
   */
  public static void handleReflectionException(Exception ex) {
    if (ex instanceof NoSuchMethodException) {
      throw new IllegalStateException("Method not found: " + ex.getMessage());
    }
    if (ex instanceof IllegalAccessException) {
      throw new IllegalStateException("Could not access method or field: " + ex.getMessage());
    }
    if (ex instanceof InvocationTargetException) {
      handleInvocationTargetException((InvocationTargetException) ex);
    }
    if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
    }
    throw new UndeclaredThrowableException(ex);
  }

  /**
   * Handle the given invocation target exception. Should only be called if no checked exception is
   * expected to be thrown by the target method.
   * <p>Throws the underlying RuntimeException or Error in case of such a root
   * cause. Throws an UndeclaredThrowableException otherwise.
   *
   * @param ex the invocation target exception to handle
   */
  public static void handleInvocationTargetException(InvocationTargetException ex) {
    rethrowRuntimeException(ex.getTargetException());
  }

  /**
   * Rethrow the given {@link Throwable exception}, which is presumably the
   * <em>target exception</em> of an {@link InvocationTargetException}.
   * Should only be called if no checked exception is expected to be thrown by the target method.
   * <p>Rethrows the underlying exception cast to a {@link RuntimeException} or
   * {@link Error} if appropriate; otherwise, throws an {@link UndeclaredThrowableException}.
   *
   * @param ex the exception to rethrow
   * @throws RuntimeException the rethrown exception
   */
  public static void rethrowRuntimeException(Throwable ex) {
    if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
    }
    if (ex instanceof Error) {
      throw (Error) ex;
    }
    throw new UndeclaredThrowableException(ex);
  }

  /**
   * Rethrow the given {@link Throwable exception}, which is presumably the
   * <em>target exception</em> of an {@link InvocationTargetException}.
   * Should only be called if no checked exception is expected to be thrown by the target method.
   * <p>Rethrows the underlying exception cast to an {@link Exception} or
   * {@link Error} if appropriate; otherwise, throws an {@link UndeclaredThrowableException}.
   *
   * @param ex the exception to rethrow
   * @throws Exception the rethrown exception (in case of a checked exception)
   */
  public static void rethrowException(Throwable ex) throws Exception {
    if (ex instanceof Exception) {
      throw (Exception) ex;
    }
    if (ex instanceof Error) {
      throw (Error) ex;
    }
    throw new UndeclaredThrowableException(ex);
  }

  // Constructor handling

  /**
   * Obtain an accessible constructor for the given class and parameters.
   *
   * @param clazz          the clazz to check
   * @param parameterTypes the parameter types of the desired constructor
   * @return the constructor reference
   * @throws NoSuchMethodException if no such constructor exists
   */
  public static <T> Constructor<T> accessibleConstructor(Class<T> clazz, Class<?>... parameterTypes)
      throws NoSuchMethodException {

    Constructor<T> ctor = clazz.getDeclaredConstructor(parameterTypes);
    makeAccessible(ctor);
    return ctor;
  }

  /**
   * Make the given constructor accessible, explicitly setting it accessible if necessary. The
   * {@code setAccessible(true)} method is only called when actually necessary, to avoid unnecessary
   * conflicts with a JVM SecurityManager (if active).
   *
   * @param ctor the constructor to make accessible
   * @see java.lang.reflect.Constructor#setAccessible
   */
  @SuppressWarnings("deprecation")  // on JDK 9
  public static void makeAccessible(Constructor<?> ctor) {
    if ((!Modifier.isPublic(ctor.getModifiers()) ||
        !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
      ctor.setAccessible(true);
    }
  }

  // Method handling

  /**
   * Attempt to find a {@link Method} on the supplied class with the supplied name and no
   * parameters. Searches all superclasses up to {@code Object}.
   * <p>Returns {@code null} if no {@link Method} can be found.
   *
   * @param clazz the class to introspect
   * @param name  the name of the method
   * @return the Method object, or {@code null} if none found
   */
  @Nullable
  public static Method findMethod(Class<?> clazz, String name) {
    return findMethod(clazz, name, EMPTY_CLASS_ARRAY);
  }

  /**
   * Attempt to find a {@link Method} on the supplied class with the supplied name and parameter
   * types. Searches all superclasses up to {@code Object}.
   * <p>Returns {@code null} if no {@link Method} can be found.
   *
   * @param clazz      the class to introspect
   * @param name       the name of the method
   * @param paramTypes the parameter types of the method (may be {@code null} to indicate any
   *                   signature)
   * @return the Method object, or {@code null} if none found
   */
  @Nullable
  public static Method findMethod(Class<?> clazz, String name, @Nullable Class<?>... paramTypes) {
    assertNotNull(clazz, "Class must not be null");
    assertNotNull(name, "Method name must not be null");
    Class<?> searchType = clazz;
    while (searchType != null) {
      Method[] methods = (searchType.isInterface() ? searchType.getMethods() :
          getDeclaredMethods(searchType, false));
      for (Method method : methods) {
        if (name.equals(method.getName()) && (paramTypes == null || hasSameParams(method,
            paramTypes))) {
          return method;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  private static boolean hasSameParams(Method method, Class<?>[] paramTypes) {
    return (paramTypes.length == method.getParameterCount() &&
        Arrays.equals(paramTypes, method.getParameterTypes()));
  }

  /**
   * Invoke the specified {@link Method} against the supplied target object with no arguments. The
   * target object can be {@code null} when invoking a static {@link Method}.
   * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException}.
   *
   * @param method the method to invoke
   * @param target the target object to invoke the method on
   * @return the invocation result, if any
   * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
   */
  @Nullable
  public static Object invokeMethod(Method method, @Nullable Object target) {
    return invokeMethod(method, target, EMPTY_OBJECT_ARRAY);
  }

  /**
   * Invoke the specified {@link Method} against the supplied target object with the supplied
   * arguments. The target object can be {@code null} when invoking a static {@link Method}.
   * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException}.
   *
   * @param method the method to invoke
   * @param target the target object to invoke the method on
   * @param args   the invocation arguments (may be {@code null})
   * @return the invocation result, if any
   */
  @Nullable
  public static Object invokeMethod(Method method, @Nullable Object target,
      @Nullable Object... args) {
    try {
      return method.invoke(target, args);
    } catch (Exception ex) {
      handleReflectionException(ex);
    }
    throw new IllegalStateException("Should never get here");
  }

  /**
   * Determine whether the given method explicitly declares the given exception or one of its
   * superclasses, which means that an exception of that type can be propagated as-is within a
   * reflective invocation.
   *
   * @param method        the declaring method
   * @param exceptionType the exception to throw
   * @return {@code true} if the exception can be thrown as-is; {@code false} if it needs to be
   * wrapped
   */
  public static boolean declaresException(Method method, Class<?> exceptionType) {
    assertNotNull(method, "Method must not be null");
    Class<?>[] declaredExceptions = method.getExceptionTypes();
    for (Class<?> declaredException : declaredExceptions) {
      if (declaredException.isAssignableFrom(exceptionType)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Perform the given callback operation on all matching methods of the given class, as locally
   * declared or equivalent thereof (such as default methods on Java 8 based interfaces that the
   * given class implements).
   *
   * @param clazz the class to introspect
   * @param mc    the callback to invoke for each method
   * @throws IllegalStateException if introspection fails
   * @see #doWithMethods
   */
  public static void doWithLocalMethods(Class<?> clazz,
      MethodCallback mc) {
    Method[] methods = getDeclaredMethods(clazz, false);
    for (Method method : methods) {
      try {
        mc.doWith(method);
      } catch (IllegalAccessException ex) {
        throw new IllegalStateException(
            "Not allowed to access method '" + method.getName() + "': " + ex);
      }
    }
  }

  /**
   * Perform the given callback operation on all matching methods of the given class and
   * superclasses.
   * <p>The same named method occurring on subclass and superclass will appear
   * twice, unless excluded by a {@link MethodFilter}.
   *
   * @param clazz the class to introspect
   * @param mc    the callback to invoke for each method
   * @throws IllegalStateException if introspection fails
   * @see #doWithMethods(Class, MethodCallback, MethodFilter)
   */
  public static void doWithMethods(Class<?> clazz,
      MethodCallback mc) {
    doWithMethods(clazz, mc, null);
  }

  /**
   * Perform the given callback operation on all matching methods of the given class and
   * superclasses (or given interface and super-interfaces).
   * <p>The same named method occurring on subclass and superclass will appear
   * twice, unless excluded by the specified {@link MethodFilter}.
   *
   * @param clazz the class to introspect
   * @param mc    the callback to invoke for each method
   * @param mf    the filter that determines the methods to apply the callback to
   * @throws IllegalStateException if introspection fails
   */
  public static void doWithMethods(Class<?> clazz,
      MethodCallback mc,
      @Nullable MethodFilter mf) {
    // Keep backing up the inheritance hierarchy.
    Method[] methods = getDeclaredMethods(clazz, false);
    for (Method method : methods) {
      if (mf != null && !mf.matches(method)) {
        continue;
      }
      try {
        mc.doWith(method);
      } catch (IllegalAccessException ex) {
        throw new IllegalStateException(
            "Not allowed to access method '" + method.getName() + "': " + ex);
      }
    }
    if (clazz.getSuperclass() != null && (mf != USER_DECLARED_METHODS
        || clazz.getSuperclass() != Object.class)) {
      doWithMethods(clazz.getSuperclass(), mc, mf);
    } else if (clazz.isInterface()) {
      for (Class<?> superIfc : clazz.getInterfaces()) {
        doWithMethods(superIfc, mc, mf);
      }
    }
  }

  /**
   * Get the unique set of declared methods on the leaf class and all superclasses. Leaf class
   * methods are included first and while traversing the superclass hierarchy any methods found with
   * signatures matching a method already included are filtered out.
   *
   * @param leafClass the class to introspect
   * @param mf        the filter that determines the methods to take into account
   * @throws IllegalStateException if introspection fails
   */
  public static Method[] getUniqueDeclaredMethods(Class<?> leafClass, @Nullable MethodFilter mf) {
    final List<Method> methods = new ArrayList<>(32);
    doWithMethods(leafClass, method -> {
      boolean knownSignature = false;
      Method methodBeingOverriddenWithCovariantReturnType = null;
      for (Method existingMethod : methods) {
        if (method.getName().equals(existingMethod.getName()) &&
            method.getParameterCount() == existingMethod.getParameterCount() &&
            Arrays.equals(method.getParameterTypes(), existingMethod.getParameterTypes())) {
          // Is this a covariant return type situation?
          if (existingMethod.getReturnType() != method.getReturnType() &&
              existingMethod.getReturnType().isAssignableFrom(method.getReturnType())) {
            methodBeingOverriddenWithCovariantReturnType = existingMethod;
          } else {
            knownSignature = true;
          }
          break;
        }
      }
      if (methodBeingOverriddenWithCovariantReturnType != null) {
        methods.remove(methodBeingOverriddenWithCovariantReturnType);
      }
      if (!knownSignature && !isCglibRenamedMethod(method)) {
        methods.add(method);
      }
    }, mf);
    return methods.toArray(EMPTY_METHOD_ARRAY);
  }

  /**
   * Variant of {@link Class#getDeclaredMethods()} that uses a local cache in order to avoid the
   * JVM's SecurityManager check and new Method instances. In addition, it also includes Java 8
   * default methods from locally implemented interfaces, since those are effectively to be treated
   * just like declared methods.
   *
   * @param clazz the class to introspect
   * @return the cached array of methods
   * @throws IllegalStateException if introspection fails
   * @see Class#getDeclaredMethods()
   */
  public static Method[] getDeclaredMethods(Class<?> clazz) {
    return getDeclaredMethods(clazz, true);
  }

  private static Method[] getDeclaredMethods(Class<?> clazz, boolean defensive) {
    assertNotNull(clazz, "Class must not be null");
    Method[] result = declaredMethodsCache.get(clazz);
    if (result == null) {
      try {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
        if (defaultMethods != null) {
          result = new Method[declaredMethods.length + defaultMethods.size()];
          System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
          int index = declaredMethods.length;
          for (Method defaultMethod : defaultMethods) {
            result[index] = defaultMethod;
            index++;
          }
        } else {
          result = declaredMethods;
        }
        declaredMethodsCache.put(clazz, (result.length == 0 ? EMPTY_METHOD_ARRAY : result));
      } catch (Throwable ex) {
        throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
            "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
      }
    }
    return (result.length == 0 || !defensive) ? result : result.clone();
  }

  @Nullable
  private static List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
    List<Method> result = null;
    for (Class<?> ifc : clazz.getInterfaces()) {
      for (Method ifcMethod : ifc.getMethods()) {
        if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
          if (result == null) {
            result = new ArrayList<>();
          }
          result.add(ifcMethod);
        }
      }
    }
    return result;
  }

  /**
   * Determine whether the given method is an "equals" method.
   *
   * @see java.lang.Object#equals(Object)
   */
  public static boolean isEqualsMethod(@Nullable Method method) {
    if (method == null) {
      return false;
    }
    if (method.getParameterCount() != 1) {
      return false;
    }
    if (!"equals".equals(method.getName())) {
      return false;
    }
    return method.getParameterTypes()[0] == Object.class;
  }

  /**
   * Determine whether the given method is a "hashCode" method.
   *
   * @see java.lang.Object#hashCode()
   */
  public static boolean isHashCodeMethod(@Nullable Method method) {
    return method != null && method.getParameterCount() == 0 && "hashCode".equals(method.getName());
  }

  /**
   * Determine whether the given method is a "toString" method.
   *
   * @see java.lang.Object#toString()
   */
  public static boolean isToStringMethod(@Nullable Method method) {
    return (method != null && method.getParameterCount() == 0 && "toString"
        .equals(method.getName()));
  }

  /**
   * Determine whether the given method is originally declared by {@link java.lang.Object}.
   */
  public static boolean isObjectMethod(@Nullable Method method) {
    return (method != null && (method.getDeclaringClass() == Object.class ||
        isEqualsMethod(method) || isHashCodeMethod(method) || isToStringMethod(method)));
  }

  /**
   * Determine whether the given method is a CGLIB 'renamed' method, following the pattern
   * "CGLIB$methodName$0".
   *
   * @param renamedMethod the method to check
   */
  public static boolean isCglibRenamedMethod(Method renamedMethod) {
    String name = renamedMethod.getName();
    if (name.startsWith(CGLIB_RENAMED_METHOD_PREFIX)) {
      int i = name.length() - 1;
      while (i >= 0 && Character.isDigit(name.charAt(i))) {
        i--;
      }
      return (i > CGLIB_RENAMED_METHOD_PREFIX.length() && (i < name.length() - 1)
          && name.charAt(i) == '$');
    }
    return false;
  }

  /**
   * Make the given method accessible, explicitly setting it accessible if necessary. The
   * {@code setAccessible(true)} method is only called when actually necessary, to avoid unnecessary
   * conflicts with a JVM SecurityManager (if active).
   *
   * @param method the method to make accessible
   * @see java.lang.reflect.Method#setAccessible
   */
  @SuppressWarnings("deprecation")  // on JDK 9
  public static void makeAccessible(Method method) {
    if ((!Modifier.isPublic(method.getModifiers()) ||
        !Modifier.isPublic(method.getDeclaringClass().getModifiers())) && !method.isAccessible()) {
      method.setAccessible(true);
    }
  }

  // Field handling

  /**
   * Attempt to find a {@link Field field} on the supplied {@link Class} with the supplied
   * {@code name}. Searches all superclasses up to {@link Object}.
   *
   * @param clazz the class to introspect
   * @param name  the name of the field
   * @return the corresponding Field object, or {@code null} if not found
   */
  @Nullable
  public static Field findField(Class<?> clazz, String name) {
    return findField(clazz, name, null);
  }

  /**
   * Attempt to find a {@link Field field} on the supplied {@link Class} with the supplied
   * {@code name} and/or {@link Class type}. Searches all superclasses up to {@link Object}.
   *
   * @param clazz the class to introspect
   * @param name  the name of the field (may be {@code null} if type is specified)
   * @param type  the type of the field (may be {@code null} if name is specified)
   * @return the corresponding Field object, or {@code null} if not found
   */
  @Nullable
  public static Field findField(Class<?> clazz, @Nullable String name, @Nullable Class<?> type) {
    assertNotNull(clazz, "Class must not be null");
    assertTrue(name != null || type != null, "Either name or type of the field must be specified");
    Class<?> searchType = clazz;
    while (Object.class != searchType && searchType != null) {
      Field[] fields = getDeclaredFields(searchType);
      for (Field field : fields) {
        if ((name == null || name.equals(field.getName())) &&
            (type == null || type.equals(field.getType()))) {
          return field;
        }
      }
      searchType = searchType.getSuperclass();
    }
    return null;
  }

  /**
   * Set the field represented by the supplied {@linkplain Field field object} on the specified
   * {@linkplain Object target object} to the specified {@code value}.
   * <p>In accordance with {@link Field#set(Object, Object)} semantics, the new value
   * is automatically unwrapped if the underlying field has a primitive type.
   * <p>This method does not support setting {@code static final} fields.
   * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException(Exception)}.
   *
   * @param field  the field to set
   * @param target the target object on which to set the field
   * @param value  the value to set (may be {@code null})
   */
  public static void setField(Field field, @Nullable Object target, @Nullable Object value) {
    try {
      field.set(target, value);
    } catch (IllegalAccessException ex) {
      handleReflectionException(ex);
    }
  }

  /**
   * Get the field represented by the supplied {@link Field field object} on the specified
   * {@link Object target object}. In accordance with {@link Field#get(Object)} semantics, the
   * returned value is automatically wrapped if the underlying field has a primitive type.
   * <p>Thrown exceptions are handled via a call to {@link #handleReflectionException(Exception)}.
   *
   * @param field  the field to get
   * @param target the target object from which to get the field
   * @return the field's current value
   */
  @Nullable
  public static Object getField(Field field, @Nullable Object target) {
    try {
      return field.get(target);
    } catch (IllegalAccessException ex) {
      handleReflectionException(ex);
    }
    throw new IllegalStateException("Should never get here");
  }

  /**
   * Invoke the given callback on all locally declared fields in the given class.
   *
   * @param clazz the target class to analyze
   * @param fc    the callback to invoke for each field
   * @throws IllegalStateException if introspection fails
   * @see #doWithFields
   */
  public static void doWithLocalFields(Class<?> clazz, FieldCallback fc) {
    for (Field field : getDeclaredFields(clazz)) {
      try {
        fc.doWith(field);
      } catch (IllegalAccessException ex) {
        throw new IllegalStateException(
            "Not allowed to access field '" + field.getName() + "': " + ex);
      }
    }
  }

  /**
   * Invoke the given callback on all fields in the target class, going up the class hierarchy to
   * get all declared fields.
   *
   * @param clazz the target class to analyze
   * @param fc    the callback to invoke for each field
   * @throws IllegalStateException if introspection fails
   */
  public static void doWithFields(Class<?> clazz, FieldCallback fc) {
    doWithFields(clazz, fc, null);
  }

  /**
   * Invoke the given callback on all fields in the target class, going up the class hierarchy to
   * get all declared fields.
   *
   * @param clazz the target class to analyze
   * @param fc    the callback to invoke for each field
   * @param ff    the filter that determines the fields to apply the callback to
   * @throws IllegalStateException if introspection fails
   */
  public static void doWithFields(Class<?> clazz, FieldCallback fc, @Nullable FieldFilter ff) {
    // Keep backing up the inheritance hierarchy.
    Class<?> targetClass = clazz;
    do {
      Field[] fields = getDeclaredFields(targetClass);
      for (Field field : fields) {
        if (ff != null && !ff.matches(field)) {
          continue;
        }
        try {
          fc.doWith(field);
        } catch (IllegalAccessException ex) {
          throw new IllegalStateException(
              "Not allowed to access field '" + field.getName() + "': " + ex);
        }
      }
      targetClass = targetClass.getSuperclass();
    }
    while (targetClass != null && targetClass != Object.class);
  }

  /**
   * This variant retrieves {@link Class#getDeclaredFields()} from a local cache in order to avoid
   * the JVM's SecurityManager check and defensive array copying.
   *
   * @param clazz the class to introspect
   * @return the cached array of fields
   * @throws IllegalStateException if introspection fails
   * @see Class#getDeclaredFields()
   */
  private static Field[] getDeclaredFields(Class<?> clazz) {
    assertNotNull(clazz, "Class must not be null");
    Field[] result = declaredFieldsCache.get(clazz);
    if (result == null) {
      try {
        result = clazz.getDeclaredFields();
        declaredFieldsCache.put(clazz, (result.length == 0 ? EMPTY_FIELD_ARRAY : result));
      } catch (Throwable ex) {
        throw new IllegalStateException("Failed to introspect Class [" + clazz.getName() +
            "] from ClassLoader [" + clazz.getClassLoader() + "]", ex);
      }
    }
    return result;
  }

  /**
   * Determine whether the given field is a "public static final" constant.
   *
   * @param field the field to check
   */
  public static boolean isPublicStaticFinal(Field field) {
    int modifiers = field.getModifiers();
    return (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier
        .isFinal(modifiers));
  }

  // Cache handling

  /**
   * Clear the internal method/field cache.
   */
  public static void clearCache() {
    declaredMethodsCache.clear();
    declaredFieldsCache.clear();
  }


  /**
   * Action to take on each method.
   */
  @FunctionalInterface
  public interface MethodCallback {

    /**
     * Perform an operation using the given method.
     *
     * @param method the method to operate on
     */
    void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
  }


  /**
   * Callback optionally used to filter methods to be operated on by a method callback.
   */
  @FunctionalInterface
  public interface MethodFilter {

    /**
     * Determine whether the given method matches.
     *
     * @param method the method to check
     */
    boolean matches(Method method);

    /**
     * Create a composite filter based on this filter <em>and</em> the provided filter.
     * <p>If this filter does not match, the next filter will not be applied.
     *
     * @param next the next {@code MethodFilter}
     * @return a composite {@code MethodFilter}
     * @throws IllegalArgumentException if the MethodFilter argument is {@code null}
     */
    default MethodFilter and(MethodFilter next) {
      assertNotNull(next, "Next MethodFilter must not be null");
      return method -> matches(method) && next.matches(method);
    }
  }


  /**
   * Callback interface invoked on each field in the hierarchy.
   */
  @FunctionalInterface
  public interface FieldCallback {

    /**
     * Perform an operation using the given field.
     *
     * @param field the field to operate on
     */
    void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
  }


  /**
   * Callback optionally used to filter fields to be operated on by a field callback.
   */
  @FunctionalInterface
  public interface FieldFilter {

    /**
     * Determine whether the given field matches.
     *
     * @param field the field to check
     */
    boolean matches(Field field);

    /**
     * Create a composite filter based on this filter <em>and</em> the provided filter.
     * <p>If this filter does not match, the next filter will not be applied.
     *
     * @param next the next {@code FieldFilter}
     * @return a composite {@code FieldFilter}
     * @throws IllegalArgumentException if the FieldFilter argument is {@code null}
     */
    default FieldFilter and(FieldFilter next) {
      assertNotNull(next, "Next FieldFilter must not be null");
      return field -> matches(field) && next.matches(field);
    }
  }
}
