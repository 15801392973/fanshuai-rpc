package com.fanshuai.codec;

import com.fanshuai.domain.RpcRequest;
import com.fanshuai.domain.RpcResponse;
import com.google.gson.Gson;

import java.nio.charset.Charset;

public class RpcCodec {
    private static Gson gson = new Gson();
    public static byte[] encodeResponse(RpcResponse response) {
        String json = gson.toJson(response);

        return json.getBytes(Charset.forName("UTF-8"));
    }

    public static RpcRequest decodeRequest(byte[] data) {
        String json = new String(data, Charset.forName("UTF-8"));

        return gson.fromJson(json, RpcRequest.class);
    }
}
