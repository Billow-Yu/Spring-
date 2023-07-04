package com.jiatao.spring;

public class BeanDefinition {

    private Class type;
    private String scope;

    public void setType(Class type) {
        this.type = type;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Class getType() {
        return type;
    }

    public String getScope() {
        return scope;
    }
}
