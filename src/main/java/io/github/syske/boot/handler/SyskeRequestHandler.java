package io.github.syske.boot.handler;

import com.google.common.collect.Maps;
import io.github.syske.boot.http.impl.SyskeRequest;
import io.github.syske.boot.http.impl.SyskeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @program: syske-boot
 * @description: 请求处理器
 * @author: syske
 * @date: 2021-05-30 10:05
 */
public class SyskeRequestHandler implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(SyskeRequestHandler.class);
    private Socket socket;
    private SyskeRequest syskeRequest;
    private SyskeResponse syskeResponse;

    public SyskeRequestHandler(Socket socket) throws IOException{
        this.socket = socket;
        init();
    }

    private void init() throws IOException{
        this.syskeRequest = new SyskeRequest(socket.getInputStream());
        this.syskeResponse = new SyskeResponse(socket.getOutputStream());
    }

    @Override
    public void run() {
        try {
           doDispatcher();
        } catch (Exception e) {
            logger.error("系统错误：", e);
        }
    }

    /**
     * 请求分发处理
     * @throws Exception
     */
    public void doDispatcher() throws Exception{
        logger.info("请求头信息：{}", syskeRequest.getHeader());
        logger.info("请求信息：{}", syskeRequest.getRequestAttributeMap());
        syskeResponse.write(String.format("hello syskeCat, dateTime:%d", System.currentTimeMillis()));
        socket.close();
    }
}

