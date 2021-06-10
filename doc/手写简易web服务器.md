# 2021.05.28 手写简易web服务器

### 前言

已经做`java`开发好几年了，但是一直觉得`web`服务器是个很神秘的东西，之前有参考过一个大佬用`netty`写的简易版的`springboot`，但是最后不了了之了。今天，突然又有了想写个`web`服务器了，所以就有了今天个小`demo`。

但是`demo`还是有点问题，就是浏览器请求的时候，服务端无法响应，但是把浏览器请求终止掉，服务端开始响应了，但是这时候响应结果已经无法传给浏览器了。这个问题，后期得研究下。

### 开整

今天才真正知道并理解了`Tomcat`是基于`socket`实现的，以前可能看到过相关博客和文档，但是一直无法正在理解，想不明白它到底是咋样通过`Socket`实现的，直到今天写完这个`demo`，我发现突然就想明白了，顿悟的感觉，所以我已经决定自己要动手写一个`web`框架，名字就叫`syske-boot`，`flag`先立起来。

#### 服务端

服务端其实就是一个`socket`服务端，用于处理客户端请求。

```java
public class SyskeCatServer {
    private static final int port = 8080;
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("SyskeCatServer is starting……");
            while (true){
                Socket accept = serverSocket.accept();
                InputStream inputStream = accept.getInputStream();
                OutputStream outputStream = accept.getOutputStream();
                SimpleHttpServer simpleHttpServer = new SimpleHttpServer();
                SyskeRequest syskeRequest = new SyskeRequest(inputStream);
                SyskeResponse syskeResponse = new SyskeResponse(outputStream);
                simpleHttpServer.doService(syskeRequest, syskeResponse);
                accept.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverSocket.close();
        }
    }
}
```

首先，从`serverSocket`中分别获取输入流和输出流，这里的`SyskeRequest`就是对`InputStream`的封装，`SyskeResponse`就是对`OutputStream`的封装。我们根据这个也可以大胆推测，`tomcat`的`Request`和`Response`也是类似的实现。

#### doService方法

```java
class SimpleHttpServer {

    public void doService(SyskeRequest request, SyskeResponse response) throws Exception {
        try {
            byte[] bytes = new byte[1024];
            int read;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while ((read = request.getInputStream().read(bytes)) != -1) {
                byteArrayOutputStream.write(bytes);
            }
            byte[] toByteArray = byteArrayOutputStream.toByteArray();
            String requestStr = new String(toByteArray);
            System.out.println(String.format("请求参数：%s", requestStr));
            String[] split = requestStr.split("\r\n");
            System.out.println("end");
            response.write("hello syskeCat");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

#### SyskeRequest

这里就是包装了`InputStream`

```java
public class SyskeRequest {
    private InputStream inputStream;

    public InputStream getInputStream() {
        return inputStream;
    }

    public SyskeRequest(InputStream inputStream) {
        this.inputStream = inputStream;
    }
}
```

#### SyskeResponse

这里就是对`OutputStream`简单包装

```java
public class SyskeResponse {
        private OutputStream outputStream;

        public SyskeResponse(OutputStream outputStream) {
            this.outputStream = outputStream;
        }

        //将文本转换为字节流
        public void write(String content) throws IOException {
            StringBuffer httpResponse = new StringBuffer();
            httpResponse.append("HTTP/1.1 200 OK\n")      //按照HTTP响应报文的格式写入
                    .append("Content-Type:text/html\n")
                    .append("\r\n")
                    .append("<html><head><link rel=\"icon\" href=\"data:;base64,=\"></head><body>")
                    .append(content)          //将页面内容写入
                    .append("</body></html>");
            outputStream.write(httpResponse.toString().getBytes());      //将文本转为字节流
            outputStream.close();
        }
}
```

#### 测试

好了，上面这些就是核心代码了，是不是很简单。我们来运行下看看，直接运行`main`方法就行了。服务端启动了：

![](https://gitee.com/sysker/picBed/raw/master/images/20210529173623.png)

浏览器访问下看看：

![](https://gitee.com/sysker/picBed/raw/master/images/20210529173824.png)

这里浏览器一直没响应，但是后台也没有收到请求，但是当我终止请求后（点击浏览器的`x`），服务端收到了请求：

![](https://gitee.com/sysker/picBed/raw/master/images/20210529174212.png)

看下后端收到的`socket`请求参数，是不是很熟悉：

```
请求参数：GET /tfgdfgdf HTTP/1.1
Host: localhost:8080
Connection: keep-alive
Cache-Control: max-age=0
sec-ch-ua: " Not A;Brand";v="99", "Chromium";v="90", "Google Chrome";v="90"
sec-ch-ua-mobile: ?0
Upgrade-Insecure-Requests: 1
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
Sec-Fetch-Site: none
Sec-Fetch-Mode: navigate
Sec-Fetch-User: ?1
Sec-Fetch-Dest: document
Accept-Encoding: gzip, deflate, br
Accept-Language: zh-CN,zh;q=0.9
Cookie: NMTID=00O3GJZgJXpkp8vI0cpmNRliUnGvYkAAAF4sPsi2w
```

这不就是浏览器的请求头吗？里面包括了请求地址、以及客户端的其他信息。

如果你想实现更加请求地址响应的需求，只需要解析下请求数据就行了，我觉得这些都简单了，最难的是从`0`到`1`，就是浏览器可以请求后可以收到响应，我觉得我现在做到了`0.5`，后面的`0.5`还需要再研究下。

### 总结

整体来说，这个实例还是比较简单的，虽然最后没有完整地达到了最终的目的，但是还是有收获的，至少服务请求收到了吧，所以从这个点上将，收获还是大于预期的，而且对我而言还发现新大陆，只要这个缺憾，后面再好好摸索下，反正这个硬砍我啃定了。