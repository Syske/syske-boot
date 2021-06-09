package io.github.syske.boot;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.syske.boot.annotation.ComponentScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.syske.boot.handler.SyskeBootContentScanHandler;
import io.github.syske.boot.handler.SyskeRequestNioHandler;

/**
 * @program: syske-boot
 * @description:
 * @author: syske
 * @date: 2021-06-06 15:53
 */
@ComponentScan({"io.github.syske.boot"})
public class NonBlockingSocketChannelServer {
    private static final Logger logger = LoggerFactory.getLogger(NonBlockingSocketChannelServer.class);
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    /**
     *  考虑并发 移除也可以用ConcurrentHashMap
      */
    public static volatile Map<Integer, SocketChannel> keys =
        Collections.synchronizedMap(new HashMap<>());
    static ConcurrentLinkedQueue<MsgWrapper> msgQueue = new ConcurrentLinkedQueue<>();
    static Charset charset = StandardCharsets.UTF_8;

    public static void main(String[] args) {
        SyskeBootContentScanHandler.init(NonBlockingSocketChannelServer.class);
        new AcceptSocketThread().start();
    }

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
//                        logger.info("服务端监听中.....");
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
                    if (Objects.nonNull(ite)) {
                        ite.remove();
                    }
                    logger.error("error: 端口占用为：{}，的连接的客户端下线了", keys.get(key).socket().getPort(), e);
                }
            }
            logger.info("读取线程监听中......");
        }

    }

    static class MsgWrapper {
        public int key;
        public String msg;
    }
}