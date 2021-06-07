package io.github.syske.boot.http.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import io.github.syske.boot.exception.IllegalParameterException;
import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.RequestMethod;
import io.github.syske.boot.http.header.RequestHear;
import io.github.syske.boot.util.StringUtil;

/**
 * @program: example-2021.05.28
 * @description: 请求
 * @author: syske
 * @date: 2021-05-29 15:48
 */
public class SyskeRequest implements Request {
    private final Logger logger = LoggerFactory.getLogger(SyskeRequest.class);
    /**
     * 输入流
     */
    private InputStream inputStream;

    private String input;
    /**
     * 请求参数
     */
    private Map<String, Object> requestAttributeMap;
    private Object requestBody;

    private RequestHear header;

    public SyskeRequest(InputStream inputStream) throws IOException, IllegalParameterException{
        this.inputStream = inputStream;
        initRequest();
    }

    public SyskeRequest(String input) throws IllegalParameterException{
        this.input = input;
        initRequest();
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public RequestHear getRequestHear() {
        return header;
    }

    @Override
    public Object getRequestBody() {
        return requestBody;
    }

    @Override
    public Map<String, Object> getRequestAttributeMap() throws IllegalParameterException{
        if (requestAttributeMap != null) {
            return requestAttributeMap;
        }
        initRequest();
        return requestAttributeMap;
    }

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

}
