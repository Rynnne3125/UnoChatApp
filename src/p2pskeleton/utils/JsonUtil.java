package p2pskeleton.utils;

import com.google.gson.Gson;

/**
 * Utility class for JSON serialization and deserialization using Gson.
 * Provides convenient static methods for converting between Java objects and JSON strings.
 */
public class JsonUtil {
    
    /**
     * Singleton Gson instance for JSON operations.
     */
    private static Gson gson = new Gson();
    
    /**
     * Converts a Java object to its JSON string representation.
     * 
     * @param obj The object to serialize to JSON.
     * @return The JSON string representation of the object.
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
    
    /**
     * Converts a JSON string to a Java object of the specified class.
     * 
     * @param <T> The type of the object to deserialize to.
     * @param json The JSON string to deserialize.
     * @param cls The class of the object to deserialize to.
     * @return The deserialized object.
     */
    public static <T> T fromJson(String json, Class<T> cls) {
        return gson.fromJson(json, cls);
    }
    
    /**
     * Gets the Gson instance used by this utility.
     * 
     * @return The Gson instance.
     */
    public static Gson getGson() {
        return gson;
    }
    
    /**
     * Sets a custom Gson instance for this utility.
     * 
     * @param customGson The custom Gson instance to use.
     */
    public static void setGson(Gson customGson) {
        gson = customGson;
    }
}