package org.example.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationNotAllowedException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import java.util.concurrent.Executors;

@Component
public class SpringBeanFactoryUtil implements ApplicationContextAware, BeanFactoryAware, SmartInstantiationAwareBeanPostProcessor {
    public final static String BEANNAME_PREFIX_SCOPEDTARGET = "scopedTarget.";
    private static ApplicationContext applicationContext;
    private static BeanFactory beanDefinitionRegistry;
    private static final List<BeanOperationListener> beanOperationListenerList = Collections.synchronizedList(new ArrayList<>());

    private SpringBeanFactoryUtil() {
    }

    public static enum Scope {
        Singleton,
        Prototype;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (applicationContext == null) {
            applicationContext = context;
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (beanDefinitionRegistry == null) {
            beanDefinitionRegistry = beanFactory;
        }
    }

    /**
     * 檢查Bean是否已經在Container中
     *
     * @param <T>
     * @param beanClass
     * @param toCamelCase
     * @return
     */
    public static <T> boolean isBeanExist(final Class<T> beanClass, boolean... toCamelCase) {
        return isBeanExist(beanClass.getName(), toCamelCase);
    }

    /**
     * 檢查Bean是否已經在Container中
     *
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    public static boolean isBeanExist(final String beanClassName, boolean... toCamelCase) {
        try {
            String beanName = getBeanName(beanClassName, toCamelCase);
            return applicationContext.containsBean(beanName);
        } catch (Throwable t) {
            fireGetOnException(beanClassName, t);
            return false;
        }
    }

    /**
     * 根據類獲取bean
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> T getBean(final Class<T> clazz) {
        return getBean(clazz, true);
    }

    /**
     * 根據類獲取bean
     *
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T> T getBean(final Class<T> clazz, boolean fireGetOnException) {
        try {
            return (T) applicationContext.getBean(clazz);
        } catch (Throwable t) {
            if (fireGetOnException) {
                fireGetOnException(clazz.getName(), t);
            }
            return null;
        }
    }

    /**
     * 根據name獲取bean
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(final String beanClassName, boolean... toCamelCase) {
        String beanName = getBeanName(beanClassName, toCamelCase);
        try {
            return (T) applicationContext.getBean(beanName);
        } catch (Throwable t) {
            fireGetOnException(beanClassName, t);
            return null;
        }
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final Class<T> beanClass, boolean... toCamelCase) {
        return registerBean(beanClass, Scope.Singleton, null, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final Class<T> beanClass, final Object[] constructorArgumentValues, boolean... toCamelCase) {
        return registerBean(beanClass, Scope.Singleton, constructorArgumentValues, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param scope
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final Class<T> beanClass, final Scope scope, boolean... toCamelCase) {
        return registerBean(beanClass, scope, null, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param scope
     * @param constructorArgumentValues
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final Class<T> beanClass, final Scope scope, final Object[] constructorArgumentValues, boolean... toCamelCase) {
        return registerBean(beanClass.getName(), scope, constructorArgumentValues, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final String beanClassName, boolean... toCamelCase) {
        return registerBean(beanClassName, Scope.Singleton, null, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final String beanClassName, final Object[] constructorArgumentValues, boolean... toCamelCase) {
        return registerBean(beanClassName, Scope.Singleton, constructorArgumentValues, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param scope
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final String beanClassName, final Scope scope, boolean... toCamelCase) {
        return registerBean(beanClassName, scope, null, toCamelCase);
    }

    /**
     * 手動註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param scope
     * @param constructorArgumentValues
     * @param toCamelCase
     * @return
     */
    public static <T> T registerBean(final String beanClassName, final Scope scope, final Object[] constructorArgumentValues, boolean... toCamelCase) {
        String beanName = getBeanName(beanClassName, toCamelCase);
        if (applicationContext.containsBean(beanName)) {
            return getBean(beanName);
        }
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClassName);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        if (scope == Scope.Prototype) {
            beanDefinition.setScope("prototype");
        }
        if (ArrayUtils.isNotEmpty(constructorArgumentValues)) {
            ConstructorArgumentValues constructor = new ConstructorArgumentValues();
            for (int i = 0; i < constructorArgumentValues.length; i++) {
                constructor.addIndexedArgumentValue(i, constructorArgumentValues[i]);
            }
            beanDefinition.setConstructorArgumentValues(constructor);
        }
        try {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanDefinitionRegistry;
            registry.registerBeanDefinition(beanName, beanDefinition);
        } catch (Throwable t) {
            fireRegisterOnException(beanClassName, t);
            return null;
        }
        try {
            return getBean(beanName);
        } catch (Throwable t) {
            unregisterBean(beanClassName, toCamelCase);
            return null;
        }
    }

    /**
     * 反註冊一個bean
     *
     * @param <T>
     * @param beanClass
     * @param toCamelCase
     */
    public static <T> boolean unregisterBean(final Class<T> beanClass, boolean... toCamelCase) {
        return unregisterBean(beanClass.getName(), toCamelCase);
    }

    /**
     * 反註冊一個bean
     *
     * @param <T>
     * @param beanClassName
     * @param toCamelCase
     */
    public static <T> boolean unregisterBean(final String beanClassName, boolean... toCamelCase) {
        String beanName = getBeanName(beanClassName, toCamelCase);
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanDefinitionRegistry;
        if (!registry.containsBeanDefinition(beanName)) {
            return false;
        }
        try {
            registry.removeBeanDefinition(beanName);
            return true;
        } catch (Throwable t) {
            fireUnregisterOnException(beanClassName, t);
            return false;
        }
    }

    /**
     * 註冊一個Controller
     *
     * @param <T>
     * @param controllerClass
     * @param toCamelCase
     */
    public static <T> T registerController(final Class<T> controllerClass, boolean... toCamelCase) {
        return registerController(controllerClass.getName(), toCamelCase);
    }

