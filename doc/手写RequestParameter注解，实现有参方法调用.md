# 2021.05.31 手写RequestParameter注解，实现有参方法调用

### 前言

昨天实现了requestMapping和controller注解，解决了请求地址与接口方法之间的映射问题，可以根据前端请求地址调用对应的接口方法。但是还不能解决有参方法的调用，今天我们就来啃下这个硬碴，解决这个最后一公里。

### 开肝

#### 定义RequestParameter注解

`springboot`的注解是`RequestParam`，我们今天实现的需求就是参考`RequestParam`，但是我没有时间去研究`springboot`的源码（至少今天没有时间），就先按照自己的想法来实现了。和前面注解不一样的一点是，这个注解的`target`指定的是`ElementType.PARAMETER`，因为这个注解是加在方法的参数上的。

```java
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParameter {
    String value();
}
```

加到方法的参数上是这样的：

```java
@RequestMapping("/sayHello")
    public String test(@RequestParameter("name") String name) {
        return "hello," + name;
    }
```

是不是看着还挺像像样的。

#### 业务端使用

目前业务处理还是在`doDispatcher`方法，所以今天依然是动这个方法。

先说下思路，思路也很简单，就是服务器收到前端请求后，根据`requestMapping`（请求地址）拿到对应的方法，从方法中拿到参数注解`RequestParameter`（这个注解的作用就是标记参数名，让我们可以根据参数名拿到参数），然后根据注解的`value`拿到参数名，然后根据参数名从`requestAttributeMap`拿到请求参数的值，组装方法入参列表。

```java
/**
     * 请求分发处理
     * @throws Exception
     */
    public void doDispatcher() throws Exception{
        RequestHear requestHear = request.getRequestHear();
        Map<String, Object> requestAttributeMap = request.getRequestAttributeMap();
        logger.info("请求头信息：{}", requestHear);
        logger.info("请求信息：{}", requestAttributeMap);
        if (Objects.isNull(requestHear) || Objects.isNull(requestAttributeMap)) {
            return;
        }
        String requestMapping = requestHear.getRequestMapping();
        if (requestMappingMap.containsKey(requestMapping)) {
            Method method = requestMappingMap.get(requestMapping);
            logger.debug("method:{}", method);
            Class<?> declaringClass = method.getDeclaringClass();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Object[] parameters = new Object[parameterAnnotations.length];
            for (int i = 0; i < parameterAnnotations.length; i++) {
                RequestParameter annotation = (RequestParameter)parameterAnnotations[i][0];
                parameters[i] = requestAttributeMap.get(annotation.value());
            }
            Object o = declaringClass.newInstance();
            Object invoke = method.invoke(o, parameters);
            logger.info("invoke:{}", invoke);
            response.write(String.format("hello syskeCat, dateTime:%d\n result = %s", System.currentTimeMillis(), invoke));
        } else {
            response.write(404, String.format("resources not found :%d", System.currentTimeMillis()));
        }
        socket.close();
    }
```

我单独把修改部分拿出来，简单解释下。

```java
Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Object[] parameters = new Object[parameterAnnotations.length];
            for (int i = 0; i < parameterAnnotations.length; i++) {
                RequestParameter annotation = (RequestParameter)parameterAnnotations[i][0];
                parameters[i] = requestAttributeMap.get(annotation.value());
            }
            Object o = declaringClass.newInstance();
            Object invoke = method.invoke(o, parameters);
```

`method.getParameterAnnotations`的作用是获取方法的所有参数注解，返回结果是一个二维数组，因为一个方法可以有多个参数，每个参数都可以有多个注解，所以肯定是一个二维数组。`parameterAnnotations[0][0]`就是第一个参数的第一个注解，`parameterAnnotations[1][0]`就是第二个参数的第一个注解，其他以此类推。这张图更清楚地说明了这一点：

![](https://gitee.com/sysker/picBed/raw/master/images/20210601102404.png)

因为获取到的注解是`Annotation`，并非是我们的定义的注解，所以需要进行强制转换成我们的自定义注解`RequestParameter`。

然后通过注解的`value()`方法从注解中拿到参数名，根据参数名从参数`map`中拿到请求的值，组装成参数集合，然后反射调用。

请求参数处理这边我们也做了一些调整，主要是为了获取请求参数参数：

```java
 Map<String, Object> attributeMap = Maps.newHashMap();
        if (requestMapping.contains("?")) {
            int endIndex = requestMapping.lastIndexOf('?');
            String requestParameterStr = requestMapping.substring(endIndex + 1);
            requestMapping = requestMapping.substring(0, endIndex);
            String[] split = requestParameterStr.split("&");
            for (String s : split) {
                String[] split1 = s.split("=");
                attributeMap.put(StringUtil.trim(split1[0]), StringUtil.trim(split1[1]));
            }

        }
```

主要就是在增加了请求参数的处理，把`sayHello?name=yunzhongzhi&age=12`处理成`requestMapping`和请求参数`map`。这块后期还需要优化，要把`get`请求和`post`请求分开，分别处理，所以目前我们的服务器只支持`get`请求。

#### 测试

完成以上调整，我们的方法就已经支持有参调用了，我们来测试下吧！

![](https://gitee.com/sysker/picBed/raw/master/images/20210601094459.png)



![](https://gitee.com/sysker/picBed/raw/master/images/20210601094541.png)

![](https://gitee.com/sysker/picBed/raw/master/images/20210601105044.png)

效果还不错，目标完美达成。

### 总结

感觉有思路的话，写东西还是比较快的，这些需求都是今天早上实现的，中间被`Annotation`卡住了，一直无法获取到参数名，后来发现只要强转一下就行了。千里之行，始于足下，感兴趣的小伙伴肝起来。



下面是项目的开源仓库，有兴趣的小伙伴可以去看看，如果有想法的小伙伴，我真心推荐你自己动个手，自己写一下，真的感觉不错：

```
https://github.com/Syske/syske-boot
```

![](https://gitee.com/sysker/picBed/raw/master/images/20210530130936.png)