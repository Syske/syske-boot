package io.github.syske.boot.http;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;

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
    SocketChannel getSocketChannel();

    /**
     * 写入响应结果
     * @param content
     * @throws IOException
     */
    void write(String content) throws IOException;

    /**
     * 写入响应结果
     * @param code
     * @param content
     * @throws IOException
     */
    void write(int code, String content) throws IOException;
}
