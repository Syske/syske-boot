package io.github.syske.boot.http.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.github.syske.boot.http.Response;

/**
 * @program: example-2021.05.28
 * @description: 响应
 * @author: syske
 * @date: 2021-05-29 15:47
 */
public class SyskeResponse implements Response {
    private static Charset charset = StandardCharsets.UTF_8;
    private SocketChannel channel;

    public SyskeResponse(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public SocketChannel getSocketChannel() {
        return channel;
    }

    /**
     * 写入响应信息
     * @param content
     * @throws IOException
     */
    @Override
    public void write(String content) throws IOException {
        this.write(200, content);
    }

    /**
     * 写入响应信息
     * @param code
     * @param content
     * @throws IOException
     */
    @Override
    public void write(int code, String content) throws IOException {
        StringBuffer httpResponse = new StringBuffer();
        // 按照HTTP响应报文的格式写入
        httpResponse.append("HTTP/1.1 ").append(code).append(" OK\n").append("Content-Type:text/html\n").append("\r\n")
                .append("<html><head><link rel=\"icon\" href=\"data:;base64,=\"></head><body>").append(content)
                .append("</body></html>");
        // 将文本转为字节流
        channel.write(charset.encode(httpResponse.toString()));
        channel.close();
    }
}
