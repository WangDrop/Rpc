package com.wc.rpc.client.proxy;

import com.wc.rpc.client.ConnectionManager;
import com.wc.rpc.client.RPCFuture;
import com.wc.rpc.client.RpcClient;
import com.wc.rpc.client.RpcClientHandler;
import com.wc.rpc.protocol.RpcRequest;
import com.wc.rpc.protocol.RpcResponse;
import org.omg.CORBA.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by 12083 on 2016/8/23.
 */
public class ObjectProxy <T> implements InvocationHandler, IAsyncObjectProxy{
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;

    public ObjectProxy(Class<T> clazz){
        this.clazz = clazz;
    }

    @Override
    public RPCFuture call(String funcName, Object... args) {
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(); //随意选取一个，使用RoundRobin
        RpcRequest request = createRequest(this.clazz.getName(), funcName, args);
        RPCFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setMethodName(method.getName());
        request.setClassName(method.getDeclaringClass().getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());
        //debug
        LOGGER.debug(method.getDeclaringClass().getName());
        LOGGER.debug(method.getName());
        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            LOGGER.debug(method.getParameterTypes()[i].getName());
        }
        for (int i = 0; i < args.length; ++i) {
            LOGGER.debug(args[i].toString());
        }
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler();

        RPCFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture.get(); //这个acquire实际上是一个阻塞操作，客户端如果没有拿到Rpc响应的话会一直阻塞在这里
    }

    private RpcRequest createRequest(String className, String methodName, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);

        Class[] parameterTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = getClassType(args[i]);
        }
        request.setParameterTypes(parameterTypes);

        LOGGER.debug(className);
        LOGGER.debug(methodName);
        for (int i = 0; i < parameterTypes.length; ++i) {
            LOGGER.debug(parameterTypes[i].getName());
        }
        for (int i = 0; i < args.length; ++i) {
            LOGGER.debug(args[i].toString());
        }

        return request;
    }

    private Class<?> getClassType(Object obj){
        Class<?> classType = obj.getClass();
        String typeName = classType.getName();
        switch (typeName){
            case "java.lang.Integer":
                return Integer.TYPE;
            case "java.lang.Long":
                return Long.TYPE;
            case "java.lang.Float":
                return Float.TYPE;
            case "java.lang.Double":
                return Double.TYPE;
            case "java.lang.Character":
                return Character.TYPE;
            case "java.lang.Boolean":
                return Boolean.TYPE;
            case "java.lang.Short":
                return Short.TYPE;
            case "java.lang.Byte":
                return Byte.TYPE;
        }

        return classType;
    }
}
