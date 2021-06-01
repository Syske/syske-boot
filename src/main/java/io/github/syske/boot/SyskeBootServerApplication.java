package io.github.syske.boot;

import io.github.syske.boot.handler.SyskeBootContentScanHandler;
import io.github.syske.boot.handler.SyskeRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: syske-boot
 * @description: syske-boot启动入口
 * @author: syske
 * @date: 2021-05-30 10:03
 */
public class SyskeBootServerApplication {
    private static final Logger logger = LoggerFactory.getLogger(SyskeBootServerApplication.class);
    private static final int SERVER_PORT = 8080;
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public static void main(String[] args) {
        SyskeBootContentScanHandler.init();
        start();
    }

    /**
     * 启动服务器
     * @throws Exception
     */
    public static void start() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT) ) {
            Socket accept = null;
            logger.info("SyskeCatServer is starting……, port: {}", SERVER_PORT);
            while ((accept = serverSocket.accept()) != null){
                threadPoolExecutor.execute(new SyskeRequestHandler(accept));
            }
        } catch (Exception e) {
            logger.error("服务器后端异常", e);
        }
    }
}
