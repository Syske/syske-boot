package io.github.syske.boot.web.filter;

import io.github.syske.boot.annotation.WebFilter;
import io.github.syske.boot.http.Request;
import io.github.syske.boot.http.Response;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

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
        if (Objects.nonNull(filterChain)) {
            filterChain.doFilter(request, response);
        }
    }

    private static class VirtualFilterChain implements FilterChain {
        private final FilterChain originalChain;
        private final List<? extends Filter> additionalFilters;
        private int currentPosition = 0;

        public VirtualFilterChain(FilterChain chain, List<? extends Filter> additionalFilters) {
            this.originalChain = chain;
            this.additionalFilters = additionalFilters;
        }

        @Override
        public void doFilter(Request request, Response response) throws IOException {
            if (this.currentPosition == this.additionalFilters.size()) {
                this.originalChain.doFilter(request, response);
            } else {
                ++this.currentPosition;
                Filter nextFilter = this.additionalFilters.get(this.currentPosition - 1);
                nextFilter.doFilter(request, response, this);
            }

        }
    }
}