    /**
     * 註冊一個Controller
     *
     * @param <T>
     * @param controllerClassName
     * @param toCamelCase
     */
    public static <T> T registerController(final String controllerClassName, boolean... toCamelCase) {
        String beanName = getBeanName(controllerClassName, toCamelCase);
        T controller = registerBean(beanName);
        try {
            RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
            Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods", Object.class);
            ReflectionUtils.makeAccessible(method);
            method.invoke(requestMappingHandlerMapping, beanName);
        } catch (Throwable t) {
            fireRegisterOnException(controllerClassName, t);
        }
        return controller;
    }

    /**
     * 反註冊一個Controller
     *
     * @param <T>
     * @param controllerClass
     * @param toCamelCase
     */
    public static <T> boolean unregisterController(final Class<T> controllerClass, boolean... toCamelCase) {
        return unregisterController(controllerClass.getName(), toCamelCase);
    }

    /**
     * 反註冊一個Controller
     *
     * @param <T>
     * @param controllerClassName
     * @param toCamelCase
     */
    public static <T> boolean unregisterController(final String controllerClassName, boolean... toCamelCase) {
        T controller = getBean(controllerClassName, toCamelCase);
        if (controller == null) {
            // 等於null, 不代表BeanDefinitionRegistry中沒有, 所以還是要檢查一下
            if (!unregisterBean(controllerClassName, toCamelCase)) {
            }
            return false;
        }
        try {
            RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
            Class<?> targetClass = controller.getClass();
            ReflectionUtils.doWithMethods(targetClass, (Method method) -> {
                Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
                try {
                    Method createMappingMethod = RequestMappingHandlerMapping.class.getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                    ReflectionUtils.makeAccessible(createMappingMethod);
                    RequestMappingInfo requestMappingInfo = (RequestMappingInfo) createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);
                    if (requestMappingInfo != null) {
                        requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                    }
                } catch (Exception e) {
                    fireRegisterOnException(controllerClassName, e);
                }
            }, ReflectionUtils.USER_DECLARED_METHODS);
            return true;
        } catch (BeanCreationNotAllowedException e) {
            fireUnregisterOnException(controllerClassName, e);
        } catch (Throwable t) {
            fireUnregisterOnException(controllerClassName, t);
        } finally {
            // 最後要unregisterBean
            return unregisterBean(controllerClassName, toCamelCase);
        }
    }

    /**
     * 根據類名獲取beanName
     *
     * @param beanClassName
     * @param toCamelCase
     * @return
     */
    private static String getBeanName(final String beanClassName, boolean... toCamelCase) {
        String beanName = beanClassName;
        if (ArrayUtils.isNotEmpty(toCamelCase) && toCamelCase[0]) {
            int index = beanClassName.lastIndexOf(".");
            if (index >= 0) {
                beanName = beanName.substring(index + 1);
            }
            beanName = toLowerCase(beanName, 0, 0);
        }
        return beanName;
    }
    /**
     * 將指定beginIndex到endIndex的字符轉為小寫
     *
     * @param str
     * @param beginIndex
     * @param endIndex
     * @return
     */
    public static String toLowerCase(String str, int beginIndex, int endIndex) {
        if (StringUtils.isBlank(str)) {
            return str;
        }
        if (beginIndex > endIndex) {
            throw createIllegalArgumentException("beginIndex = [", beginIndex, "] cannot bigger than endIndex = [", endIndex, "]!!");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < beginIndex - 1; i++) {
                sb.append(str.charAt(i));
            }
            for (int i = beginIndex; i <= endIndex; i++) {
                sb.append(String.valueOf(str.charAt(i)).toLowerCase());
            }
            for (int i = endIndex + 1; i < str.length(); i++) {
                sb.append(str.charAt(i));
            }
            return sb.toString();
        }
    }

    public static IllegalArgumentException createIllegalArgumentException(Object... messages) {
        return new IllegalArgumentException(StringUtils.join(messages));
    }
    public static interface BeanOperationListener extends EventListener {
        public default void registerOnException(String beanName, Throwable t) {
        }

        public default void getOnException(String beanName, Throwable t) {
        }

        public default void unregisterOnException(String beanName, Throwable t) {
        }
    }

    public static void addBeanOperationListener(BeanOperationListener listener) {
        if (!beanOperationListenerList.contains(listener)) {
            beanOperationListenerList.add(listener);
        }
    }

    public static void removeBeanOperationListener(BeanOperationListener listener) {
        beanOperationListenerList.remove(listener);
    }

    private static void fireRegisterOnException(final String beanName, final Throwable t) {
        BeanOperationListener[] listeners = new BeanOperationListener[beanOperationListenerList.size()];
        beanOperationListenerList.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            for (BeanOperationListener listener : listeners) {
                Executors.newCachedThreadPool().execute(() -> {
                    listener.registerOnException(beanName, t);
                });
            }
        }
    }

    private static void fireGetOnException(final String beanName, final Throwable t) {
        BeanOperationListener[] listeners = new BeanOperationListener[beanOperationListenerList.size()];
        beanOperationListenerList.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            for (BeanOperationListener listener : listeners) {
                Executors.newCachedThreadPool().execute(() -> {
                    listener.getOnException(beanName, t);
                });
            }
        }
    }

    private static void fireUnregisterOnException(final String beanName, final Throwable t) {
        BeanOperationListener[] listeners = new BeanOperationListener[beanOperationListenerList.size()];
        beanOperationListenerList.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            for (BeanOperationListener listener : listeners) {
                Executors.newCachedThreadPool().execute(() -> {
                    listener.unregisterOnException(beanName, t);
                });
            }
        }
    }
}
