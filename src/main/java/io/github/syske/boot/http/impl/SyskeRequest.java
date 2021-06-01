package io.github.syske.boot.http.impl;

import com.google.common.collect.Maps;
import io.github.syske.boot.exception.IllegalParameterException;
import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.RequestMethod;
import io.github.syske.boot.http.header.RequestHear;
import io.github.syske.boot.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

/**
 * @program: example-2021.05.28
 * @description: 请求
 * @author: syske
 * @date: 2021-05-29 15:48
 */
public class SyskeRequest implements Request {
    /**
     * 输入流
     */
    private InputStream inputStream;
    /**
     * 请求参数
     */
    private Map<String, Object> requestAttributeMap;

    private RequestHear header;

    public SyskeRequest(InputStream inputStream) throws IOException, IllegalParameterException{
        this.inputStream = inputStream;
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
    public Map<String, Object> getRequestAttributeMap() throws IOException, IllegalParameterException{
        if (requestAttributeMap != null) {
            return requestAttributeMap;
        }
        initRequest();
        return requestAttributeMap;
    }

    /**
     * 初始化请求
     *
     * @throws IOException
     */
    private void initRequest() throws IOException, IllegalParameterException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String headerStr = bufferedReader.readLine();
        if (Objects.isNull(headerStr)) {
            return;
        }
        String[] headers = headerStr.split(" ", 3);
        String requestMapping = headers[1];
        Map<String, Object> attributeMap = Maps.newHashMap();
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
        String readLine = null;
        while ((readLine = bufferedReader.readLine()) != null) {
            if(readLine.length()==0) {
                break;
            }
            if (readLine.contains(":")) {
                String[] split = readLine.split(":", 2);
                attributeMap.put(split[0], split[1]);
            }
        }
        requestAttributeMap = attributeMap;
    }

}
