package io.github.syske.boot.util;

/**
 * string util
 *
 * @author syske
 * @version 1.0
 * @date 2021-06-01 上午7:27
 */
public class StringUtil {
    /**
     * 判断字符串是否为空
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }

    /**
     * 判断字符串是否为空
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 传入字符串头尾trim
     * @param  str 字符串
     * @return String
     */
    public static String trim(String str) {
        if (isEmpty(str)) {
            return "";
        }
        return str.trim();
    }
}
