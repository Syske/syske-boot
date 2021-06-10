# 手写web服务器：实现简单filter逻辑

### 前言

今天早上起床的时候，我还在想应该实现哪个组件，想了半天，发现基本上常用的注解组件都被我们給实现了（当然，虽然都实现了，但是基本上都是简易版），最后想来想去觉得`filter`可以实现下，毕竟没有这个模块，项目中的就没法实现权限控制了。

在开始的时候，我已经知道`filter`的难点是地址的匹配，也就是如何把我们配置的地址转换为正则表达式，最后发现这块涉及的知识点有点多，所以今天就只演示通配地址，即`/*`。

好了，我们一起来看下吧。

#### 过滤器实现过程

#### 定义注解

这里依然很轻车熟路，注解的配置我增加了很多属性，最核心的就是`urlPatterns`，也就是我们的拦截地址。

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WebFilter {
    String[] value() default {};

    String description() default "";

    String displayName() default "";

    String[] urlPatterns() default {};
}
```

#### 定义Filter接口

这里参考了`javaEE`的`filter`接口

```java
public interface Filter {
    default void init() {}

    void doFilter(Request request, Response response, FilterChain filterChain) throws IOException;

    default void destrory() {}
}

public interface FilterChain {
    /**
     * filter调用链
     * @param request
     * @param response
     * @throws IOException
     */
    void doFilter(Request request, Response response) throws IOException;
}
```

#### 定义filter

加上`webFilter`，并配置拦截地址，实现`doFilter`方法

```java
@WebFilter(urlPatterns = {"/*"}, description = "test filter")
public class TestFilter implements Filter{
    @Override
    public void doFilter(Request request, Response response, FilterChain filterChain) throws IOException {
        System.out.println(String.format("过滤器TestFilter被访问，拦截地址：%s", request.getRequestHear().getRequestMapping()));
        if (Objects.nonNull(filterChain)) {
            filterChain.doFilter(request, response);
        }
    }
}
```



#### 过滤器初始化

这里主要是配合`IoC`，拿到过滤器的配置信息，并实例化`filter`

```java
private static Map<Filter, String[]> filterUrlPatternsMap = Maps.newHashMap();

    private static LinkedList<Filter> filterLinkedList = Lists.newLinkedList();

    public static Map<Filter, String[]> getFilterUrlPatternsMap() {
        return filterUrlPatternsMap;
    }

    public static LinkedList<Filter> getFilterLinkedList() {
        return filterLinkedList;
    }

    public static void init(Class zClass) throws IllegalAccessException, InstantiationException {
        Annotation webFilter = zClass.getAnnotation(WebFilter.class);
        if (Objects.nonNull(webFilter)) {
            String[] urlPatterns = ((WebFilter) webFilter).urlPatterns();
            Filter filter = (Filter)zClass.newInstance();
            filterUrlPatternsMap.put(filter, urlPatterns);
            filterLinkedList.add(filter);
        }
    }
```

#### 修改`disPatcher`方法

这里就是对请求地址进行过滤，当匹配到请求时，执行匹配到过滤器的`doFilter`方法

```java
Map<Filter, String[]> filterUrlPatternsMap = FilterHandler.getFilterUrlPatternsMap();
            LinkedList<Filter> filterLinkedList = FilterHandler.getFilterLinkedList();
            ListIterator<Filter> filterListIterator = filterLinkedList.listIterator();
            while (filterListIterator.hasNext()) {
                Filter filter = filterListIterator.next();
                String[] values = filterUrlPatternsMap.get(filter);
                for (String value : values) {
                    if(Pattern.matches(value.replace('/', '.'), requestMapping)) {
                        filter.doFilter(request, response, null);
                    }

                }
            }
```

#### 测试

我们用浏览器访问任意地址，比如`/testAutowire`，会看到`doFilter`方法被执行了，控制台打印如下信息：

![](https://gitee.com/sysker/picBed/raw/master/images/20210610133129.png)

说明，我们的`filter`已经起作用了，是不是很简单呀。

#### 总结

虽然`filter`的核心功能实现了，但作为一个合格的`web`服务器，拦截器也得够健壮，够灵活，所以还有很多工作要做：

比如要实现链式调用，就是实现多个过滤器的顺序调用，层层调用，层层返回，形成调用链，这一块后面要进一步实现；

另外一个问题就是，我们要实现更灵活的地址匹配，前面我也说了，目前的地址只实现了通配和严格匹配两种，正则表达式支持的也不够完美，这一块也是后面要优化的点。

好了，核心内容到这里就结束了，但我想说两句闲话，这两天在刷一个制作精良的剧——《觉醒年代》，这应该是这几年，我看过最有价值的电视剧了，让我对很多革命先烈有了更深刻的认识，有兴趣的小伙伴可以去看下，真的很赞。

最近这几天，我追得根本停不下来，这两天都睡得很晚，有时候就到一点了，早上醒来已经七点多了，洗漱完就八点了，九点得出门，所以留给我写代码、写文章的时间不到一个小时，稍微一卡壳，就只能做一样，然后中午只有半个小时写文章，所以内容就显得有些仓促，感觉每天都像打仗，后面得注意下了，得按时休息了。