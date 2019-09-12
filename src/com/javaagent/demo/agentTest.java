package com.javaagent.demo;

public class agentTest {
    public static void main(String[] args){
        long begin = System.currentTimeMillis();
        main$agent(args);
        long end = System.currentTimeMillis();
        System.out.println(end - begin);
    }

    public static void main$agent(String[] args){
        System.out.println("==========main方法执行========");
    }
}

