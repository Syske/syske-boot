package io.github.syske.boot.handler;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;
import io.github.syske.boot.http.RequestMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.syske.boot.annotation.RequestParameter;
import io.github.syske.boot.exception.IllegalParameterException;
import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.Response;
import io.github.syske.boot.http.header.RequestHear;
import io.github.syske.boot.http.impl.SyskeRequest;
import io.github.syske.boot.http.impl.SyskeResponse;

/**
 * @program: syske-boot
 * @description: 请求处理器
 * @author: syske
 * @date: 2021-05-30 10:05
 */
public class SyskeRequestNioHandler implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(SyskeRequestNioHandler.class);
    private String input;
    private SocketChannel channel;
    private Request request;
    private Response response;
    private Map<String, Method> requestMappingMap;
    private Map<String, Object> contentMap;

    public SyskeRequestNioHandler(SocketChannel channel, String input) throws IOException{
        this.input = input;
        this.channel = channel;
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
        this.request = new SyskeRequest(input);
        this.response = new SyskeResponse(channel);
        this.requestMappingMap = SyskeBootContentScanHandler.getRequestMappingMap();
        this.contentMap = SyskeBootContentScanHandler.getContentMap();
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
        Object requestBody = request.getRequestBody();
        logger.info("请求头信息：{}", requestHear);
        logger.info("请求信息：{}", requestAttributeMap);
        logger.info("请求体：{}", requestBody);
        if (Objects.isNull(requestHear) || Objects.isNull(requestAttributeMap)) {
            return;
        }
        String requestMapping = requestHear.getRequestMapping();
        if (requestMappingMap.containsKey(requestMapping)) {
            Method method = requestMappingMap.get(requestMapping);
            logger.debug("method:{}", method);
            Object invoke = null;
            if (RequestMethod.GET.equals(requestHear.getRequestMethod())) {
                invoke = doGet(method, requestAttributeMap);
            } else if (RequestMethod.POST.equals(requestHear.getRequestMethod())) {
                invoke = doPost(method);
            }
            logger.info("invoke:{}", invoke);
            response.write(String.format("hello syskeCat, dateTime:%d\n result = %s", System.currentTimeMillis(), invoke));
        } else {
            response.write(404, String.format("resources not found :%d", System.currentTimeMillis()));
        }
    }

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

   private Object doRequest(Method method, Map<String, Object> requestParameterMap) throws IllegalAccessException, InstantiationException, InvocationTargetException {
       Class<?> declaringClass = method.getDeclaringClass();
       Annotation[][] parameterAnnotations = method.getParameterAnnotations();
       Object[] parameters = new Object[parameterAnnotations.length];
       for (int i = 0; i < parameterAnnotations.length; i++) {
           RequestParameter annotation = (RequestParameter)parameterAnnotations[i][0];
           parameters[i] = requestParameterMap.get(annotation.value());
       }
       Object o = contentMap.get(declaringClass.getName());
       Object invoke = method.invoke(o, parameters);
       return invoke;
    }
}

