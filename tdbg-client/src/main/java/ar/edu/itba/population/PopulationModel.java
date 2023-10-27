package ar.edu.itba.population;

public enum PopulationModel {

    SOCIAL_NETWORK("S"), FLIGHTS("F");

    private String text;

    PopulationModel(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static PopulationModel fromString(String text) {
        for (PopulationModel value : PopulationModel.values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        throw new IllegalArgumentException();
    }
}
