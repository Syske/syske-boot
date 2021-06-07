package io.github.syske.boot.http;

import io.github.syske.boot.http.header.RequestHear;

import java.io.InputStream;
import java.util.Map;

/**
 * @program: syske-boot
 * @description: 请求头
 * @author: syske
 * @date: 2021-05-30 10:11
 */
public interface Request {
    /**
     * 获取输入流
     * @return
     */
    InputStream getInputStream();

    /**
     * 获取请求参数
     * @return
     */
    Map<String, Object> getRequestAttributeMap() throws Exception;

    /**
     * 获取请求头
     * @return
     */
    RequestHear getRequestHear();

    /**
     * 获取请求体
     * @return
     */
    Object getRequestBody();
}
