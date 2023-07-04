package com.jiatao.service;


import com.jiatao.spring.Autowired;
import com.jiatao.spring.BeanNameAware;
import com.jiatao.spring.Component;
import com.jiatao.spring.InitializingBean;

@Component("userService")
//@Scope("prototype")
public class UserService implements BeanNameAware, InitializingBean ,UserInterface{
    @Autowired
    private OrderService orderService;

    private String beanName;

    public void test(){
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName=beanName;
    }

    //用户自定义在Bean初始化时调的方法
    @Override
    public void afterPropertiesSet() {
        System.out.println("执行afterPropertiesSet方法");
    }
}
