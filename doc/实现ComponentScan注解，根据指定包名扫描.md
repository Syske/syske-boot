# 2021.06.02 实现ComponentScan注解，根据指定包名扫描

### 前言

手写`web`服务器，到今天已经实现了`controller`、`RequestMapping`、`RequestParameter`、`Service`还有简易的`Ioc`，从构成要素上将我们的小项目已经算是一个比较完整的服务器了，但也有很多需要优化的地方，今天我们就来实现`ComponentScan`注解，优化一下包扫描，实现可以根据我们指定的包名进行组件扫描。

这个注解，功能上我们参考了`spring`，因为是纯手写，所以我们并没有去看`spring`的源码，最近确实也没时间看。

### 整起来

今天核心的工作就两个，首先是定义一个新的注解，然后我们根据这个注解去扫描指定的包。如果注解不存在，我们就从服务器入口，即`SyskeBootServerApplication`类所在包开始扫描。

#### 定义ComponentScan注解

现在定义注解以及是轻车熟路了 ，`so easy`！这里的`value() `方法定义的是数组，用于接受需要扫描的包名，也就是说我们也是支持多包名的。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScan {
    String[] value() ;
}
```

然后把`ComponentScan`注解加在我们服务的入口上：

![](https://gitee.com/sysker/picBed/raw/master/images/20210603081039.png)

为什么要加在服务器入口上呢？这是我人为规定的，现在还没有实现`Configuration`注解，加在服务器主入口是最好的选择。

#### 优化包扫描器

根据注解内容优化包扫描器：

```java
/**
     * 扫描指定的包路径，如果无该路径，则默认扫描服务器核心入口所在路径
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
            String[] value = ((ComponentScan)annotation).value();
            for (String s : value) {
                scanPackage(s, classSet);
            }
        }
        logger.info("componentScanInit end, classSet = {}", classSet);
    }
```

在服务器启动时，需要先执行`componentScanInit`方法，这个方法需要传入一个`Class`，也就是我们项目的主入口的类。

方法内部会先判断这个类是否有`ComponentScan`注解，如果有则根据注解`value()`的值进行扫描，否则拿到传入类的包路径，然后开始扫描。

#### 测试

运行启动下，我们发现控制台已经打印出了扫描到的类：

![](https://gitee.com/sysker/picBed/raw/master/images/20210603082638.png)



### 总结

今天的内容依然很简单，就只是实现了一个注解，然后根据这个注解优化了我们包扫描的业务代码，没有什么复杂的知识点，后面我们还需要对很多功能进行优化，包括以下几点：

- `post`请求处理与响应
- `GetMapping`、`PostMapping`的实现，这个两个注解实现起来很简单了，和`RequestMapping`基本上一致
- 配置注解的实现：`value`、`ConfigurationProperties`
- `get`请求页面模板实现
- 集中异常处理

目前大概能想到这几点，其他的等后面再说。明天应该会先解决`post`请求这块，思路已经有了。好了，今天就先到这里吧！