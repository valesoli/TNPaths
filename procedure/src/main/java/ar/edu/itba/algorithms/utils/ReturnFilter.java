package ar.edu.itba.algorithms.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReturnFilter {

    private final Map<String, Object> returns;

    public ReturnFilter(Map<String, Object> returns) {
        this.returns = returns;
    }

    public Map<String, Object> apply(Map<String, Object> record) {

        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> aliases = (List<Map<String, Object>>) returns.get("ret");

        for (Map<String, Object> object : aliases) {
            processObject(record, result, object);
        }
        return result;
    }

    private void processObject(Map<String, Object> record, Map<String, Object> result, Map<String, Object> object) {
        String variable = (String) object.get("name");
        String alias = (String) object.get("alias");

        ReturnFilterTuple tuple = new ReturnFilterTuple(variable, alias);

        Object o = record.get(variable);

        if (o instanceof List) {
            tuple.processList((List<Map<String, Object>>) record.get(variable), result);
        } else {
            tuple.processEntity((Map<String, Object>) record.get(variable), result);
        }
    }
}

