package util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {
    private static ObjectMapper mapper = new ObjectMapper();
    public static <T> String toJson(T bean) throws Exception {
        return mapper.writeValueAsString(bean);
    }

    public static <T> T fromJson(String json, Class<T> cls) throws Exception {
        return mapper.readValue(json, cls);
    }
}
