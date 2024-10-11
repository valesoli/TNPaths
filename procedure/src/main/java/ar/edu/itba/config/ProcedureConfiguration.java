package ar.edu.itba.config;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.config.constants.ConfigurationFieldNames;
import ar.edu.itba.config.constants.DirectionType;
import ar.edu.itba.config.constants.ProjectionType;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * This class wraps the configuration object received by the Neo4J procedure.
 */
public class ProcedureConfiguration {

    private final Map<String, Object> config;

    /**
     * Creates a ProcedureConfiguration object using the configuration received by the procedure.
     *
     * @param config the configuration received by the procedure.
     */
    public ProcedureConfiguration(Map<String, Object> config) {
        this.config = new HashMap<>(config);
    }

    /**
     * Returns true if the configuration contains all the keys.
     *
     * @param keys the keys to check if they are contained in the configuration.
     * @return true if all the keys are known, false otherwise.
     */
    private boolean containsKeys(String... keys) {
        for (String key: keys) {
            if (!this.config.containsKey(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the kind of projection to apply the algorithm in the graph.
     *
     * @return returns the kind of projection to apply the algorithm in the graph.
     */
    public ProjectionType getProjection() {
        ProjectionType defaultProjection = this.config.containsKey(ConfigurationFieldNames.NODE_LABEL) ||
                this.config.containsKey(ConfigurationFieldNames.EDGE_LABEL) ?
                ProjectionType.LABEL : ProjectionType.FULL;
        return ProjectionType.fromString(
                (String) this.config.getOrDefault(
                        ConfigurationFieldNames.GRAPH, defaultProjection.getText()
                )
        );
    }

    /**
     * Returns a DirectionType that indicates whether the direction of the edges matter or not for the algorithm.
     * If there is no value, it returns the default value that is BOTH.
     *
     * @return returns a DirectionType that indicates whether the direction of the edges matter of not for the
     * algorithm.
     */
    public DirectionType getDirection() {
        return DirectionType.fromString(
                (String) this.config.getOrDefault(
                        ConfigurationFieldNames.DIRECTION, DirectionType.BOTH.getText()
                )
        );
    }

    /**
     * Returns a String value for a specific key.
     *
     * @param key the key to search in the configuration.
     * @return returns a String that represents the value inside the configuration.
     */
    public String getStringFromKey(String key) {
        return (String) this.config.get(key);
    }

    /**
     * Returns a String value for a specific key, if found. Otherwise, it returns the value received as a parameter.
     *
     * @param key the key to search in the configuration
     * @param defaultValue the value to return if the key was not found.
     * @return returns a String that represents the value inside the configuration, if found.
     */
    public String getOrElse(String key, String defaultValue) {
        return (String) this.config.getOrDefault(key, defaultValue);
    }

    /**
     * Returns the Interval that limits the graph search.
     *
     * @return returns an interval that limits the graph to search.
     */
    public Interval getIntervalLimit() {
        String intervalStr = (String) this.config.getOrDefault(
                ConfigurationFieldNames.BETWEEN, null
        );
        if (intervalStr == null) { return null; }
        return IntervalParser.fromString(intervalStr);
    }
    
    public List<Integer> getExcludeList(){
    	
    	/*List<Integer> a = new ArrayList(Arrays.asList( config.getOrDefault(ConfigurationFieldNames.EXCLUDE,null)));*/
    	String stringList = (String) config.getOrDefault(ConfigurationFieldNames.EXCLUDE, null);
    	if (stringList == null) return null;
    	
    	List<String> myList = new ArrayList<String>(Arrays.asList(stringList.substring(1,stringList.length()-1).split(",")));
    	List<Integer> b = new ArrayList<>();
    	for (String s:myList) {
    		b.add(Integer.parseInt(s.trim()));
    	}
    	return b; 
    	
    }
    
    public static void main(String[] args) {
    	
    	List<Integer> a = new ArrayList<>(Arrays.asList(2,3,5));
    	String stringList = a.toString();
       	System.out.println(stringList);
       	System.out.println(stringList.substring(1,stringList.length()-1));
       	List<Integer> b = new ArrayList<>();
       	
    	List<String> myList = new ArrayList<String>(Arrays.asList(stringList.substring(1,stringList.length()-1).split(",")));
    	for (String s:myList) {
    		b.add(Integer.parseInt(s.trim()));
    	}
    	System.out.println(b);
    	/*Map<String, Object> temp = new HashMap<>();
    	temp.put("exclude", a);
    	temp.put("between", "a");
    	System.out.println(temp);
    	Long i = 3L;
    	--i;
    	System.out.println(a.contains((int)(long)i));
    	*/
    }
}
