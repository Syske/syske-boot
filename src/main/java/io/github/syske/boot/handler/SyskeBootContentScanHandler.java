package io.github.syske.boot.handler;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.github.syske.boot.annotation.RequestMapping;

/**
 * @program: syske-boot
 * @description: 核心扫描工具
 * @author: syske
 * @date: 2021-05-30 15:54
 */
public class SyskeBootContentScanHandler {
    private static final Logger logger = LoggerFactory.getLogger(SyskeBootContentScanHandler.class);

    private static Set<Class> controllerSet = Sets.newHashSet();
    private static Map<String, Method> requestMappingMap = Maps.newHashMap();

    private SyskeBootContentScanHandler() {}

    /**
     * 获取请求方法Map
     * @return
     */
    public static Map<String, Method> getRequestMappingMap() {
        return requestMappingMap;
    }

    /**
     * 类加载器初始化
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static void init() {
        try {
            // 扫描conttoller
            scanPackage("io.github.syske.boot.controller", controllerSet);
            // 扫描controller的RequestMapping
            scanRequestMapping(controllerSet);
        } catch (Exception e) {
            logger.error("syske-boot 启动异常：", e);
        }
    }

    /**
     * 扫描controller的RequestMapping
     * 
     * @param controllerSet
     */
    private static void scanRequestMapping(Set<Class> controllerSet) {
        logger.info("start to scanRequestMapping, controllerSet = {}", controllerSet);
        if (controllerSet == null) {
            return;
        }
        controllerSet.forEach(aClass -> {
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                requestMappingMap.put(annotation.value(), method);
            }
        });
        logger.info("scanRequestMapping end, requestMappingMap = {}", requestMappingMap);
    }

    /**
     * 扫描指定的包名下的类
     * 
     * @param packageName
     * @param classSet
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void scanPackage(String packageName, Set<Class> classSet)
        throws IOException, ClassNotFoundException {
        logger.info("start to scanPackage, packageName = {}", packageName);
        Enumeration<URL> classes = ClassLoader.getSystemResources(packageName.replace('.', '/'));
        while (classes.hasMoreElements()) {
            URL url = classes.nextElement();
            File packagePath = new File(url.getPath());
            if (packagePath.isDirectory()) {
                String[] files = packagePath.list();
                for (String fileName : files) {
                    String className = fileName.substring(0, fileName.lastIndexOf('.'));
                    String fullClassName = String.format("%s.%s", packageName, className);
                    classSet.add(Class.forName(fullClassName));
                }
            }
        }
        logger.info("scanPackage end, classSet = {}", classSet);
    }

}
