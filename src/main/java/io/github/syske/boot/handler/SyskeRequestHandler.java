package io.github.syske.boot.handler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;

import io.github.syske.boot.annotation.RequestParameter;
import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.Response;
import io.github.syske.boot.http.header.RequestHear;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.syske.boot.exception.IllegalParameterException;
import io.github.syske.boot.http.impl.SyskeRequest;
import io.github.syske.boot.http.impl.SyskeResponse;

/**
 * @program: syske-boot
 * @description: 请求处理器
 * @author: syske
 * @date: 2021-05-30 10:05
 */
public class SyskeRequestHandler implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(SyskeRequestHandler.class);
    private Socket socket;
    private Request request;
    private Response response;
    private Map<String, Method> requestMappingMap;

    public SyskeRequestHandler(Socket socket) throws IOException{
        this.socket = socket;
        try {
            init();
        } catch (IllegalParameterException e) {
            logger.error("非法请求参数:", e);
            response.write(500, "非法请求参数");
        } catch (IOException e) {
            logger.error("请求信息解析错误:", e);
            response.write(500, "请求信息解析错误");
        }
    }

    private void init() throws IOException, IllegalParameterException {
        this.request = new SyskeRequest(socket.getInputStream());
        this.response = new SyskeResponse(socket.getOutputStream());
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
}

