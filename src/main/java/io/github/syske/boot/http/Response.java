package io.github.syske.boot.http;

import java.io.OutputStream;

/**
 * @program: syske-boot
 * @description: 响应头
 * @author: syske
 * @date: 2021-05-30 10:11
 */
public interface Response {
    /**
     * 获取输出流
     * @return
     */
    OutputStream getOutputStream();
}
