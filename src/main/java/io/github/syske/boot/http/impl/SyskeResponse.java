package io.github.syske.boot.http.impl;

import java.io.IOException;
import java.io.OutputStream;

import io.github.syske.boot.http.Response;

/**
 * @program: example-2021.05.28
 * @description: 响应
 * @author: syske
 * @date: 2021-05-29 15:47
 */
public class SyskeResponse implements Response {
    private OutputStream outputStream;

    public SyskeResponse(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    //将文本转换为字节流
    public void write(String content) throws IOException {
        StringBuffer httpResponse = new StringBuffer();
        // 按照HTTP响应报文的格式写入
        httpResponse.append("HTTP/1.1 200 OK\n").append("Content-Type:text/html\n").append("\r\n")
            .append("<html><head><link rel=\"icon\" href=\"data:;base64,=\"></head><body>").append(content)
            .append("</body></html>");
        // 将文本转为字节流
        outputStream.write(httpResponse.toString().getBytes());
        outputStream.flush();
        outputStream.close();
    }
}