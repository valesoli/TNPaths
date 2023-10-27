package ar.edu.itba.grammar.struct;

import ar.edu.itba.grammar.CypherOutputGenerator;

public enum AggregateFunction {

    EARLIEST("earliest"),
    LATEST_DEPARTURE("latestDeparture"),
    LATEST_ARRIVAL("latestArrival"),
    FASTEST("fastest"),
    SHORTEST("shortest");

    private String name;

    AggregateFunction(String name) {
        this.name = name;
    }

    public String toString() {
        return "paths.intervals." + this.name;
    }

    public static AggregateFunction getFromFunctionName(String function) {
        switch (function.toLowerCase()) {
            case CypherOutputGenerator.FUNCTION_EARLIEST:
                return EARLIEST;
            case CypherOutputGenerator.FUNCTION_LATEST_ARRIVAL:
                return LATEST_ARRIVAL;
            case CypherOutputGenerator.FUNCTION_LATEST_DEPARTURE:
                return LATEST_DEPARTURE;
            case CypherOutputGenerator.FUNCTION_SHORTEST:
                return SHORTEST;
            case CypherOutputGenerator.FUNCTION_FASTEST:
                return FASTEST;
        }
        return null;
    }

}
