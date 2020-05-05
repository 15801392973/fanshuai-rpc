package com.fanshuai;

import com.fanshuai.domain.RpcRequest;
import com.fanshuai.domain.RpcResponse;
import com.fanshuai.io.RpcChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//存储返回值的容易
public class RpcResponseValueContainer {
    private static RpcResponseValueContainer container = new RpcResponseValueContainer();

    private RpcResponseValueContainer() {
    }

    //单例
    public static RpcResponseValueContainer getInstance() {
        return container;
    }

    //object锁
    private Map<String, Object> lockMap = new ConcurrentHashMap<>();

    //rpc调用结果
    private Map<String, RpcResponse> responseMap = new ConcurrentHashMap<>();

    //开始调用rpc，阻塞等待唤醒
    public void beginRpcRequest(RpcRequest request, RpcChannel channel) throws Exception {
        String requestId = request.getRequestId();

        lockMap.put(requestId, new Object());
        Object lock = lockMap.get(requestId);

        //发送消息
        channel.sendRpcRequest(request);

        //没有返回结果时，阻塞线程
        synchronized (lock) {
            while (responseMap.get(requestId) == null) {
                lock.wait();
            }
        }

    }

    //存储返回结果，并唤醒阻塞线程
    public void writeRpcResponse(RpcResponse response) {
        String requestId = response.getRequestId();
        responseMap.put(requestId, response);

        Object lock = lockMap.get(requestId);
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    //获取返回结果
    public RpcResponse getReponse(String requestId) {
        RpcResponse response = (RpcResponse) responseMap.get(requestId);
        if (null != response) {
            //清除requestId在map中内容
            responseMap.remove(requestId);
            lockMap.remove(requestId);
        }

        return response;
    }
}
