# 2021.05.30 手写controller、requestMapping注解，实现简单请求

### 前言

今天我们还是继续研究手写`web`服务器，经过昨天一天，服务器这边，我已经基本实现了`controller`注解和`requestMapping`注解。

服务器启动的时候，会自动去扫描带有`controller`注解的类，然后根据`controller`再去扫描`requestMapping`注解，最后生成一个`key`为`url`，`value`为方法的`map`。

当后端接收到前端请求后，根据请求地址调用相应的方法，如果地址不存在，就返回`404`。目前，调用方法这块目前只实现了简单方法的调用，带入参的方法还没实现，也是同样的思路，通过反射直接调用，然后将返回值写入响应即可。

下面让我们一起看下我是如何实现的。

### Controller注解

首先定义一个注解，加了两个元注解，一个是表明我们的注解是加在类上面的，一个表明我们的类要保留到运行时。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    String value() default "";
}
```

同时，我们还为注解指定了一个方法（我不知道这个应该叫属性还是方法），目的是接收`controller`的名字。

### RequestMapping注解

这个注解和上面的注解类似，因为这个类是要加到方法和类上的，所以这个注解我在`target`上多加了一个`ElementType.METHOD`。`value()`是用来接收`url`的，后期可能还有增加请求方法这个字段，这个后期再说。

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String value();
}
```

加了上面两个注解的`controller`长这个样子：

```java
@Controller("test")
public class TestController {

    @RequestMapping("/test")
    public String testRequstMapping() {
        return "hello syske-boot";
    }
}
```

另一个

```java
@Controller
public class Test2Controller {

    @RequestMapping("/test2")
    public String test2() {
        return "test2";
    }
}
```



### 包扫描器

这里才是关键了，所有的类扫描都是基于这里实现的。后期实现`IoC`和`Aop`也要用到。

现在`controller`的包路径是写死的，后面可以通过注解加在服务器主入口上，就和`springboot`差不多，这个也很好实现。

这里的逻辑也很简单，就是扫描给定的包路径，判断类是否有`controller`注解，有就把它放进`controllerSet`。

然后再循环遍历`controllerSet`，将加了`@RequsetMapping`注解的方法放进`requestMappingMap`。

```java
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
```

到这里，包扫描器的逻辑就完了。后期，随着注解越来越多，考虑到兼容性，这块的方法应该还需要进一步的抽象封装。

### SyskeRequestHandler调整

上面的扫描最终的目的都是为了响应请求的时候能够更灵活，也是为这里服务的，所以需要对`doDispatcher`方法调整。

这里的逻辑也很简单，就是根据请求头中的地址，去匹配对应的方法，如果地址不存在就返回`404`。

如果方法存在，拿出对应的方法，反射调用即可。

现在是在`doDispatcher`方法内部实例化了`controller`，后面实现简单`IoC`之后，就可以从我们的容器中直接获取实例了。

```java
private void init() throws IOException, IllegalParameterException {
        this.syskeRequest = new SyskeRequest(socket.getInputStream());
        this.syskeResponse = new SyskeResponse(socket.getOutputStream());
        this.requestMappingMap = SyskeBootContentScanHandler.getRequestMappingMap();
    }
 public void doDispatcher() throws Exception{
        logger.info("请求头信息：{}", syskeRequest.getRequestHear());
        logger.info("请求信息：{}", syskeRequest.getRequestAttributeMap());
        String requestMapping = syskeRequest.getRequestHear().getRequestMapping();
        if (requestMappingMap.containsKey(requestMapping)) {
            Method method = requestMappingMap.get(requestMapping);
            logger.debug("method:{}", method);
            Class<?> declaringClass = method.getDeclaringClass();
            Object o = declaringClass.newInstance();
            Object invoke = method.invoke(o);
            logger.info("invoke:{}", invoke);
            syskeResponse.write(String.format("hello syskeCat, dateTime:%d\n result = %s", System.currentTimeMillis(), invoke));
        } else {
            syskeResponse.write(404, String.format("resources not found :%d", System.currentTimeMillis()));
        }
        socket.close();
    }
```

我们看下请求效果，我们分别调用上面两个`controller`接口试下，先看`/test`：

![](https://gitee.com/sysker/picBed/raw/master/images/20210531082259.png)

再看`/test2`：

![](https://gitee.com/sysker/picBed/raw/master/images/20210531082344.png)

`result`就是我们方法的返回值，说明我们的预期结果已经完美达成，后面就是好好打磨优化了。

### 总结

其实昨天方法调用这块还没实现，是刚刚写的，总体来说很简单，用到了反射的相关知识。下一步考虑先实现有参方法的调用问题，然后再实现`IoC`。总之，这个东西已经慢慢变成服务器该有的样子，一切还是让我觉得蛮意外的，所以大家有想法的时候，一定要努力去做，做了一切才有更多可能，我们一起加油吧！



下面是项目的开源仓库，有兴趣的小伙伴可以去看看，如果有想法的小伙伴，我真心推荐你自己动个手，自己写一下，真的感觉不错：

```
https://github.com/Syske/syske-boot
```

![](https://gitee.com/sysker/picBed/raw/master/images/20210530130936.png)