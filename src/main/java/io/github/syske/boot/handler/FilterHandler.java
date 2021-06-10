package io.github.syske.boot.handler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.syske.boot.annotation.WebFilter;
import io.github.syske.boot.web.filter.Filter;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

/**
 * 过滤器处理
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-10 8:35
 */
public class FilterHandler {
    private static Map<Filter, String[]> filterUrlPatternsMap = Maps.newHashMap();

    private static LinkedList<Filter> filterLinkedList = Lists.newLinkedList();

    public static Map<Filter, String[]> getFilterUrlPatternsMap() {
        return filterUrlPatternsMap;
    }

    public static LinkedList<Filter> getFilterLinkedList() {
        return filterLinkedList;
    }

    public static void init(Class zClass) throws IllegalAccessException, InstantiationException {
        Annotation webFilter = zClass.getAnnotation(WebFilter.class);
        if (Objects.nonNull(webFilter)) {
            String[] urlPatterns = ((WebFilter) webFilter).urlPatterns();
            Filter filter = (Filter)zClass.newInstance();
            filterUrlPatternsMap.put(filter, urlPatterns);
            filterLinkedList.add(filter);
        }
    }
}
