package com.fanshuai.domain;

import lombok.Data;

@Data
public class Frament {
    private Integer messageType;

    private RpcRequest rpcRequest;
    private RpcResponse rpcResponse;

    public static Frament generateHeartBeat() {
        Frament frament = new Frament();
        frament.setMessageType(MessageType.HEART_BEAT.value);

        return frament;
    }

    public static Frament generateRpcRequest(RpcRequest request) {
        Frament frament = new Frament();
        frament.setMessageType(MessageType.REQUEST.value);
        frament.setRpcRequest(request);

        return frament;
    }

    public static Frament generateRpcResponse(RpcResponse response) {
        Frament frament = new Frament();
        frament.setMessageType(MessageType.RESPONSE.value);
        frament.setRpcResponse(response);

        return frament;
    }

    @Override
    public String toString() {
        return "Frament{" +
                "messageType=" + messageType +
                ", rpcRequest=" + rpcRequest +
                ", rpcResponse=" + rpcResponse +
                '}';
    }
}
