package io.github.syske.boot.http;

/**
 * @program: syske-boot
 * @description: 请求方法
 * @author: syske
 * @date: 2021-05-30 14:39
 */
public enum RequestMethod {
    /**
     * get请求
     */
    GET("get","get请求"),
    /**
     * post请求
     */
    POST("post", "post请求"),
    /**
     * put请求
     */
    PUT("put", "put请求"),
    NULL("null", "illegal parameter");

    private String method;
    private String description;
    RequestMethod(String method, String description){
        this.method = method;
        this.description = description;
    }

    public String getMethod() {
        return method;
    }

    public String getDescription() {
        return description;
    }

    private static final RequestMethod[] values = RequestMethod.values();

    /**
     * 匹配请求方法
     * @param methodName
     * @return
     */
    public static RequestMethod match(String methodName) {
        for (RequestMethod value : values) {
            if (value.getMethod().equalsIgnoreCase(methodName)) {
                return value;
            }
        }
        return RequestMethod.NULL;
    }
}
