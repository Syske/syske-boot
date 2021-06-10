# 手写web服务器：服务器重构，实现post请求处理

### 前言

前几天一直被`post`请求处理的问题卡着，因此`web`服务器这边也没啥进展，再加上昨天又突然被告知要去加班，所以这个问题就一直被一次次往后拖，还好今天有时间，然后就抽空把这个问题彻底解决了，然后服务这边也彻底从原来的`socket`，被我重构成`Nio`的`ServerSocketChannel`，也就是我们前面说的非阻塞式`socket`，今天主要介绍整个重构过程，`nio`的知识点暂时也不打算讲，因为我也没有搞得特别清楚。好了，话不多说，直接重构。

### 重构

手写我们重新写了`sokcet`的核心程序，实现方式彻底改变了，首先是一个服务器接收客户端请求的线程：

#### 接收服务器请求线程

```
 static class AcceptSocketThread extends Thread {
        volatile boolean runningFlag = true;

        @Override
        public void run() {
            try {
                ServerSocketChannel serverChannel = ServerSocketChannel.open();
                serverChannel.bind(new InetSocketAddress(30000));
                serverChannel.configureBlocking(false);

                while (runningFlag) {
                    SocketChannel channel = serverChannel.accept();

                    if (null == channel) {
                        logger.info("服务端监听中.....");
                    } else {
                        channel.configureBlocking(false);
                        logger.info("一个客户端上线，占用端口 ：{}", channel.socket().getPort());
                        keys.put(channel.socket().getPort(), channel);
                        new ResponseThread().start();
                    }
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
```

在线程内部，我们通过`ServerSocketChannel.open`创建了一个`ServerSocketChannel`通信频道，并设置频道端口是`30000`;

`configureBlocking`是设置当前通信是否阻塞，这里我们设置的是`false`，也就是非阻塞通信；

然后通过一个死循环监听服务器`serverChannel`是否被连接，这里`serverChannel.accept()`返回值为`null`表示未建立连接或者连接被关闭；

如果建立连接，我们将通信频道放进`keys`通信频道队列中：

```
public static volatile Map<Integer, SocketChannel> keys =
        Collections.synchronizedMap(new HashMap<>());
```

并启动一个响应请求线程去处理这个频道中的请求，下面我们看处理线程

#### 处理请求线程

在写这些文字时候，我发现这里其实没必要创建队列存放会话频道，可以直接把这块的队列传进线程，并处理（因为我这块代码是参考别人的，然后进行了大改，后面还需要进一步优化）

```java
/**
     * 处理客户端请求
     */
    static class ResponseThread extends Thread {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        @Override
        public void run() {
            int num = 0;
            Iterator<Integer> ite = keys.keySet().iterator();
            while (ite.hasNext()) {
                int key = ite.next();
                StringBuffer stb = new StringBuffer();
                try {
                    SocketChannel socketChannel = keys.get(key);
                    if (Objects.isNull(socketChannel)) {
                        break;
                    }
                    while ((num = socketChannel.read(buffer)) > 0) {
                        buffer.flip();
                        stb.append(charset.decode(buffer).toString());
                        buffer.clear();
                    }
                    if (stb.length() > 0) {
                        MsgWrapper msg = new MsgWrapper();
                        msg.key = key;
                        msg.msg = stb.toString();
                        logger.info("端口：{}的通道，读取到的数据：{}",msg.key, msg.msg);
                        msgQueue.add(msg);
                        threadPoolExecutor.execute(new SyskeRequestNioHandler(socketChannel, msg.msg));
                        ite.remove();
                    }
                } catch (Exception e) {
                    ite.remove();
                    logger.error("error: 端口占用为：{}，的连接的客户端下线了", keys.get(key).socket().getPort(), e);
                }
            }
            logger.info("读取线程监听中......");
        }

    }
```

因为原代码，作者的接收线程、处理线程都是在`main`方法启动的，所以他这样定义是`ok`的，但我这里其实就没必要了。

看了上面的代码，大家会发现，`nio`中不再有`InputStream`或者`OutputStream`这样的类，这是因为`nio`的底层实现采用了新的架构，有一个`selector`进行频道管理，当某个频道有数据进来的时候，`selector`会切换到这个频道进行数据处理，如果没有数据他会去处理其他频道的数据，不像我们之前的`I/O`，一次通信就一个管道，没有数据就一直等待，所以也就不会导致阻塞。

