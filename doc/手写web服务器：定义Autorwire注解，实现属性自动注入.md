# 手写web服务器：定义Autorwired注解，实现属性自动注入

### 前言

昨天，我们已经解决了`post`请求的阻塞问题，所以我们今天又可以继续搞事情了，今天我们要实现的也是`spring`中很核心的注解——`Autowired`。这个注解想必大家肯定不陌生，在`spring`项目中，我们经常用它来为我们的属性注入值，实现属性的自动装配。

好了，话不多说，我们来看具体如何实现.

### 实现过程

#### 定义注解

这一块就很简单了，前面我们也不止一次写过，这里`target`指定的是属性

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
}
```

#### 加到属性上

直接把`@Autowired`注解加在我们要注入的属性上

![](https://gitee.com/sysker/picBed/raw/master/images/20210607084321.png)

#### 优化扫描方法

属性是在类初始化的时候，自动被赋值的，所以我们要调整初始化流程

```java
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
```

我们在初始化这里加了一段字段初始化的代码，上面是完整代码，字段初始化只有短短几行：

```java
Field[] fields = aClass.getDeclaredFields();
try {
    Object o = aClass.newInstance();
    for (Field field : fields) {
        Autowired annotation = field.getAnnotation(Autowired.class);
        if (Objects.nonNull(annotation)) {
            field.setAccessible(true);
            field.set(o, contentMap.get(field.getType().getName()));
        }
    }
    contentMap.put(aClass.getName(), o);
} catch (InstantiationException e) {
    logger.error("初始controller失败：", e);
} catch (IllegalAccessException e) {
    logger.error("初始controller失败：", e);
}
```

这里需要注意的是，因为属性是私有的，必须通过`getDeclaredFields`获取属性值，`getFields`方法是没办法拿到私有属性的；

另外一个需要注意的点是，私有属性必须通过`setAccessible`设置为可访问才可以，否则会报错：

![](https://gitee.com/sysker/picBed/raw/master/images/20210607085016.png)

因为字段赋值是基于对象实例的，所以我们要先创建类的实例：

```java
Object o = aClass.newInstance()
```

然后通过`field.set`给属性赋值，这里赋值要通过`IOC`容器拿到赋值对象的实例，所以被赋值属性的实例必须先初始化，否则会有问题。

同时，我们把带有`@Autowired`注解的类的实例也存进了`IOC`容器，这样在后面调用`controller`对用`mapping`方法的时候，我们直接从`ioc`容器中拿出来即可：

```java
Object o = contentMap.get(declaringClass.getName());
Object invoke = method.invoke(o, parameters);
```

![](https://gitee.com/sysker/picBed/raw/master/images/20210607085553.png)

这是因为如果你在调用的时候再去创建实例，这时候属性也要赋值，否则会报错的，所以初始化的时候直接创建实例是比较合理的方式。

#### 测试

浏览器调用下试下：

![](https://gitee.com/sysker/picBed/raw/master/images/20210607094217.png)

可以看到，我们调用的时候，`service`已经有值了，方法调用完成后，结果正常返回：

![](https://gitee.com/sysker/picBed/raw/master/images/20210607094401.png)

### 总结

好了，今天的内容到这里就结束了。在上面的内容中，我们展示了`@Autowired`注解的定义、具体的应用，以及`Ioc`对于`Autowired`注解的处理过程，最后我们经过测试，结果与预期一致，当然具体`springboot`是如何实现的，还需要进一步的研究和探讨，我这里分享的是自己的实现思路，感兴趣的小伙伴可以自己动手试下。