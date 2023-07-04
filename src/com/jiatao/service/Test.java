package com.jiatao.service;

import com.jiatao.spring.JiaTaoApplicationContext;
public class Test {
    public static void main(String[] args) {

        JiaTaoApplicationContext applicationContext = new JiaTaoApplicationContext(AppConfig.class);

        UserInterface userService = (UserInterface) applicationContext.getBean("userService");
        userService.test();
    }
}