我觉得有个例子能很好地说明这两种模型，传统的`I/o`就好比一个单位的电话，电话虽然很多，但是线路只有一条，同时只能有一个电话进行通话，电话不断，其他人根本就打不进去，也没法接电话，只能等着这个接收电话的人打完电话；

`Nio`就相当于这个单位为了解决同时只能有一个人打电话这种情况，专门雇了一个接线员负责线路切换，当有电话进来以后，接线员会把对应的电话借给对应的人，这样即提高了线路的效率，也避免了阻塞的情况。

![](https://gitee.com/sysker/picBed/raw/master/images/20210606191422.png)

做完上面的改动后，我们的`post`请求就不再阻塞了，然后我们还优化了`request`的初始化。

#### 优化请求初始化

现在不论`get`请求，还是`post`请求，最终都会拿到一个纯文本的请求参数，然后我我把它分别处理成`header`（请求方法、请求地址）、`requestAttributeMap`（请求头参数）、`requestBody`（请求体）：

```java
 private void initRequest() throws IllegalParameterException{
        logger.info("SyskeRequest start init");
        String[] inputs = input.split("\r\n");
        System.out.println(Arrays.toString(inputs));
        Map<String, Object> attributeMap = Maps.newHashMap();
        boolean hasBanlk = false;
        StringBuilder requestBodyBuilder = new StringBuilder();
        for (int i = 0; i < inputs.length; i++) {
            if(i == 0) {
                String[] headers = inputs[0].split(" ", 3);
                String requestMapping = headers[1];
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
                this.header = new RequestHear(RequestMethod.match(headers[0]), requestMapping);
            } else {
                if (StringUtil.isEmpty(inputs[i])) {
                    hasBanlk = true;
                }
                if (inputs[i].contains(":") && Objects.equals(hasBanlk, Boolean.FALSE)) {
                    String[] split = inputs[i].split(":", 2);
                    attributeMap.put(split[0], split[1]);
                } else {
                    // post 请求
                    requestBodyBuilder.append(inputs[i]);
                }
            }
        }
        requestAttributeMap = attributeMap;
        requestBody = JSON.parseObject(requestBodyBuilder.toString());
        logger.info("requestBodyBuilder: {}", requestBodyBuilder.toString());
        logger.info("SyskeRequest init finished. header: {}, requestAttributeMap: {}", header, requestAttributeMap);
    }
```

这里就很简单了，就是通过`\r\n`分割即可。

`get`和`post`唯一的区别就是，`get`请求的参数都在`requestAttributeMap`，而`post`的请求参数在`requestBody`。

```java
/**
     * 处理post请求
     * @param method
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    private Object doPost(Method method) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        JSONObject requestBody = (JSONObject)request.getRequestBody();
        return doRequest(method, requestBody);
    }

    /**
     * 处理get请求
     * @param method
     * @param requestAttributeMap
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Object doGet(Method method, Map<String, Object> requestAttributeMap) throws InvocationTargetException, IllegalAccessException, InstantiationException {
       return doRequest(method, requestAttributeMap);
    }
```

#### 测试

然后我们测试下看下，这里就只测试`post`了，这里我用的`postMan`：

![](https://gitee.com/sysker/picBed/raw/master/images/20210606194347.png)

看下后台：

![](https://gitee.com/sysker/picBed/raw/master/images/20210606194429.png)

请求体已经有数据了，后面的就很简单了

### 总结

我现在越来越觉得，作为一个`web`后端工程师，网络编程是一个特别重要的技能，因为你不了解数据在网络中的传输过程，不了解各种协议，不了解各种请求头，那你再遇到具体问题的时候，是根本没有任何思路的。

可能在你眼里你可能会觉得一切你解决不了的问题，都是玄学问题，但事实并非如此。

所以，对我现在而言，学习的方向大概分为这几种：

- 多线程：这个应该是一个比较核心，掌握的好，你的工作真的会事半功倍的
- 网络编程：这个原因我前面说了
- 算法，包括数据结构等：帮助你构建更好的模型，让你的程序运行更快，性能更好
- 虚拟化相关知识，比如`docker`、`k8s`等，以及`jenkins`自动化构建，这一块现在是比较主流的技术
- 主流开源框架学习，这里我会花比较少的时间，以搞清楚具体的原理和实现方式为目的

今天把这个问题解决了，后面又可以继续实现`springboot`的其他注解了，继续搞事情。好了，今天就到这里吧