package com.javaagent.database;

import javassist.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class JdbcMySqlCollects {
    public static final  JdbcMySqlCollects instance = new JdbcMySqlCollects();
    private final static String[] connection_agent_methods = new String[]{"prepareStatement"};
    private final static String[] prepared_statement_methods = new String[]{"execute", "executeUpdate","executeQuery"};

    public CtClass buildClass(String className, ClassLoader loader) throws Exception {
        if(!className.equals("com.mysql.jdbc.NonRegisteringDriver")){
            throw new RuntimeException("fail param");
        }
        ClassPool pool = new ClassPool();
        pool.insertClassPath(new LoaderClassPath(loader));
        CtClass ctClass = pool.get(className);
        CtMethod connectMethod = ctClass.getMethod("connect","(Ljava/lang/String;Ljava/util/Properties;)Ljava/sql/Connection;");
        CtMethod agentMethod = CtNewMethod.copy(connectMethod, ctClass, null);
        agentMethod.setName(agentMethod.getName() + "$agent");
        ctClass.addMethod(agentMethod);
        connectMethod.setBody(source);
        return ctClass;
    }
    //构建Class字节
    public byte[] buildClassByte(String className, ClassLoader loader) throws  Exception {
        return buildClass(className,loader).toBytecode();
    }

    //生成一个动态代理的连接
    public Connection proxyConnection(final  Connection connection){
        Object c = Proxy.newProxyInstance(JdbcMySqlCollects.class.getClassLoader(), new Class[]{Connection.class},new ConnectionHandler(connection));
        return (Connection) c;
    }

    public Object proxyPreparedStatement(PreparedStatement ps, JdbcStatics jdbcStat){
        Object c = Proxy.newProxyInstance(JdbcMySqlCollects.class.getClassLoader(), new Class[]{PreparedStatement.class},new PreparedStatementHandler(ps, jdbcStat));
        return (PreparedStatement) c;
    }

    public JdbcStatics begin(){
        JdbcStatics jdbcStat = new JdbcStatics();
        jdbcStat.beginTime = System.currentTimeMillis();
        return jdbcStat;
    }

    public void error(JdbcStatics jdbcStat, Throwable e){
        jdbcStat.error = e.getMessage();
        jdbcStat.type = e.getClass().getName();
    }

    public void end(JdbcStatics jdbcStat){
        jdbcStat.endTime = System.currentTimeMillis();
        jdbcStat.userTime = jdbcStat.endTime = jdbcStat.beginTime;
    }



    private class PreparedStatementHandler implements InvocationHandler{

        private final PreparedStatement ps;
        private final JdbcStatics jdbcStat;
        public PreparedStatementHandler(PreparedStatement ps, JdbcStatics jdbcStat){
            this.ps = ps;
            this.jdbcStat = jdbcStat;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean isTargetMethod = false;
            for(String agentm : prepared_statement_methods){
                if(agentm.equals(method.getName())){
                    isTargetMethod = true;
                }
            }
            Object result = null;
            try {
                result = method.invoke(ps,args);
            }catch (Exception e){
                if(isTargetMethod){
                    JdbcMySqlCollects.this.error(jdbcStat, e);
                }
                throw  e;
            }finally {
                if(isTargetMethod){
                    JdbcMySqlCollects.this.end(jdbcStat);
                }
            }
            return result;
        }
    }

    private class ConnectionHandler implements InvocationHandler{
        private final Connection connection;
        private ConnectionHandler(Connection connection){
            this.connection = connection;
        }
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            boolean isTargetMethod = false;
            for(String agentm : connection_agent_methods){
                if(agentm.equals(method.getName())){
                    isTargetMethod = true;
                }
            }
            Object result = null;
            JdbcStatics jdbcStat = null;
            try {
                if(isTargetMethod) {
                    jdbcStat = (JdbcStatics) JdbcMySqlCollects.this.begin();
                    jdbcStat.jdbcUrl = connection.getMetaData().getURL();
                    jdbcStat.sql = (String)args[0];
                }
                result = method.invoke(connection,args);
                if(isTargetMethod && result instanceof PreparedStatement) {
                    PreparedStatement ps = (PreparedStatement) result;
                    result = proxyPreparedStatement(ps, jdbcStat);
                }
            }catch (Exception e){
                JdbcMySqlCollects.this.error(jdbcStat, e);
                JdbcMySqlCollects.this.end(jdbcStat);
                throw  e;
            }
            return result;
        }
    }

    static class JdbcStatics {
        public String type;
        public String error;
        public String sql;
        public String jdbcUrl;
        public long beginTime;
        public long endTime;
        public long userTime;
    }

    final static  String source = "{\n"
            + "     java.sql.Connection result = null;\n"
            + "     try {\n"
            + "         result = ($w)connect$agent($$);\n"
            + "         result=com.javaagent.database.JdbcMySqlCollects.instance.proxyConnection(result);\n"
            + "         } catch (Throwable e) {\n"
            + "             throw e;\n"
            + "         } finally {\n"
            + "         }\n"
            + "     return ($r) result;\n"
            + " }\n";
}
