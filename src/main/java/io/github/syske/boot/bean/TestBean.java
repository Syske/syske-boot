package io.github.syske.boot.bean;

/**
 * test bean
 *
 * @author sysker
 * @version 1.0
 * @date 2021-06-08 7:44
 */
public class TestBean {
    private String name;
    private int age;

    public TestBean() {
    }

    public TestBean(String name) {
        System.out.println("create bean, " + name);
    }
    public void testBean() {
        System.out.println(this);
        System.out.println("hello bean");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "TestBean{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
