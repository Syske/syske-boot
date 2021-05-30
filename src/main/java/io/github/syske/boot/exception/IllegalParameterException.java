package io.github.syske.boot.exception;

/**
 * @program: syske-boot
 * @description: illegal parameter exception
 * @author: syske
 * @date: 2021-05-30 14:57
 */
public class IllegalParameterException extends Exception{
    public IllegalParameterException(String message) {
        super(message);
    }
}
