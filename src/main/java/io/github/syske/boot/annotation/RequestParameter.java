package io.github.syske.boot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * requet pameter
 *
 * @author syske
 * @version 1.0
 * @date 2021-06-01 上午7:58
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParameter {
    String value();
}
