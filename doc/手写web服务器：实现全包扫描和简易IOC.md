# 2021.06.01 手写web服务器：实现全包扫描和简易IOC

### 前言

最近写`web`服务器优点上头，根本停不下来，对，我就是卷王本卷，昨天实现了前端有参方法的调用，趁着这股热劲，今天我们来实现下`IOC`容器，从原理和实现上来讲，都不难了，因为我们在`controller`和`requestMapping`注解实现的时候已经验证过了，沿着同样的思路搞就行了。好了话不多说，直接开整。

### 开整

同样是基于我们之前的代码实现，感兴趣的小伙伴可以去看完整代码，文末有项目地址。

#### Serive注解

写这个注解主要是为了测试，本来要实现`Component`，一时半会没想起来单词如何拼写，所以就选择了`service`。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    String value() default "";
}
```

注解的内容和前面的都差不多，很简单，就是加了个`target`和`Retention`，然后把这个类加在我们的`service`上即可：

```java
@Service
public class TestService {

    public void helloIoc(String name) {
        System.out.println("hello ioc, " + name);
    }
}
```

#### 优化包扫描器

之前的包扫描器只能扫描一层目录，这样每层都指定包名就很繁琐，所以我把它简单优化了下，这样只用输入根包名，就可以实现整包扫描。

这里用到了递归，当路径是文件夹时，就会再次调用自己。

```java
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
```

#### 测试

我们指定个包路径测试下，顺便测试下通过`Ioc`拿到对象，实现方法调用：

```java
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
```

包扫描完后，生成一个`class`的`set`集合;

通过`scanRequestMapping`方法从`class`集合中拿出`controller`的类，并生成`requestMapping`和方法的集合；

通过`initSyskeBootContent`方法从`class`集合中拿出`service`的类，并创建实例，放进`contentMap`，这样在你需要实例的时候，直接通过全类名（包名 + 类名）就可以拿到，然后执行你想要执行的方法即可。

看下效果：

```
883  [main] INFO  i.g.s.b.h.SyskeBootContentScanHandler - scanRequestMapping end, requestMappingMap = {/sayHello2=public java.lang.String io.github.syske.boot.controller.TestController.test(java.lang.String,java.lang.String), /sayHello=public java.lang.String io.github.syske.boot.controller.TestController.testName(java.lang.String), /test2=public java.lang.String io.github.syske.boot.controller.Test2Controller.test2(), /test=public java.lang.String io.github.syske.boot.controller.TestController.testRequstMapping()} 
883  [main] INFO  i.g.s.b.h.SyskeBootContentScanHandler - requestMappingMap = {/sayHello2=public java.lang.String io.github.syske.boot.controller.TestController.test(java.lang.String,java.lang.String), /sayHello=public java.lang.String io.github.syske.boot.controller.TestController.testName(java.lang.String), /test2=public java.lang.String io.github.syske.boot.controller.Test2Controller.test2(), /test=public java.lang.String io.github.syske.boot.controller.TestController.testRequstMapping()} 
899  [main] INFO  i.g.s.b.h.SyskeBootContentScanHandler - contentMap = {io.github.syske.boot.service.TestService=io.github.syske.boot.service.TestService@2812cbfa} 
hello ioc, 云中志
```

方法完美被执行，想法实现，打完收工。

### 总结

又是看起来复杂、写起来不难的一次需求，但是通过这样的方式，能让你更深入的理解`spring`的`ioc`原理，当然原理可能会有差异，但是也大同小异，再退一步来说，就算不一样，面试的时候，面试官问你懂不懂`Ioc`底层原理，你也可以大胆地我自己做过类似于`Ioc`东西，这一点就很牛皮了。

最近内卷这个词特别火，所有的平台都在讨论，但是`IT`这个行业不早都在内卷了吗？面试造火箭，进门拧螺丝，太卷了。

前几天看到一个段子，说是一个公司招司机，面试官问司机，你知道汽车的启动过程吗？能大概说一下吗？然后司机巴拉巴拉说了一大堆：

![](https://gitee.com/sysker/picBed/raw/master/images/20210602084740.png)

段子原文地址，有兴趣的小伙伴自己去看：

```
https://blog.csdn.net/dfskhgalshgkajghljgh/article/details/106457745
```



下面是项目的开源仓库，有兴趣的小伙伴可以去看看，如果有想法的小伙伴，我真心推荐你自己动个手，自己写一下，真的感觉不错：

```
https://github.com/Syske/syske-boot
```

![](https://gitee.com/sysker/picBed/raw/master/images/20210530130936.png)