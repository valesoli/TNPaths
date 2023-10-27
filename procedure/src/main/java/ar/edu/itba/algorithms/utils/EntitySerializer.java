package ar.edu.itba.algorithms.utils;

import org.neo4j.graphdb.Entity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EntitySerializer {

    public static Map<String, Object> serialize(Map<String, Object> record) {

        Map<String, Object> result = new LinkedHashMap<>();

        for (String key : record.keySet()) {
            Object object = record.get(key);

            if (object instanceof Entity) {
                addAllProperties(result, key, object);
            } else if (object instanceof List) {
                for (Object objectInList : (List<Entity>) object) {
                    addAllProperties(result, key, objectInList);
                }
            }
        }

        return result;
    }

    private static void addAllProperties(Map<String, Object> result, String key, Object object) {
        result.put(key, ((Entity) object).getAllProperties());
    }
}
