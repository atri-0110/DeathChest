package org.allaymc.deathchest.serialization;

import com.google.gson.*;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtMapBuilder;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gson TypeAdapter for serializing and deserializing NbtMap objects.
 * Converts NbtMap to a JSON-compatible Map structure.
 */
public class NbtMapAdapter implements JsonSerializer<NbtMap>, JsonDeserializer<NbtMap> {

    @Override
    public JsonElement serialize(NbtMap src, Type typeOfSrc, JsonSerializationContext context) {
        if (src == null) {
            return JsonNull.INSTANCE;
        }
        
        // Convert NbtMap to a regular Map for JSON serialization
        Map<String, Object> regularMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : src.entrySet()) {
            regularMap.put(entry.getKey(), convertNbtValue(entry.getValue()));
        }
        
        return context.serialize(regularMap);
    }

    @Override
    public NbtMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) 
            throws JsonParseException {
        if (json == null || json.isJsonNull()) {
            return null;
        }
        
        try {
            // Deserialize JSON to regular Map
            Map<String, Object> regularMap = context.deserialize(json, Map.class);
            if (regularMap == null) {
                return null;
            }
            
            // Convert regular Map back to NbtMap
            NbtMapBuilder builder = NbtMap.builder();
            for (Map.Entry<String, Object> entry : regularMap.entrySet()) {
                builder.put(entry.getKey(), convertJsonValue(entry.getValue()));
            }
            
            return builder.build();
        } catch (Exception e) {
            throw new JsonParseException("Failed to deserialize NbtMap", e);
        }
    }

    /**
     * Converts NBT value types to JSON-compatible types.
     */
    private Object convertNbtValue(Object value) {
        if (value == null) {
            return null;
        }
        
        // Handle arrays
        if (value instanceof byte[] arr) {
            return arr;
        } else if (value instanceof int[] arr) {
            return arr;
        } else if (value instanceof long[] arr) {
            return arr;
        }
        
        // Handle nested NbtMap
        if (value instanceof NbtMap nested) {
            Map<String, Object> regularMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : nested.entrySet()) {
                regularMap.put(entry.getKey(), convertNbtValue(entry.getValue()));
            }
            return regularMap;
        }
        
        // Handle primitive types
        return value;
    }

    /**
     * Converts JSON value types back to NBT-compatible types.
     */
    private Object convertJsonValue(Object value) {
        if (value == null) {
            return null;
        }
        
        // Handle nested maps (convert back to NbtMap)
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            NbtMapBuilder builder = NbtMap.builder();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                builder.put(entry.getKey(), convertJsonValue(entry.getValue()));
            }
            return builder.build();
        }
        
        // Handle arrays
        if (value instanceof byte[] arr) {
            return arr;
        } else if (value instanceof int[] arr) {
            return arr;
        } else if (value instanceof long[] arr) {
            return arr;
        }
        
        // Handle primitive types
        return value;
    }
}
