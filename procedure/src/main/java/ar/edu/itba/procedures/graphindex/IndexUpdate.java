package ar.edu.itba.procedures.graphindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.neo4j.cypher.internal.frontend.v2_3.ast.functions.E;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import ar.edu.itba.algorithms.utils.interval.Granularity;
import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.time.DateTime;

public class IndexUpdate {

    @Context
    public GraphDatabaseService db;
    
    @Context
    public Log log;
    
    @Procedure(value="graphindex.treeInsertOrUpdateEdge", mode=Mode.WRITE)
    @Description("Update index after insertion or update of edge between 2 nodes.")
    public void insertOrUpdateEdge (
        @Name("startNode") Node startNode,
        @Name("endNode") Node endNode,
        @Name("relationshipName") String relationshipName
    ) {

        // Get edge from node v1 to v2
        Result relationship = db.execute("MATCH (u1:Object) - [r:" + relationshipName + "] -> (u2:Object)\n" +
        "WHERE ID(u1) = " + startNode.getId() + " AND ID(u2) = " + endNode.getId() +
        "\n RETURN r");
        Relationship edge;
        ArrayList<String> intervals = new ArrayList<>();
        if (!relationship.hasNext())
            edge = startNode.createRelationshipTo(endNode, RelationshipType.withName(relationshipName));
        else {
            edge = (Relationship) relationship.next().get("r");
            intervals.addAll(Arrays.asList((String[]) edge.getProperty("interval")));
        }
        intervals.add(new Interval(DateTime.now().toEpochSecond(false), DateTime.now().toEpochSecond(false), Granularity.DATETIME, true).toString());

        edge.setProperty("interval", intervals.toArray(new String[0]));
        // 1., check if there are any indexes valid at Now time
        Result nowIndexQuery = db.execute("MATCH (i:INDEX:META) where i.To = 'Now' return i");

        if (!nowIndexQuery.hasNext()) return;

        // Get Now date

        String nowDateString = DateTime.now().toString();

        // 2.1. get all edges incoming to v1 which are valid Now
        StringBuilder matchIncomingQuery = new StringBuilder();
        matchIncomingQuery.append("MATCH () - [r:").append(edge.getType().name()).append("] -> (v1:Object)\n")
            .append("WHERE ID(v1) = ").append(edge.getStartNode().getId()).append("\n    AND ANY(x in r.interval where x contains \"Now\")\n")
            .append("RETURN  r");
        
        // log.info("Executing incoming query\n" + matchIncomingQuery.toString());

        Result incomingEdges = db.execute(matchIncomingQuery.toString());

        incomingEdges.stream().forEach( row -> {
            Relationship inRel = (Relationship) row.get("r");

            StringBuilder createIndexNodeQuery = new StringBuilder();

            createIndexNodeQuery.append("CREATE (n:INDEX {\n")
                .append("From: '").append(nowDateString).append("', To: 'Now', Source: ").append(inRel.getStartNode().getProperty("id"))
                .append(", Destination: ").append(edge.getEndNode().getProperty("id")).append(", Intermediate: [").append(edge.getStartNode().getProperty("id")).append("]")
                .append(", Length: 2})\n")
                .append("MERGE (n)-[:type]->(i:INDEX:META{To:'Now'})\n");

            db.execute(createIndexNodeQuery.toString());
        });
        
        // 2.2 get all edges outgoing from v2 which are valid Now
        StringBuilder matchOutgoingQuery = new StringBuilder();
        matchOutgoingQuery.append("MATCH (v2:Object) - [r:").append(edge.getType().name()).append("] -> ()\n")
            .append("WHERE ID(v2) = ").append(edge.getEndNode().getId()).append("\n    AND ANY(x in r.interval where x contains \"Now\")\n")
            .append("RETURN  r");
        
        // log.info("Executing outgoing query\n" + matchOutgoingQuery.toString());
        
        Result outgoingEdges = db.execute(matchOutgoingQuery.toString());

        outgoingEdges.stream().forEach( row -> {
            Relationship inRel = (Relationship) row.get("r");

            StringBuilder createIndexNodeQuery = new StringBuilder();

            createIndexNodeQuery.append("CREATE (n:INDEX {\n")
                .append("From: '").append(nowDateString).append("', To: 'Now', Source: ").append(edge.getStartNode().getProperty("id"))
                .append(", Destination: ").append(inRel.getEndNode().getProperty("id")).append(", Intermediate: [").append(edge.getEndNode().getProperty("id")).append("]")
                .append(", Length: 2})\n")
                .append("MERGE (n)-[:type]->(i:INDEX:META{To:'Now'})\n");

            db.execute(createIndexNodeQuery.toString());
        });
    }

    @Procedure(value="graphindex.treeDeleteEdge", mode=Mode.WRITE)
    @Description("Update index after insertion or update of edge between 2 nodes")
    public void deleteEdge (
        @Name("edge") Relationship edge
    ) {

        // Get current date
        String nowDateString = DateTime.now().toString();

        // Replace Now in current relationship by current day  
        List<String> interval = Arrays.asList((String[]) edge.getProperty("interval"));
        edge.setProperty("interval", interval.stream().map(period -> {
            if(!period.contains("Now")) return period;
            else return period.replace("Now", DateTime.now().toString()); 
        }).collect(Collectors.toList()).toArray(new String[0]));

        StringBuilder toBeDeletedIndexNodesRight = new StringBuilder();
        StringBuilder toBeDeletedIndexNodesLeft = new StringBuilder();

        toBeDeletedIndexNodesRight.append("MATCH (i:INDEX) WHERE i.Intermediate = [").append(edge.getEndNode().getProperty("id")).append("]")
            .append(" AND i.Source = ").append(edge.getStartNode().getProperty("id")).append(" AND i.To = 'Now'\n")
            .append("SET i.To = '").append(nowDateString).append("'");

        toBeDeletedIndexNodesLeft.append("MATCH (i:INDEX) WHERE i.Intermediate = [").append(edge.getStartNode().getProperty("id")).append("]")
            .append(" AND i.Destination = ").append(edge.getEndNode().getProperty("id")).append(" AND i.To = 'Now'\n")
            .append("SET i.To = '").append(nowDateString).append("'");

        // log.info("Queries\n" + toBeDeletedIndexNodesLeft.toString() + "\n" + toBeDeletedIndexNodesRight.toString());

        db.execute(toBeDeletedIndexNodesLeft.toString());
        db.execute(toBeDeletedIndexNodesRight.toString());
    }
}
