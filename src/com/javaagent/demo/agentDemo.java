package com.javaagent.demo;

import java.lang.instrument.Instrumentation;

public class agentDemo {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("=========premain方法执行1========");
        System.out.println(agentArgs);
        inst.addTransformer(new FirstAgent());
    }
    public static void premain(String agentArgs) {
        System.out.println("=========premain方法执行2========");
        System.out.println(agentArgs);
    }

}
