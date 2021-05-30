package io.github.syske.boot.controller;

import io.github.syske.boot.annotation.Controller;
import io.github.syske.boot.annotation.RequestMapping;

/**
 * @program: syske-boot
 * @description:
 * @author: syske
 * @date: 2021-05-30 16:44
 */
@Controller
public class Test2Controller {

    @RequestMapping("/test2")
    public String test2() {
        return "test2";
    }
}
