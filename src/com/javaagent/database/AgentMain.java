package com.javaagent.database;

import com.javaagent.monitor.AgentLoader;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class AgentMain implements ClassFileTransformer {
    AgentLoader agentLoader = new AgentLoader();

    public static void premain(String agentArgs, Instrumentation inst){
        String remoteUrl = agentArgs;
        if(remoteUrl == null || remoteUrl.trim().equals("")){
            throw new RuntimeException("监听参数为空");
        }
        System.setProperty("bit_agent_url", remoteUrl);
        inst.addTransformer(new AgentMain());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

        if(className == null
                || loader == null
                || loader.getClass().getName().equals("sun.reflect.DelegatingClassLoader")
                || loader.getClass().getName().equals("org.apache.catalina.loader.StandardClassLoader")
                || className.indexOf("$Proxy") != -1){
            return null;
        }
        try {
            if(className.replaceAll("/",".").equals("com.mysql.jdbc.NonRegisteringDriver")){
                return JdbcMySqlCollects.instance.buildClassByte("com.mysql.jdbc.NonRegisteringDriver",loader);
            }
            return agentLoader.buildClass(className, loader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
