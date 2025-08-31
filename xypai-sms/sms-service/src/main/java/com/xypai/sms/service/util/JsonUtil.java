package com.xypai.sms.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Util: JSON工具类
 *
 * @author XyPai Team
 * @since 2025-01-02
 */
@Slf4j
public final class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private JsonUtil() {
    }

    /**
     * Util: 创建ObjectMapper
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 注册Java时间模块
        mapper.registerModule(new JavaTimeModule());

        // 禁用时间戳格式
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 忽略未知属性
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    /**
     * Util: 对象转JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Util: 对象转JSON失败, obj={}, error={}", obj.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    /**
     * Util: 对象转格式化JSON字符串
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Util: 对象转格式化JSON失败, obj={}, error={}", obj.getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("JSON序列化失败", e);
        }
    }

    /**
     * Util: JSON字符串转对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Util: JSON转对象失败, json={}, class={}, error={}", json, clazz.getSimpleName(), e.getMessage());
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    /**
     * Util: JSON字符串转对象（支持泛型）
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Util: JSON转对象失败, json={}, type={}, error={}", json, typeReference.getType(), e.getMessage());
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    /**
     * Util: JSON字符串转Map
     */
    public static Map<String, Object> toMap(String json) {
        return fromJson(json, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * Util: JSON字符串转List
     */
    public static <T> List<T> toList(String json, Class<T> elementClass) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass));
        } catch (JsonProcessingException e) {
            log.error("Util: JSON转List失败, json={}, class={}, error={}", json, elementClass.getSimpleName(), e.getMessage());
            throw new RuntimeException("JSON反序列化失败", e);
        }
    }

    /**
     * Util: 对象转Map
     */
    public static Map<String, Object> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }

        return OBJECT_MAPPER.convertValue(obj, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * Util: Map转对象
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            return null;
        }

        return OBJECT_MAPPER.convertValue(map, clazz);
    }

    /**
     * Util: 深度复制对象
     */
    public static <T> T deepCopy(T obj, Class<T> clazz) {
        if (obj == null) {
            return null;
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Util: 对象深度复制失败, class={}, error={}", clazz.getSimpleName(), e.getMessage());
            throw new RuntimeException("对象深度复制失败", e);
        }
    }

    /**
     * Util: 检查字符串是否为有效JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }

        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * Util: 安全的JSON转换（不抛异常）
     */
    public static String toJsonSafe(Object obj) {
        try {
            return toJson(obj);
        } catch (Exception e) {
            log.warn("Util: JSON转换失败，返回toString, obj={}, error={}",
                    obj != null ? obj.getClass().getSimpleName() : "null", e.getMessage());
            return obj != null ? obj.toString() : null;
        }
    }

    /**
     * Util: 安全的JSON解析（不抛异常）
     */
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return fromJson(json, clazz);
        } catch (Exception e) {
            log.warn("Util: JSON解析失败，返回null, json={}, class={}, error={}",
                    json, clazz.getSimpleName(), e.getMessage());
            return null;
        }
    }

    /**
     * Util: 合并JSON对象
     */
    public static String mergeJson(String json1, String json2) {
        if (json1 == null || json1.trim().isEmpty()) {
            return json2;
        }

        if (json2 == null || json2.trim().isEmpty()) {
            return json1;
        }

        try {
            Map<String, Object> map1 = toMap(json1);
            Map<String, Object> map2 = toMap(json2);

            if (map1 != null && map2 != null) {
                map1.putAll(map2);
                return toJson(map1);
            }

            return json1;
        } catch (Exception e) {
            log.error("Util: JSON合并失败, error={}", e.getMessage());
            return json1;
        }
    }
}
