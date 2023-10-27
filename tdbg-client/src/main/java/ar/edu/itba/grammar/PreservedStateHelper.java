package ar.edu.itba.grammar;

public enum PreservedStateHelper {
    STATE;

    private static boolean isIndexEnabled = false;

    public void setIsIndexEnabled(boolean isEnabled) {isIndexEnabled = isEnabled;}
    public boolean getIsIndexEnabled() {return isIndexEnabled;}
}
