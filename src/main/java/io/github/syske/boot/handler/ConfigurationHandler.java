package io.github.syske.boot.handler;

import io.github.syske.boot.annotation.Value;
import io.github.syske.boot.util.PropertiesUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Objects;

/**
 * properties配置处理类
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-08 7:46
 */
public class ConfigurationHandler {
    private static final PropertiesUtil propertiesUtil = PropertiesUtil.getInstance("application");

    /**
     * 初始化value配置信息
     * @param instance
     * @param field
     * @throws IllegalAccessException
     */
    public static void initValueConfig(Object instance, Field field) throws IllegalAccessException {
        Annotation annotation = field.getAnnotation(Value.class);
        if (Objects.nonNull(annotation)) {
            String propertiesKeyName = ((Value) annotation).value();
            Class<?> type = field.getType();
            if (!field.isAccessible()) {
                field.setAccessible(Boolean.TRUE);
            }
            if (Integer.class.equals(type)) {
                field.setInt(instance, propertiesUtil.getInt(propertiesKeyName));
            } else if (Boolean.class.equals(type)) {
                field.setBoolean(instance, propertiesUtil.getBoolean(propertiesKeyName));
            } else {
                field.set(instance, propertiesUtil.get(propertiesKeyName));
            }
        }
    }

    /**
     * 批量初始化value配置
     * @param aClass
     * @param instance
     * @throws IllegalAccessException
     */
    public static void batchInitValueConfig(Class aClass, Object instance) throws IllegalAccessException {
        Field[] declaredFields = aClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            initValueConfig(instance, declaredField);
        }

    }
}
