package io.github.syske.boot.controller;

import io.github.syske.boot.annotation.Autowired;
import io.github.syske.boot.annotation.Controller;
import io.github.syske.boot.annotation.RequestMapping;
import io.github.syske.boot.annotation.RequestParameter;
import io.github.syske.boot.bean.TestBean;
import io.github.syske.boot.service.TestService;

/**
 * @program: syske-boot
 * @description:
 * @author: syske
 * @date: 2021-05-30 16:44
 */
@Controller
public class Test2Controller {

    @Autowired
    private TestService service;

    @Autowired
    private TestBean testBean;

    @RequestMapping("/test2")
    public String test2() {
        return "test2";
    }

    @RequestMapping("/testAutowire")
    public String testAutowire(@RequestParameter("name") String name){
        testBean.testBean();
        return service.helloIoc(name);
    }

    @RequestMapping("/testValue")
    public String testValue(){
        String helloValue = service.helloValue();
        return helloValue;
    }
}
