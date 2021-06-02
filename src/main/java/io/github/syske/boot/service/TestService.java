package io.github.syske.boot.service;

import io.github.syske.boot.annotation.Service;

/**
 * test 服务
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-02 7:58
 */
@Service
public class TestService {

    public void helloIoc(String name) {
        System.out.println("hello ioc, " + name);
    }
}
