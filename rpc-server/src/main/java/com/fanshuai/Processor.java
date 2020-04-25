package com.fanshuai;

import com.fanshuai.domain.RpcRequest;
import com.fanshuai.domain.RpcResponse;
import com.fanshuai.exception.RpcException;
import org.apache.commons.collections.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Processor {
    private Map<String, Object> serviceMap = new HashMap<>();

    public void addServiceMap(List<Object> services) {
        if (CollectionUtils.isEmpty(services)) {
            return;
        }

        for (Object s : services) {
            addServiceMap(s);
        }
    }

    public void addServiceMap(Object service) {
        serviceMap.put(service.getClass().getName(), service);
    }

    public RpcResponse processRequest(RpcRequest rpcRequest) {
        RpcResponse response = new RpcResponse();
        Throwable t = null;

        if (null == rpcRequest) {
            t = new RpcException("rpc request null");
            response.setThrowable(t);
            return response;
        }

        response.setRequestId(rpcRequest.getRequestId());
        Object service = serviceMap.get(rpcRequest.getClassName());
        if (null == service) {
            t = new RpcException(String.format("cannot find rpc service [%s]", rpcRequest.getClassName()));
            response.setThrowable(t);
            return response;
        }

        Class cls = service.getClass();

        try {
            Method m = cls.getMethod(rpcRequest.getMethodName(), rpcRequest.getArgTypes());
            if (null == m) {
                t = new RpcException(String.format("cannot find method [%s,%s]", rpcRequest.getClassName(), rpcRequest.getMethodName()));
                response.setThrowable(t);
                return response;
            }

            m.setAccessible(true);

            Object result = m.invoke(service, rpcRequest.getArgs());
            response.setValue(result);
            return response;

        } catch (NoSuchMethodException e) {
            t = new RpcException(String.format("cannot find method [%s,%s]", rpcRequest.getClassName(), rpcRequest.getMethodName()));
            response.setThrowable(t);
            return response;
        } catch (InvocationTargetException e) {
            t = e;
        } catch (IllegalAccessException e) {
            t = e;
        } catch (Throwable throwable) {
            t = throwable;
        }

        response.setRequestId(rpcRequest.getRequestId());
        return response;
    }
}
