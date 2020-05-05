package com.fanshuai.codec;

import com.fanshuai.domain.Frament;
import com.google.gson.Gson;
import util.JSONUtil;

import java.nio.charset.StandardCharsets;

public class FramentCodec {
    public static byte[] encode(Frament frament) {
        try {
            String json = JSONUtil.toJson(frament);
            return json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    public static Frament decode(byte[] data) {
        String json = new String(data, StandardCharsets.UTF_8);

        try {
            return JSONUtil.fromJson(json, Frament.class);
        } catch (Exception e) {

        }
        return null;
    }
}
