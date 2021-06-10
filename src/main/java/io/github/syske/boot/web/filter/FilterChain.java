package io.github.syske.boot.web.filter;

import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.Response;

import java.io.IOException;

/**
 * @author sysker
 * @version 1.0
 * @date 2021-06-10 8:17
 */
public interface FilterChain {
    void doFilter(Request request, Response response, FilterChain filterChain) throws IOException;
}
