package io.github.syske.boot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 过滤器
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-10 8:12
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WebFilter {
    String[] value() default {};

    String description() default "";

    String displayName() default "";

    String[] urlPatterns() default {};
}
