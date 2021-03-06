# post请求阻塞问题回顾

### 前言

昨天在处理`post	`请求的时候，遇到了`socket`的`inputstream`被阻塞的情况，前前后后查了很多资料，试了很多方法，但是问题依然无法解决。

我甚至还去看了`Tomcat`的源码，也没发现好的解决思路，但是在查资料的过程中，我已经找到问题的原因了，所以今天主要是来梳理下问题的原因，总结下经验，让各位小伙伴在以后遇到类似问题的时候，可以少踩坑，少走弯路。

### 问题的原因

问题的原因很简单，就是因为我们`socket`实现用的是传统的`I/O`流，这种实现有一个显著的缺点——阻塞，因为本身它就是阻塞是通信。

这种实现方式下的`socket`通信处理过程是这样的：客户端像服务器端发起访问请求（假设服务器已经启动），服务器端接受访问请求后，开始建立通信（`InputStream`和`OutputStream`），但是线路只有一条，发送消息的时候不能接收消息，接收消息的时候也是不能发送消息的。所以，在实际数据传输的时候，服务端必须接受完客户端的所有消息后，才能返回响应消息。

在我们的项目中，阻塞就发生在接收消息阶段，服务器端一直在接收客户端消息，消息没有接受完成，通信管路被占用，所以客户端无法接收到响应。

这里还不得不说更详细的原因，也就是消息接收一直不完成的原因。我们都知道，`socket`通信是依赖`InputStream`和`OutputStream`流的，我们这里阻塞就是因为`InputStream`，在处理客户端请求的时候，我们需要不断地从`InputSteam`中读取数据，但是`InputStream`的`read`方法都是阻塞的，也就是说它读不到数据的时候，就一直等在那里等待客户端的数据，因为它不知道客户端的什么时候可以传完，这样线程就被阻塞了，后续业务也就无法处理。

`read`方法，按照官方给的文档，读取完成后应该返回`null`或者`-1`，在读取文件的的时候，是可以正常终止的，但是由于`socket`本身传输数据形式的特殊性，这个返回结果基本上是不会出现的。也就是说，如果已经没有数据了，你还在读取，这时候只会让线程阻塞。

如果这个时候，客户端终止请求，服务端就知道数据传输完了，线程就不再阻塞了，但是由于连接断开了，服务器的响应数据也就无法传给客户端了。

针对这个问题，大致有两种解决方案，一种就是客户端在发送数据最开始就告诉服务器，他要发送多少数据，然后服务器按照数据传输大小，终止读取。

我们之前有做过一个和银行对接的项目，用的就是`socket`，当时的约定的规范就是先在消息最开始发送本次消息大小，然后再拼接上消息内容，这样服务器可以先接收一个`int`的消息大小，然后后续数据再根据这个大小去读取，也就不会出现阻塞的情况。

另一种解决方案是在消息传输结束加一个结束符号。我们在最开始的`get`请求处理实现中，就是根据消息最后的空行（`\r\n`），如果读到这个内容，后续就不再读取在，这样就不会发生阻塞。

不知道大家还记不记得我们昨天说的请求头的知识点：

![](https://gitee.com/sysker/picBed/raw/master/images/20210602171928.png)

在`post`请求中，请求头与请求体之间是有一个空行的，但是在请求体结束后，并没有结束标识，所以无法确定啥时候请求体完成，这就导致了我们前面说的阻塞。

也有资料说，在`post`请求头中增加`content-length`，可以解决这个问题，从理论上讲应该没啥问题，但是我在实际测试的时候，发现也有问题，有时间再研究下。

### 总结

任何技术的诞生和出现，都是因为其他技术的缺陷或者局限性。`Nio`就是在这样的背景之下出现的，`Nio`全称就是`new I/O`，表面它是一种新的实现方式的`I/O`，它是一种非阻塞式流，它可以让你在读的同时，实现的操作，而且目前`NIO`的应用特别广泛，`netty`就是基于`Nio`构建的一个`web`框架。

说了这么多，其实就想说一句，如果某种实现方式满足不了当下的业务需求，那就换种实现方式继续战斗。只要思想不滑坡，办法总比问题多😹

另外需要补充说明下，后面我会再试下`content-length`的方式，如果还是解决不了这个问题，就打算直接上`NIO`。

好了，大家周末快乐呀！

