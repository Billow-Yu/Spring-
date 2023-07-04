package com.jiatao.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class JiaTaoApplicationContext {
    private Class configClass;

    //BeanDefinition池
    private ConcurrentHashMap<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    //Bean单例池
    private ConcurrentHashMap<String,Object> singletonObjects=new ConcurrentHashMap<>();

    private ArrayList<BeanPostProcessor> beanPostProcessorList=new ArrayList<>();
    public JiaTaoApplicationContext(Class configClass) {
        this.configClass = configClass;
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value();
            path=path.replace(".","/");
            ClassLoader classLoader = JiaTaoApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            System.out.println(resource);
            File file = new File(resource.getFile());
            System.out.println(file);
            if(file.isDirectory()){
                File[] files = file.listFiles();
                for(File f:files){
                    String fileName = f.getAbsolutePath();

                    if(fileName.endsWith(".class")){
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        className = className.replace("\\",".");

                        try {
                            Class<?> clazz = classLoader.loadClass(className);


                            if (clazz.isAnnotationPresent(Component.class)) {

                                if(BeanPostProcessor.class.isAssignableFrom(clazz)){
                                    BeanPostProcessor instance = (BeanPostProcessor)clazz.newInstance();
                                    beanPostProcessorList.add(instance);
                                }
                                Component componentAnnotation = (Component) clazz.getAnnotation(Component.class);
                                String beanName = componentAnnotation.value();

                                if(beanName.equals("")){
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                if(clazz.isAnnotationPresent(Scope.class)){
                                    Scope scopeAnnotation = (Scope) clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                }else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(beanName,beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

        }

        //
        for (String beanName : beanDefinitionMap.keySet()){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = creatBean(beanName, beanDefinition);
                singletonObjects.put(beanName,bean);
            }
        }


    }

    //创建Bean实例
    private Object creatBean(String beanName,BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getType();
        try {
            Object instance = clazz.getConstructor().newInstance();
            //依赖注入
            for(Field f:clazz.getDeclaredFields()){
                if(f.isAnnotationPresent(Autowired.class)){
                    f.setAccessible(true);
                    f.set(instance,getBean(f.getName()));
                }
            }

            //Aware回调
            if(instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }
            //用户自定义初始化前Bean操作
            for(BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                instance=beanPostProcessor.postProcessBeforeInitialization(beanName,instance);
            }
            //初始化
            if(instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }
            //用户自定义初始化后Bean 操作
            for(BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                instance = beanPostProcessor.postProcessAfterInitialization(beanName,instance);
            }

            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }


    }
    public Object getBean(String beanName){
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinition==null){
            throw new NullPointerException();
        }else {
            String scope = beanDefinition.getScope();
            if(scope.equals("singleton")){
                Object bean = singletonObjects.get(beanName);
                if(bean==null){
                    Object newBean = creatBean(beanName, beanDefinition);
                    singletonObjects.put(beanName,newBean);
                    return newBean;
                }
                return bean;
            }else{
                //多例
                return creatBean(beanName,beanDefinition);
            }
        }
    }
}
