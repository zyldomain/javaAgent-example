package com.javaagent.demo;

import javassist.ClassPool;
import javassist.CtClass;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class FirstAgent implements ClassFileTransformer {
    private String injectedClassName = "com.javaagent.demo.RealClass";
    private String fakeClassName = "com.javaagent.demo.FakeClass";
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace("/",".");

        if(className.equals(injectedClassName)){
            try{
                // CtClass ctClass = ClassPool.getDefault().get(className);
                //CtMethod ctMethod = ctClass.getDeclaredMethod("say");
                //ctMethod.insertBefore("System.out.println(1111);");
                // return ctClass.toBytecode();

                CtClass ctClass = ClassPool.getDefault().get(fakeClassName);
                ctClass.setName("com/javaagent/demo/RealClass");
                return ctClass.toBytecode();
            }catch (Exception e){

            }

        }
        return new byte[0];
    }
}
