package io.github.syske.boot.http.header;

import io.github.syske.boot.exception.IllegalParameterException;
import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.RequestMethod;

/**
 * @program: syske-boot
 * @description:
 * @author: syske
 * @date: 2021-05-30 14:28
 */
public class RequestHear {
    /**
     * 请求地址
     */
    private String requestMapping;
    /**
     * 请求方法
     */
    private RequestMethod requestMethod;

    public RequestHear(RequestMethod requestMethod, String requestMapping) throws IllegalParameterException{
        if (RequestMethod.NULL.equals(requestMethod)) {
            throw new IllegalParameterException(requestMethod.getDescription());
        }
        this.requestMapping = requestMapping;
        this.requestMethod = requestMethod;
    }

    public String getRequestMapping() {
        return requestMapping;
    }

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }

    @Override
    public String toString() {
        return "RequestHear{" +
                "requestMapping='" + requestMapping + '\'' +
                ", requestMethod=" + requestMethod +
                '}';
    }
}
