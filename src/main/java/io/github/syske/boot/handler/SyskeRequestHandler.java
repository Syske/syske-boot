package io.github.syske.boot.handler;

import com.google.common.collect.Maps;
import io.github.syske.boot.exception.IllegalParameterException;
import io.github.syske.boot.http.impl.SyskeRequest;
import io.github.syske.boot.http.impl.SyskeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
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
    private Map<String, Method> requestMappingMap;

    public SyskeRequestHandler(Socket socket) throws IOException{
        this.socket = socket;
        try {
            init();
        } catch (IllegalParameterException e) {
            logger.error("非法请求参数:", e);
            syskeResponse.write(500, "非法请求参数");
        } catch (IOException e) {
            logger.error("请求信息解析错误:", e);
            syskeResponse.write(500, "请求信息解析错误");
        }
    }

    private void init() throws IOException, IllegalParameterException {
        this.syskeRequest = new SyskeRequest(socket.getInputStream());
        this.syskeResponse = new SyskeResponse(socket.getOutputStream());
        this.requestMappingMap = SyskeBootContentScanHandler.getRequestMappingMap();
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
        logger.info("请求头信息：{}", syskeRequest.getRequestHear());
        logger.info("请求信息：{}", syskeRequest.getRequestAttributeMap());
        String requestMapping = syskeRequest.getRequestHear().getRequestMapping();
        if (requestMappingMap.containsKey(requestMapping)) {
            Method method = requestMappingMap.get(requestMapping);
            logger.debug("method:{}", method);
            syskeResponse.write(String.format("hello syskeCat, dateTime:%d", System.currentTimeMillis()));
        } else {
            syskeResponse.write(404, String.format("resources not found :%d", System.currentTimeMillis()));
        }
        socket.close();
    }
}

