package io.github.syske.boot.http.impl;

import com.google.common.collect.Maps;
import io.github.syske.boot.exception.IllegalParameterException;
import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.RequestMethod;
import io.github.syske.boot.http.header.RequestHear;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

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
    private Map<String, String> requestAttributeMap;

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
    public Map<String, String> getRequestAttributeMap() throws IOException, IllegalParameterException{
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
        String[] headers = headerStr.split(" ", 3);
        header = new RequestHear(RequestMethod.match(headers[0]), headers[1]);
        Map<String, String> attributeMap = Maps.newHashMap();
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
