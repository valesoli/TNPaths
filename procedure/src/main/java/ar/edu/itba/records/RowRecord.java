package ar.edu.itba.records;

import java.util.Map;

public class RowRecord {

    public final Map<String, Object> row;

    public RowRecord(Map<String, Object> row) {
        this.row = row;
    }
}
