package com.fanshuai;

import com.fanshuai.annotation.RpcMethod;
import com.fanshuai.domain.RpcRequest;
import com.fanshuai.domain.RpcResponse;
import com.fanshuai.io.ManagedChannel;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import util.JSONUtil;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class RpcStub {
    private ManagedChannel managedChannel;

    public RpcStub(ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    public <T> T generateProxyService(Class<T> cls) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cls);
        enhancer.setCallback(new RpcMethonInterceptor());

        return (T) enhancer.create();
    }

    private class RpcMethonInterceptor implements MethodInterceptor {
        @Override
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            if (method.getAnnotation(RpcMethod.class) == null) {
                return null;
            }

            RpcRequest request = new RpcRequest();
            request.setRequestId(UUID.randomUUID().toString());

            request.setClassName(o.getClass().getName());
            request.setMethodName(method.getName());
            request.setArgs(args);
            request.setArgTypes(method.getParameterTypes());

            RpcResponse response = managedChannel.getResponse(request);
            if (response.getThrowable() != null) {
                throw response.getThrowable();
            } else {
                Object result = response.getValue();

                Class cls = method.getReturnType();
                if (isBeanClass(cls)) {
                    String json = JSONUtil.toJson(result);
                    return JSONUtil.fromJson(json, cls);
                } else {
                    return result;
                }
            }
        }
    }

    private boolean isBeanClass(Class c) {
        return Object.class.isAssignableFrom(c) &&
                !Map.class.isAssignableFrom(c) &&
                !Collection.class.isAssignableFrom(c);
    }
}
