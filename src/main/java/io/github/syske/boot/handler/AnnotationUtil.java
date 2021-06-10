package io.github.syske.boot.handler;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * 注解工具类
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-10 8:37
 */
public class AnnotationUtil {
    /**
     * 判断指定类是否有指定的注解
     *
     * @param zClass
     * @param annotationClass
     * @return
     */
    public static boolean hasAnnotation(Class zClass, Class annotationClass) {
        Annotation annotation = zClass.getAnnotation(annotationClass);
        return Objects.nonNull(annotation);
    }
}
