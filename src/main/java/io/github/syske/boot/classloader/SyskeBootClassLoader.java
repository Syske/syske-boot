package io.github.syske.boot.classloader;

import io.github.syske.boot.annotation.RequestMapping;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;

/**
 * @program: syske-boot
 * @description: class loader
 * @author: syske
 * @date: 2021-05-30 15:54
 */
public class SyskeBootClassLoader extends ClassLoader {
    public static void main(String[] args) throws Exception{
        String classPath = "io.github.syske.boot.controller.TestController";

        Class<?> aClass = new SyskeBootClassLoader().loadClass(classPath);
        Annotation[] annotations = aClass.getAnnotations();
        System.out.println(Arrays.toString(annotations));
        scan();
        Method[] methods = aClass.getDeclaredMethods();
        for (Method method : methods) {
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            System.out.println(annotation.value());
        }
        System.out.println(aClass);
    }

    private static void scan() throws IOException {
        URL classpath = SyskeBootClassLoader.class.getClassLoader().getResource("./");
        System.out.println(classpath);
    }

}
