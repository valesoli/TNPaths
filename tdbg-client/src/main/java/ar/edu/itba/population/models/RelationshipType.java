package ar.edu.itba.population.models;

public enum RelationshipType {

    FRIEND("Friend"), LIVED_IN("LivedIn"), FAN("Fan"), FLIGHT("Flight"), EDGE("Edge");

    private String name;

    RelationshipType(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name;
    }
}
