package ar.edu.itba.algorithms.strategies.prunablepath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalNodePairPath;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.interval.IntervalSet;
import ar.edu.itba.algorithms.strategies.paths.CompleteIntersectionPathsStrategy;
import ar.edu.itba.algorithms.utils.graph.GraphUtils;

public class DirectedPrunablePathStrategy extends PrunablePathStrategy {

    private String direction;

    public DirectedPrunablePathStrategy(GraphDatabaseService db, String edgesLabel, String betweenString, String direction, Node startNode,
            Node endNode, Long minLength, Long maxLength) {
        super(db, edgesLabel, betweenString, startNode, endNode, minLength, maxLength);
        this.direction = direction;
    }

    public DirectedPrunablePathStrategy(GraphDatabaseService db, String edgesLabel, String betweenString, String direction, Node startNode,
            Long minLength, Long maxLength) {
        super(db, edgesLabel, betweenString, startNode, minLength, maxLength);
        this.direction = direction;
    }
    
    @Override
    protected String generatePrunableInfoQuery() {
        StringBuilder cPathPrunableInfoQuery = new StringBuilder()
        .append("match p= (u1:Object)")
            .append("incoming".equals(direction) ? "<" : "")
            .append("-[:").append(edgesLabel).append("*").append(minLength).append("..").append(maxLength).append("]-")
            .append("outgoing".equals(direction) ? ">" : "")
            .append("(u2:Object)\n")
        .append("where ID(u1)=$startId");
        if (endNode != null)
            cPathPrunableInfoQuery.append(" AND ID(u2) = $endId");
        // .append("unwind nodes(p) as theNodes\n")
        // .append("with p, theNodes\n")
        // .append("match (theNodes) -[]-> (attribute:Attribute) -[]-> (value:Value)\n")
        cPathPrunableInfoQuery.append("\nreturn relationships(p) as theRels\n"); //, collect({att: attribute, val: value}) as attValue
        return cPathPrunableInfoQuery.toString();
    }

    @Override
    public List<IntervalNodePairPath> getSolution() {
        List<Map<String, Object>> results = result.stream().map(result -> (Map<String, Object>) result).collect(Collectors.toList());

        List<IntervalNodePairPath> resultList = new ArrayList<IntervalNodePairPath>();

        // If direction is not null, exploit info in relationships
            for (Map<String, Object> res : results) {
                // ArrayList<Node> theNodes = (ArrayList<Node>) result.get("theNode");
                // log.info(String.valueOf(theNodes.size()));

                ArrayList<Relationship> theRels = (ArrayList<Relationship>) res.get("theRels");
                
                // theRels.size() > 0 is assumed here; Repeated code from RetrievePathsWithPaths

                IntervalNodePairPath first = new IntervalNodePairPath(theRels.get(0).getStartNode().getId(), null, 0L);
                IntervalNodePairPath currentNode = first;
                boolean discarded = false;

                for (Relationship rel : theRels) {
                    String [] relationshipIntervalStrings = (String []) rel.getProperty("interval"); // TODO handle exceptions
                    
                    List<Interval> relationshipIntervals = IntervalParser.fromStringArrayToIntervals(
                        relationshipIntervalStrings);

                    // Equivalent of Graph.setGranularityFromList()
                    for (Interval interval: relationshipIntervals) {
                        finalGranularity = finalGranularity == null ? interval.getGranularity() :
                            finalGranularity.getSmallerGranularity(interval.getGranularity());
                    }

                    if (betweenInterval != null) {
                        relationshipIntervals = GraphUtils.filterByInterval(relationshipIntervals, betweenInterval, false);
                    }

                    // IMPORTANT: this intersection determines whether path is continuous or not
                    // If not, discard path!!!

                    // TODO evaluate alternative to instantiating this strategy
                    IntervalSet interval = new CompleteIntersectionPathsStrategy(minLength, maxLength, null).getIntervalSet(
                        currentNode.getIntervalSet(), new IntervalSet(relationshipIntervals)
                    );

                    // TODO apply no loop check for all source-destination procedures
                    if (interval.isEmpty()) {
                        discarded = true;
                        break;
                    }

                    IntervalNodePairPath path = new IntervalNodePairPath(rel.getEndNode().getId(), interval, currentNode.getLength() + 1);
                    path.setPrevious(currentNode);

                    currentNode = path;
                }
                if (!discarded)
                    resultList.add(currentNode);
            }
        return resultList;
    }
}
