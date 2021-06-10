package io.github.syske.boot.web.filter;

import io.github.syske.boot.annotation.WebFilter;
import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.Response;

import java.io.IOException;

/**
 * filter测试
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-10 8:27
 */
@WebFilter(urlPatterns = {"/*"}, description = "test filter")
public class TestFilter implements Filter{
    @Override
    public void doFilter(Request request, Response response, FilterChain filterChain) throws IOException {
        System.out.println(String.format("过滤器TestFilter被访问，拦截地址：%s", request.getRequestHear().getRequestMapping()));
    }
}
