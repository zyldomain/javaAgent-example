package com.javaagent.demo;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class LubanAgent {
    public static void main(String[] args){

    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                if(className != null && className.equals("com/javaagent/service/TestService")){
                    ClassPool pool = new ClassPool();
                    pool.insertClassPath(new LoaderClassPath(loader));
                    try {
                        CtClass target = pool.get("com.javaagent.service.TestService");
                       // CtMethod mainMethod = target.getMethod("sayHello");

                        return target.toBytecode();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return new byte[0];
            }
        });
    }
}
