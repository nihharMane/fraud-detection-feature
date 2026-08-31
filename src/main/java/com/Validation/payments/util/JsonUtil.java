package com.Validation.payments.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class JsonUtil {

    private final ObjectMapper objectMapper;

    /**
     * Convert JSON string to Java object
     */
    public <T> T convertJsonToObject(String json, Class<T> clazz) {
        if (json == null || clazz == null) {
            log.debug("convertJsonToObject called with null input");
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Failed to convert JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Convert Java object to JSON string (DETERMINISTIC)
     */
    public String convertObjectToJson(Object obj) {
        if (obj == null) {
            log.debug("convertObjectToJson called with null object");
            return null;
        }
        try {

            objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

            return objectMapper.writeValueAsString(obj);

        } catch (Exception e) {
            log.error("Failed to convert object to JSON: {}", e.getMessage(), e);
            return null;
        }
    }
    public String prepareFormattedJson(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }

        try {
             LinkedHashMap<String, Object> map =
                    objectMapper.readValue(body, LinkedHashMap.class);
            // Serialize back to JSON
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("Error while formatting JSON body: {}", e.getMessage(), e);
            return null;
        }
    }
}