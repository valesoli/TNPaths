package ar.edu.itba.grammar.struct;

import java.util.HashMap;
import java.util.Map;

public class Variable {

    private String name;

    /**
     * If the attribute was used in select and the index of selection
     */
    private Integer selectIndex;

    private Map<String, Attribute> attributes = new HashMap<>();

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean hasAttribute(String attribute) {
        return attributes.containsKey(attribute);
    }

    public void addAttribute(Attribute attribute) {
        attributes.put(attribute.getName(), attribute);
    }

    public Attribute getAttribute(String attribute) {
        return attributes.get(attribute);
    }

    public Map<String, Attribute> getAttributes() { return attributes; }

    public boolean isSelected() {
        return selectIndex != null;
    }

    public void setSelectIndex(Integer index) {
        this.selectIndex = index;
    }

    public Integer getSelectIndex() {
        return selectIndex;
    }

    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }
}
