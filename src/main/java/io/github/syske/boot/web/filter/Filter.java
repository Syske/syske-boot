package io.github.syske.boot.web.filter;

import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.Response;

import java.io.IOException;

/**
 * 过滤器接口
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-10 8:15
 */
public interface Filter {
    default void init() {}

    void doFilter(Request request, Response response, FilterChain filterChain) throws IOException;

    default void destrory() {}
}
