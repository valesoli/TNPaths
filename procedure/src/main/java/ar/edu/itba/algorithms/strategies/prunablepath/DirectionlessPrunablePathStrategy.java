package ar.edu.itba.algorithms.strategies.prunablepath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import ar.edu.itba.algorithms.strategies.paths.CompleteIntersectionPathsStrategy;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.algorithms.utils.graph.GraphUtils;


public class DirectionlessPrunablePathStrategy extends PrunablePathStrategy {

    public DirectionlessPrunablePathStrategy(GraphDatabaseService db, String edgesLabel, String betweenString, Node startNode,
            Node endNode, Long minLength, Long maxLength) {
        super(db, edgesLabel, betweenString, startNode, endNode, minLength, maxLength);
    }

    public DirectionlessPrunablePathStrategy(GraphDatabaseService db, String edgesLabel, String betweenString, Node startNode,
            Long minLength, Long maxLength) {
        super(db, edgesLabel, betweenString, startNode, minLength, maxLength);
    }
    
    @Override
    public List<IntervalNodePairPath> getSolution() {
        
        List<Map<String, Object>> results = result.stream().map(result -> (Map<String, Object>) result).collect(Collectors.toList());

        List<IntervalNodePairPath> resultList = new ArrayList<IntervalNodePairPath>();

        for (Map<String, Object> res : results) {
            ArrayList<Node> nodes = (ArrayList<Node>) res.get("theNodes");
            ArrayList<Relationship> relationships = (ArrayList<Relationship>) res.get("theRels");

            IntervalNodePairPath first = new IntervalNodePairPath(nodes.get(0).getId(), null, 0L);
            IntervalNodePairPath currentNode = first;
            boolean discarded = false;

            for(int i = 0; i < relationships.size(); i++) {
                String [] relationshipIntervalStrings = (String []) relationships.get(i).getProperty("interval"); // TODO handle exceptions
                
                List<Interval> relationshipIntervals = IntervalParser.fromStringArrayToIntervals(
                    relationshipIntervalStrings);

                for (Interval interval: relationshipIntervals) {
                    finalGranularity = finalGranularity == null ? interval.getGranularity() :
                        finalGranularity.getSmallerGranularity(interval.getGranularity());
                }

                if (betweenInterval != null) {
                    relationshipIntervals = GraphUtils.filterByInterval(relationshipIntervals, betweenInterval, false);
                }

                // IMPORTANT: this intersection determines whether path is continuous or not
                // If not, discard path!!!

                IntervalSet interval = new CompleteIntersectionPathsStrategy(minLength, maxLength, null).getIntervalSet(
                    currentNode.getIntervalSet(), new IntervalSet(relationshipIntervals)
                );

                if (interval.isEmpty()) {
                    discarded = true;
                    break;
                }                    

                IntervalNodePairPath path = new IntervalNodePairPath(nodes.get(i+1).getId(), interval, currentNode.getLength() + 1);
                path.setPrevious(currentNode);

                currentNode = path;
                }

            if (!discarded)
                resultList.add(currentNode);  
        }
        return resultList;
    }

    @Override
    protected String generatePrunableInfoQuery() {
        StringBuilder cPathPrunableInfoQuery = new StringBuilder()
        .append("match p= (u1:Object)")
            .append("-[:").append(edgesLabel).append("*").append(minLength).append("..").append(maxLength).append("]-")
            .append("(u2:Object)\n")
        .append("where ID(u1)=$startId");
        if (endNode != null)
            cPathPrunableInfoQuery.append(" AND ID(u2) = $endId");
        // .append("unwind nodes(p) as theNodes\n")
        // .append("with p, theNodes\n")
        // .append("match (theNodes) -[]-> (attribute:Attribute) -[]-> (value:Value)\n")
        cPathPrunableInfoQuery.append("\nreturn relationships(p) as theRels\n"); //, collect({att: attribute, val: value}) as attValue
        cPathPrunableInfoQuery.append(", nodes(p) as theNodes");
        return cPathPrunableInfoQuery.toString();
    }
}
