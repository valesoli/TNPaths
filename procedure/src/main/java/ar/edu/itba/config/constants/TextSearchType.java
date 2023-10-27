package ar.edu.itba.config.constants;

/**
 * This interface represents a type of Enums that should coincide with a string inside the configuration map received in
 * the procedure.
 */
public interface TextSearchType {

    /**
     * Returns the text contained in the TextSearchType. This texts should coincide with the one received in the
     * configuration.
     *
     * @return the text contained in the instance.
     */
    String getText();

}
