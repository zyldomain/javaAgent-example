package com.javaagent.monitor;

import javassist.*;

public class AgentLoader {
    public final  static String source = "{\n"
            + "         com.javaagent.monitor.MonitorAdapter.Statistics monitor = com.javaagent.monitor.MonitorAdapter.begin(\"%s\", \"%s\");\n"
            + "         Object result;\n"
            + "         try{\n"
            + "             result = ($w) %s$agent($$);\n"
            + "         } catch (Throwable e) {\n"
            + "             com.javaagent.monitor.MonitorAdapter.error(monitor,e);\n"
            + "             throw e;\n"
            + "         } finally{\n"
            + "             com.javaagent.monitor.MonitorAdapter.end(monitor);\n"
            + "         }\n"
            + "         return ($r) result;\n"
            + "}\n";


    public final  static String voidSource = "{\n"
            + "         com.javaagent.monitor.MonitorAdapter.Statistics monitor = com.javaagent.monitor.MonitorAdapter.begin(\"%s\", \"%s\");\n"
            + "         Object result;\n"
            + "         try{\n"
            + "            %s$agent($$);\n"
            + "         } catch (Throwable e) {\n"
            + "             com.javaagent.monitor.MonitorAdapter.error(monitor,e);\n"
            + "             throw e;\n"
            + "         } finally{\n"
            + "             com.javaagent.monitor.MonitorAdapter.end(monitor);\n"
            + "         }\n"
            + "}\n";

    public byte[] buildClass(String className, ClassLoader loader)throws NotFoundException, Exception {
        ClassPool classPool = new ClassPool(false);
        classPool.insertClassPath(new LoaderClassPath(loader));

        CtClass ctClass = classPool.get(className.replaceAll("/","."));
        if(!isTarget(ctClass)){
            return null;
        }

        CtMethod[] methods = ctClass.getDeclaredMethods();

        for(CtMethod m : methods){
            if(!Modifier.isPublic(m.getModifiers())){
                continue;
            }

            if(Modifier.isStatic(m.getModifiers())){
                continue;
            }

            if(Modifier.isNative(m.getModifiers())){
                continue;
            }

            /**
             * 插入监听method
             */

            CtMethod ctMethod = m;
            String methodName = m.getName();
            String oldMethodName = methodName + "$agent";
            //重构代码
            ctMethod.setName(oldMethodName);
            CtMethod agentMethod = CtNewMethod.copy(ctMethod, methodName, ctClass,null);
            agentMethod.setBody(buildBody(className,methodName,ctMethod.getReturnType().getName()));
            ctClass.addMethod(agentMethod);
        }
        return ctClass.toBytecode();
    }

    public static String buildBody(String className, String methodName, String returnType){
        String result = String.format(returnType.equals("void") ? voidSource : source, className, methodName, methodName);
        return result;
    }
    private boolean isTarget(CtClass ctClass) throws  ClassNotFoundException {
        for(Object obj : ctClass.getAnnotations()) {
            if(obj.toString().equals("@org.springframework.stereotype.Service")){
                return true;
            }
        }
        return false;
    }
}
