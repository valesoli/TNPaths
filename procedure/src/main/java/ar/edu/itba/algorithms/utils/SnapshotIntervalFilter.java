package ar.edu.itba.algorithms.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SnapshotIntervalFilter {

    private static final String intervalProperty = "interval";

    public static Map<String, Object> filter(Map<String, Object> record) {

        Map<String, Object> result = new LinkedHashMap<>(record);

        for (Object object : result.values()) {
            if (object instanceof List) {
                for (Map<String, Object> objectInList : (List<Map<String, Object>>) object) {
                    removeIntervalProperty(objectInList);
                }

            } else {
                removeIntervalProperty((Map<String, Object>) object);
            }
        }
        return result;
    }

    private static void removeIntervalProperty(Map<String, Object> entity){
        entity.remove(intervalProperty);
    }
}
