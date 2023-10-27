package ar.edu.itba.population.models;

import ar.edu.itba.util.interval.Interval;

import java.util.Map;

public class ValueNode extends Node {

    public final static String LABEL = "Value";
    private final String value;

    public ValueNode(int id, Interval interval, String value) {
        super(id, interval);
        this.value = value;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("value", value);
        return map;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return LABEL;
    }
}
