package ar.edu.itba.algorithms.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReturnFilterTuple {

    private final String variable;
    private final String alias;

    public ReturnFilterTuple(String variable, String alias) {
        this.variable = variable;
        this.alias = alias;
    }

    private String returnName() {
        return alias.isEmpty() ? variable : alias;
    }

    public void processEntity(Map<String, Object> entity, Map<String, Object> result) {
        String returnName = returnName();
        result.put(returnName, entity);
    }

    public void processList(List<Map<String, Object>> entities, Map<String, Object> result) {
        String returnName = returnName();
        List<Object> returns = new ArrayList<>(entities);
        result.put(returnName, returns);
    }
}
