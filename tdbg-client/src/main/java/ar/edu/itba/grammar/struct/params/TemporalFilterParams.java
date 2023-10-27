package ar.edu.itba.grammar.struct.params;

import ar.edu.itba.grammar.struct.ReturnEntity;

import java.util.List;

public class TemporalFilterParams {
    private String query;
    private String value;
    private List<ReturnEntity> returnEntities;

    public TemporalFilterParams(final String query, final String value, final List<ReturnEntity> returnEntities) {
        this.query = query;
        this.value = value;
        this.returnEntities = returnEntities;
    }

    public String getQuery() {
        return query;
    }

    public String getValue() {
        return value;
    }

    public List<ReturnEntity> getReturnEntities() {
        return returnEntities;
    }
}
