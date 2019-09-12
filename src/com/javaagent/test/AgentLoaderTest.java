package com.javaagent.test;

import com.javaagent.monitor.AgentLoader;
import com.javaagent.service.UserService;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class AgentLoaderTest {
    @Test
    public void buildClassTest(){
        AgentLoader loader = new AgentLoader();
        try{
            final String className = "com.javaagent.service.UserServiceImpl";
            final byte[] claByte = loader.buildClass(className, getClass().getClassLoader());
            ClassLoader c = new ClassLoader(getClass().getClassLoader()) {
                @Override
                public Class<?> loadClass(String name) throws ClassNotFoundException {
                    if(name.equals(className)){
                        return defineClass(name,claByte,0,claByte.length);
                    }
                    return super.loadClass(name);
                }
            };
            String pahname = System.getProperty("user.dir") + "/target/UserServiceImpl.class";
            Path path = new File(pahname).toPath();
            Files.write(path, claByte);
            //获取重新编译后的class
            Class<?> newCla = c.loadClass(className);
            UserService ins = (UserService) newCla.newInstance();

        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            Thread.currentThread().sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
