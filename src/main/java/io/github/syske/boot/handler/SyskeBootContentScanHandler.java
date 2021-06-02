package io.github.syske.boot.handler;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.github.syske.boot.annotation.Service;
import io.github.syske.boot.service.TestService;
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
    private static Set<Class> classSet = Sets.newHashSet();
    private static Map<String, Method> requestMappingMap = Maps.newHashMap();
    private static Map<String, Object> contentMap = Maps.newHashMap();

    private SyskeBootContentScanHandler() {}

    /**
     * 获取请求方法Map
     * @return
     */
    public static Map<String, Method> getRequestMappingMap() {
        return requestMappingMap;
    }

    /**
     * 获取IOC集合
     * @return
     */
    public static Map<String, Object> getContentMap() {
        return contentMap;
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
                if (Objects.nonNull(annotation)) {
                    requestMappingMap.put(annotation.value(), method);
                }
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

    private static void initSyskeBootContent(Set<Class> classSet) {
        if(classSet == null || classSet.size() == 0) {
            return;
        }
        classSet.forEach(c -> {
            try {
                if (hasAnnotation(c, Service.class)) {
                    String name = c.getName();
                    Object o = c.newInstance();
                    contentMap.put(name, o);
                }
            } catch (Exception e) {
                logger.error("容器初始化失败", e);
            }

        });
    }

    private static boolean hasAnnotation(Class zClass, Class annotationClass) {
        Annotation annotation = zClass.getAnnotation(annotationClass);
        return Objects.nonNull(annotation);
    }

    private static void scanPackageToIoc(String packageName, Set<Class> classSet)
            throws IOException, ClassNotFoundException {
        logger.info("start to scanPackage, packageName = {}", packageName);
        Enumeration<URL> classes = ClassLoader.getSystemResources(packageName.replace('.', '/'));
        while (classes.hasMoreElements()) {
            URL url = classes.nextElement();
            File packagePath = new File(url.getPath());
            if (packagePath.isDirectory()) {
                File[] files = packagePath.listFiles();
                for (File file : files) {
                    String fileName = file.getName();
                    if (file.isDirectory()) {
                        String newPackageName = String.format("%s.%s", packageName, fileName);
                        scanPackageToIoc(newPackageName, classSet);
                    } else {
                        String className = fileName.substring(0, fileName.lastIndexOf('.'));
                        String fullClassName = String.format("%s.%s", packageName, className);
                        classSet.add(Class.forName(fullClassName));
                    }
                }
            } else {
                String className = url.getPath().substring(0, url.getPath().lastIndexOf('.'));
                String fullClassName = String.format("%s.%s", packageName, className);
                classSet.add(Class.forName(fullClassName));
            }
        }
        logger.info("scanPackage end, classSet = {}", classSet);
    }

    public static void main(String[] args) throws Exception{
        scanPackageToIoc("io.github.syske.boot", classSet);
        logger.info("classSet = {}", classSet);
        scanRequestMapping(classSet);
        logger.info("requestMappingMap = {}", requestMappingMap);
        initSyskeBootContent(classSet);
        logger.info("contentMap = {}", contentMap);
        Object o = contentMap.get("io.github.syske.boot.service.TestService");
        if (o instanceof TestService) {
            ((TestService)o).helloIoc("云中志");
        }
    }

}
