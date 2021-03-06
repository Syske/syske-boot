package io.github.syske.boot.handler;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.github.syske.boot.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @program: syske-boot
 * @description: 核心扫描工具
 * @author: syske
 * @date: 2021-05-30 15:54
 */
public class SyskeBootContentScanHandler {
    private static final Logger logger = LoggerFactory.getLogger(SyskeBootContentScanHandler.class);

    private static Set<Class> classSet = Sets.newHashSet();
    private static Map<String, Method> requestMappingMap = Maps.newHashMap();
    private static Map<String, Object> contentMap = Maps.newHashMap();

    private SyskeBootContentScanHandler() {
    }

    /**
     * 获取请求方法Map
     *
     * @return
     */
    public static Map<String, Method> getRequestMappingMap() {
        return requestMappingMap;
    }

    /**
     * 获取IOC集合
     *
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
    public static void init(Class aClass) {
        try {
            // 扫描包
            componentScanInit(aClass);
            // 初始化配置类
            initConfiguration();
            // 初始化IoC容器
            initSyskeBootContent();
            // 扫描controller的RequestMapping
            initRequestMappingMap();
        } catch (Exception e) {
            logger.error("syske-boot 启动异常：", e);
        }
    }

    /**
     * 扫描指定的包路径，如果无该路径，则默认扫描服务器核心入口所在路径
     *
     * @param aClass
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void componentScanInit(Class aClass) throws IOException, ClassNotFoundException {
        logger.info("componentScanInit start init……");
        logger.info("componentScanInit aClass: {}", aClass);
        Annotation annotation = aClass.getAnnotation(ComponentScan.class);
        if (Objects.isNull(annotation)) {
            Package aPackage = aClass.getPackage();
            scanPackage(aPackage.toString(), classSet);
        } else {
            String[] value = ((ComponentScan) annotation).value();
            for (String s : value) {
                scanPackage(s, classSet);
            }
        }
        logger.info("componentScanInit end, classSet = {}", classSet);
    }

    /**
     * 扫描controller的RequestMapping
     */
    private static void initRequestMappingMap() {
        logger.info("start to scanRequestMapping, controllerSet = {}", classSet);
        if (classSet == null) {
            return;
        }
        classSet.forEach(aClass -> {
            Annotation controller = aClass.getAnnotation(Controller.class);
            if (Objects.isNull(controller)) {
                return;
            }
            Method[] methods = aClass.getDeclaredMethods();
            for (Method method : methods) {
                RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                if (Objects.nonNull(annotation)) {
                    requestMappingMap.put(annotation.value(), method);
                }
            }
            Field[] fields = aClass.getDeclaredFields();
            try {
                Object o = aClass.newInstance();
                for (Field field : fields) {
                    Autowired annotation = field.getAnnotation(Autowired.class);
                    if (Objects.nonNull(annotation)) {
                        field.setAccessible(true);
                        field.set(o, contentMap.get(field.getType().getName()));
                    }
                    ConfigurationHandler.initValueConfig(o, field);
                }
                contentMap.put(aClass.getName(), o);
            } catch (InstantiationException e) {
                logger.error("初始controller失败：", e);
            } catch (IllegalAccessException e) {
                logger.error("初始controller失败：", e);
            }
        });
        logger.info("scanRequestMapping end, requestMappingMap = {}", requestMappingMap);
    }

    /**
     * 初始化容器：扫描service注解类
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private static void initSyskeBootContent() {
        logger.info("start to initSyskeBootContent ……");
        long startTIme = System.currentTimeMillis();
        if (classSet == null || classSet.size() == 0) {
            return;
        }
        classSet.forEach(c -> {
            try {
                // 初始化过滤器
                FilterHandler.init(c);
                if (AnnotationUtil.hasAnnotation(c, Service.class)) {
                    String name = c.getName();
                    Object o = c.newInstance();
                    ConfigurationHandler.batchInitValueConfig(c, o);
                    contentMap.put(name, o);
                }
            } catch (Exception e) {
                logger.error("容器初始化失败", e);
            }

        });
        long useTime = System.currentTimeMillis() - startTIme;
        logger.info("initSyskeBootContent finished. useTime {}ms", useTime);
    }

    /**
     * 扫描指定包名下所有类，并生成classSet
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
                File[] files = packagePath.listFiles();
                for (File file : files) {
                    String fileName = file.getName();
                    if (file.isDirectory()) {
                        String newPackageName = String.format("%s.%s", packageName, fileName);
                        scanPackage(newPackageName, classSet);
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
    }

    /**
     * 初始化配置类
     */
    private static void initConfiguration() {
        classSet.forEach(c -> {
            try {
                if (AnnotationUtil.hasAnnotation(c, Configuration.class)) {
                    Method[] methods = c.getMethods();
                    Object o = c.newInstance();
                    ConfigurationHandler.batchInitValueConfig(c, o);
                    for (Method method : methods) {
                        Bean annotation = method.getAnnotation(Bean.class);
                        if (Objects.nonNull(annotation)) {
                            Object invoke = method.invoke(o);
                            contentMap.put(method.getReturnType().getName(), invoke);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("扫描配置类错误", e);
            }
        });
    }

}
