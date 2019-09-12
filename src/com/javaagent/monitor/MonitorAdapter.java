package com.javaagent.monitor;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MonitorAdapter {
    static ExecutorService threadService = Executors.newCachedThreadPool();

    public static Statistics begin(String name, String method) {
        Statistics s = new Statistics();
        s.className = name;
        s.methodName = method;
        s.begin = System.currentTimeMillis();
        s.createTime = System.currentTimeMillis();
        return s;
    }

    public static void error(Statistics stat, Throwable throwable) {
        stat.errorMsg = throwable.getMessage();
        stat.errorType = throwable.getClass().getName();
        if(throwable instanceof InvocationTargetException) {
            stat.errorType = ((InvocationTargetException) throwable).getTargetException().getClass().getName();
            stat.errorMsg = ((InvocationTargetException) throwable).getTargetException().getMessage();
        }
    }

    public static void end(Statistics stat){
        stat.end = System.currentTimeMillis();
        stat.userTime = stat.end - stat.begin;
        stat.classSimpleName = stat.className.substring(stat.className.lastIndexOf(".") + 1);
        stat.serviceName = stat.classSimpleName + "." + stat.methodName;
        sendStatics(stat);
        System.out.println("代理结束:" + stat.toString());
    }

    public static void sendStatics(Statistics stat){

    }

    static class Statistics {
        public long userTime;
        public long begin;
        public long end;
        public long createTime;
        public String classSimpleName;
        public String  className;
        public String methodName;
        public String serviceName;
        public String errorMsg;
        public String errorType;
    }
}
