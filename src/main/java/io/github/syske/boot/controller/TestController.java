package io.github.syske.boot.controller;

import io.github.syske.boot.annotation.Controller;
import io.github.syske.boot.annotation.RequestMapping;
import io.github.syske.boot.annotation.RequestParameter;

/**
 * @program: syske-boot
 * @description: controller test
 * @author: syske
 * @date: 2021-05-30 15:37
 */
@Controller("test")
public class TestController {

    @RequestMapping("/test")
    public String testRequstMapping() {
        return "hello syske-boot";
    }

    @RequestMapping("/sayHello")
    public String test(@RequestParameter("name") String name) {
        return "hello," + name;
    }
}
