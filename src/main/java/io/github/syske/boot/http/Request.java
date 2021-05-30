package io.github.syske.boot.http;

import java.io.InputStream;
import java.io.OutputStream;
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
    Map<String, String> getRequestAttributeMap() throws Exception;
}