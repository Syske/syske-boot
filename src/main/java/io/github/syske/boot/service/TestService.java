package io.github.syske.boot.service;

import io.github.syske.boot.annotation.Service;
import io.github.syske.boot.annotation.Value;

/**
 * test 服务
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-02 7:58
 */
@Service
public class TestService {

    @Value("syske.boot.server.name")
    private String serverName;

    public String helloIoc(String name) {
        return "hello ioc, " + name;
    }

    public String helloValue() {
        return "hello, " + serverName;
    }
}
