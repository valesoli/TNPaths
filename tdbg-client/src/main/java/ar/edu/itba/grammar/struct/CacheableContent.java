package ar.edu.itba.grammar.struct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import ar.edu.itba.util.Pair;

public class CacheableContent {

    /**
     * Attributes for variables in main where clause
     * Keys: Variable names
     * Values: Attribute names
     */
    private Map<String, Set<String>> matchWhereAttributes = new HashMap<>();

    /**
     * Attributes for variables in when where clause
     * Keys: Variable names
     * Valeus: Attribute names
     */
    private Map<String, Set<String>> whenWhereAttributes = new HashMap<>();


    /**
     * Stores MATCH path length
     */
    private List<Integer> matchRelationLengthBounds = new ArrayList<>();

    /**
     * Stores BETWEEN interval
     */
    private Pair<String, String> betweenInterval;

    /**
     * Stores SNAPSHOT value
     */

    private String snapshotInterval;

    /**
     * 
     * WHEN info
     * 
     */

    /**
     * Store when path length
     */
    private List<Integer> whenRelationLengthBounds = new ArrayList<>();



    /**
     * 
     * CPATH info
     * 
     */
    private List<Integer> cpathRelationLengthBounds = new ArrayList<>();

    private Pair<String, String> cpathInterval;

    public void addMatchAttributeForVariable(String variable, String attributeName) {
        if (matchWhereAttributes.get(variable) == null)
            matchWhereAttributes.put(variable, new TreeSet<String>());
        matchWhereAttributes.get(variable).add(attributeName);
    }

    public void addWhenWhereAttributeForVariable(String variable, String attributeName) {
        if (whenWhereAttributes.get(variable) == null)
            whenWhereAttributes.put(variable, new TreeSet<String>());
        whenWhereAttributes.get(variable).add(attributeName);
    }

    public Set<String> getMatchWhereAttributesForVariable(String variable) {
        return this.matchWhereAttributes.get(variable);
    }

    public Set<String> getWhenWhereAttributesForVariable(String variable) {
        return this.whenWhereAttributes.get(variable);
    }

    public boolean variableHasMatchWhereAttributes(String variable) {
        return this.matchWhereAttributes.containsKey(variable);
    }

    public boolean variableHasWhenWhereAttributes(String variable) {
        return this.whenWhereAttributes.containsKey(variable);
    }

    public List<Integer> getMatchRelationLengthBounds() {
        return matchRelationLengthBounds;
    }

    public void setMatchRelationBounds(Integer distance) {
        if (this.matchRelationLengthBounds.size() == 0)
            this.matchRelationLengthBounds.add(distance);
    }

    public void setMatchRelationBounds(Integer minDist, Integer maxDist) {
        if (this.matchRelationLengthBounds.size() == 0) {
            this.matchRelationLengthBounds.add(minDist);
            this.matchRelationLengthBounds.add(maxDist);
        }
    }

    public List<Integer> getWhenRelationLengthBounds() {
        return whenRelationLengthBounds;
    }

    public void setWhenRelationBounds(Integer distance) {
        if (this.whenRelationLengthBounds.size() == 0)
            this.whenRelationLengthBounds.add(distance);
    }

    public void setWhenRelationBounds(Integer minDist, Integer maxDist) {
        if (this.whenRelationLengthBounds.size() == 0) {
            this.whenRelationLengthBounds.add(minDist);
            this.whenRelationLengthBounds.add(maxDist);
        }
    }

    public void setCpathRelationBounds(Integer distance) {
        if (this.cpathRelationLengthBounds.size() == 0)
            this.cpathRelationLengthBounds.add(distance);
    }

    public void setCpathRelationBounds(Integer minDist, Integer maxDist) {
        if (this.cpathRelationLengthBounds.size() == 0) {
            this.cpathRelationLengthBounds.add(minDist);
            this.cpathRelationLengthBounds.add(maxDist);
        }
    }

    public Pair<String, String> getCpathInterval() {
        return cpathInterval;
    }

    public void setCpathInterval(Pair<String, String> cpathInterval) {
        this.cpathInterval = cpathInterval;
    }

    public Pair<String, String> getBetweenInterval() {
        return betweenInterval;
    }

    public void setBetweenInterval(Pair<String, String> betweenInterval) {
        this.betweenInterval = betweenInterval;
    }

    public String getSnapshotInterval() {
        return snapshotInterval;
    }

    public void setSnapshotInterval(String snapshotInterval) {
        this.snapshotInterval = snapshotInterval;
    }
}
