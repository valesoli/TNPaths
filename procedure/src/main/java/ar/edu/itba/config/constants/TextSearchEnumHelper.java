package ar.edu.itba.config.constants;

/**
 * This is a helper class with only static methods for the TextSearchType. The objective of this class is to abstract
 * the search of the values of the enums. This class cannot be instantiated.
 */
class TextSearchEnumHelper {

    private TextSearchEnumHelper() {
        throw new UnsupportedOperationException("This class should not be instantiated!");
    }

    /**
     * This static method abstracts the logic of the search for all the Enums to avoid any problem with inheritance and
     * the values of the Class. It returns null if the value was not found.
     *
     * @param values the values of the Enum.
     * @param type the string to search for the values in the Enum.
     * @return the value that matched with that string.
     */
    static TextSearchType fromString(TextSearchType[] values, String type) {
        for (TextSearchType t: values) {
            if (t.getText().equals(type)){
                return t;
            }
        }
        return null;
    }

}
