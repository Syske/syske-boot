# 手写web服务器：定义Configuration和Bean注解，实现更灵活的类注入

### 前言

前几天，我们实现了`Service`注解，解决了类注入的问题（`component`注解后面实现，这个就很简单了），但是这种方式不够灵活，比如我们要实现某些属性的赋值，或者其他特殊的构建方法，这些注解就不够灵活了，为了解决这个问题，`spring`提供了`Configuration`注解和`Bean`注解，今天我们就参照这两个注解的功用，用我自己的方式来实现这两个注解，让我们的类注入更灵活。

话不多说，直接开始。

### 实现过程

#### 定义注解

`configuration`注解，我们从一开始就在写注解，所以到现在都是闭着眼睛写的，`so easy`！

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
}
```

`Bean`注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
}
```

#### 核心实现

我们说的核心实现，主要是指这两个注解的使用，因为`Bean`注解的作用是创建对象，所以必须在服务器启动前完成创建，但是不能早于包扫描前，我们要在包扫描完成后进行：

```java
// 扫描包
componentScanInit(aClass);
// 初始化配置类
initConfiguration();
```

我们看下配置类初始化里面是如何实现的：

```java
	/**
     * 初始化配置类
     */
private static void initConfiguration() {
    classSet.forEach(c -> {
        try {
            if (hasAnnotation(c, Configuration.class)) {
                Method[] methods = c.getMethods();
                Object o = c.newInstance();
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
```

这里也很简单，就是循环遍历我们的包扫描结果，拿出有`Configuration`注解的类，然后找到有`Bean`注解的方法，运行方法，把方法运行结果放进我们的`Ioc`容器即可。

#### 简单应用

我们有这样一个类，我们需要通过`Bean`和`Configuration`注解来实现更灵活的类注入

```java
public class TestBean {
    private String name;
    private int age;

    public TestBean() {
    }

    public TestBean(String name) {
        System.out.println("create bean, " + name);
    }
    public void testBean() {
        System.out.println(this);
        System.out.println("hello bean");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
    @Override
    public String toString() {
        return "TestBean{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
```

我们的配置类：

```
@Configuration
public class TestConfig {
    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setAge(18);
        testBean.setName("云中志");
        return testBean;
    }
}
```

#### 测试

我们在`Controller`中注入`testBean`:

```java
@Controller
public class Test2Controller {

    @Autowired
    private TestService service;

    @Autowired
    private TestBean testBean;

    @RequestMapping("/testAutowire")
    public String testAutowire(@RequestParameter("name") String name){
        testBean.testBean();
        return service.helloIoc(name);
    }
}
```

然后，我们前端访问下看下效果：

![](https://gitee.com/sysker/picBed/raw/master/images/20210608083456.png)

配置类中的属性也已经被注入进来了，这种方式的好处就是比较灵活，你可以根据自己的需要调用对应的方法，实现符合你需求的构建方式，是不是很简单呀!

### 总结

今天的内容总体来说还是比较简单的，核心的点就是注解的解析和方法的反射调用，当然难点也是有的，你要在实现之前考虑好思路，因为代码本身只是思路的表达，所以在我的认知理解中，我觉得合格的软件工程师，首先得是个合格的架构师和设计师，否则你真的是能当个小码农了，遇到需求的时候，先想想如果让你来做，你会如何实现，而不是考虑该不该你来做，每一次你排斥的事情，其实对你而言，都是一次机会，重要的是你如何看待它。

好了，今天的内容就到这里吧。
