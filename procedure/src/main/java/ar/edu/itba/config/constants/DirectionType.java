package ar.edu.itba.config.constants;

/**
 * Represents the direction of the edges to use in the algorithm. The values can vary whether to respect the direction
 * of the edges or not.
 */
public enum DirectionType implements TextSearchType {

    /**
     * BOTH means that the direction of the edges, if there are any, does not mather.
     */
    BOTH("both"),
    /**
     * INCOMING means that the algorithm will only use the incoming edges in the algorithm.
     */
    INCOMING("incoming"),
    /**
     * OUTGOING means that the algorithm will only use the outgoing edges in the algorithm.
     */
    OUTGOING("outgoing");

    private final String text;

    DirectionType(String text) {
        this.text = text;
    }

    /**
     * Returns the text contained in the DirectionType. This texts should coincide with the one received in the
     * configuration.
     *
     * @return the text contained in the instance.
     */
    @Override
    public String getText() {
        return this.text;
    }

    /**
     * Returns the DirectionType from a string that represents its type. If the string does not coincide with any type
     * it returns null.
     *
     * @param type an String that should coincide with a DirectionType.
     * @return the DirectionType that coincides with the type String.
     */
    public static DirectionType fromString(String type) {
        return (DirectionType) TextSearchEnumHelper.fromString(DirectionType.values(), type);
    }

}
