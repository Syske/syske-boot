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
    /**
     * filter调用链
     * @param request
     * @param response
     * @throws IOException
     */
    void doFilter(Request request, Response response) throws IOException;
}
