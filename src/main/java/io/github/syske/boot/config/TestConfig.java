package io.github.syske.boot.config;

import io.github.syske.boot.annotation.Bean;
import io.github.syske.boot.annotation.Configuration;
import io.github.syske.boot.bean.TestBean;

/**
 * 配置注解测试
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-08 7:43
 */
@Configuration
public class TestConfig {
    @Bean
    public TestBean testBean() {
        TestBean testBean = new TestBean();
        testBean.setAge(18);
        testBean.setName("云中志");
        return testBean;
    }
}
