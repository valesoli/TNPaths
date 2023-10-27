package ar.edu.itba.procedures.graphindex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
import ar.edu.itba.algorithms.utils.interval.IntervalParser;
import ar.edu.itba.algorithms.utils.time.DateTime;

public class ConcatIndexUpdate {
    
    
    @Context
    public GraphDatabaseService db;
    
    @Context
    public Log log;
    
    @Procedure(value="graphindex.concatInsertOrUpdateEdge", mode=Mode.WRITE)
    @Description("Update index after insertion or update of edge between 2 nodes.")
    public void insertOrUpdateEdge (
        @Name("startNode") Node startNode,
        @Name("endNode") Node endNode,
        @Name("relationshipName") String relationshipName
    ) {

        // Get edge from node v1 to v2, create or update (aka resurrect) it
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

        List<Long> indexNodeIds = new ArrayList<>();

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
        
        Result incomingEdges = db.execute(matchIncomingQuery.toString());

        incomingEdges.stream().forEach( row -> {
            Relationship inRel = (Relationship) row.get("r");

            StringBuilder createIndexNodeQuery = new StringBuilder();

            createIndexNodeQuery.append("CREATE (n:INDEX:").append(relationshipName).append(" {\n")
                .append("From: '").append(nowDateString).append("', To: 'Now', Source: ").append(inRel.getStartNode().getProperty("id"))
                .append(", Destination: ").append(edge.getEndNode().getProperty("id")).append(", Intermediate: ").append(edge.getStartNode().getProperty("id"))
                .append(", Length: 2})\n")
                .append("RETURN ID(n) as nId");

            indexNodeIds.add((Long) db.execute(createIndexNodeQuery.toString()).next().get("nId"));
        });
        
        // 2.2 get all edges outgoing from v2 which are valid Now
        StringBuilder matchOutgoingQuery = new StringBuilder();
        matchOutgoingQuery.append("MATCH (v2:Object) - [r:").append(edge.getType().name()).append("] -> ()\n")
            .append("WHERE ID(v2) = ").append(edge.getEndNode().getId()).append("\n    AND ANY(x in r.interval where x contains \"Now\")\n")
            .append("RETURN  r");
        
        Result outgoingEdges = db.execute(matchOutgoingQuery.toString());

        outgoingEdges.stream().forEach( row -> {
            Relationship inRel = (Relationship) row.get("r");

            StringBuilder createIndexNodeQuery = new StringBuilder();

            createIndexNodeQuery.append("CREATE (n:INDEX:").append(relationshipName).append(" {\n")
                .append("From: '").append(nowDateString).append("', To: 'Now', Source: ").append(edge.getStartNode().getProperty("id"))
                .append(", Destination: ").append(inRel.getEndNode().getProperty("id")).append(", Intermediate: ").append(edge.getEndNode().getProperty("id"))
                .append(", Length: 2})\n")
                .append("RETURN ID(n) as nId");

            indexNodeIds.add((Long) db.execute(createIndexNodeQuery.toString()).next().get("nId"));
        });

        // Super untidy connect, first incoming edges to new index nodes, then outgoing

        StringBuilder indexNodesQuery = new StringBuilder();
        indexNodesQuery
            .append("MATCH (i1:INDEX:").append(relationshipName).append(") ").append("MATCH (i2:INDEX:").append(relationshipName).append(")\n")
            .append("WHERE ID(i1) in ").append(indexNodeIds.toString())
            .append("    AND i1.Destination = i2.Source\n")
            .append("RETURN i1, i2");

        // log.info("Getting nodes to new Index nodes\n" + indexNodesQuery);

        Result indexNodes = db.execute(indexNodesQuery.toString());

        connectPairs(indexNodes);

        indexNodesQuery = new StringBuilder();
        indexNodesQuery
            .append("MATCH (i1:INDEX:").append(relationshipName).append(") ").append("MATCH (i2:INDEX:").append(relationshipName).append(")\n")
            .append("WHERE ID(i2) in ").append(indexNodeIds.toString())
            .append("    AND i1.Destination = i2.Source\n")
            .append("RETURN i1, i2");

        // log.info("Getting nodes from new Index nodes\n" + indexNodesQuery);

        indexNodes = db.execute(indexNodesQuery.toString());

        connectPairs(indexNodes);
    }

    private void connectPairs (Result indexNodes) {
        indexNodes.stream().forEach(row -> {
            Node i1 = (Node) row.get("i1");
            Node i2 = (Node) row.get("i2");

            Interval i1Period = IntervalParser.fromStringLimits((String) i1.getProperty("From"),(String) i1.getProperty("To"));
            Interval i2Period = IntervalParser.fromStringLimits((String) i2.getProperty("From"),(String) i2.getProperty("To"));
            Optional<Interval> intersection = i1Period.intersection(i2Period);
            if (!intersection.isPresent()) return;
            
            // log.info("Creating rel for " + i1.getId() + " " + i2.getId());
            Relationship concatEdge = i1.createRelationshipTo(i2, RelationshipType.withName("concat"));
            concatEdge.setProperty("interval", intersection.get().toString());
        });
    }

    @Procedure(value="graphindex.concatDeleteEdge", mode=Mode.WRITE)
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

        // toBeDeletedIndexNodesRight.append("MATCH (i:INDEX) WHERE i.Intermediate = ").append(edge.getEndNode().getProperty("id"))
        //     .append(" AND i.Source = ").append(edge.getStartNode().getProperty("id")).append(" AND i.To = 'Now'\n")
        //     .append("SET i.To = '").append(nowDateString).append("'");

        // toBeDeletedIndexNodesLeft.append("MATCH (i:INDEX) WHERE i.Intermediate = ").append(edge.getStartNode().getProperty("id"))
        //     .append(" AND i.Destination = ").append(edge.getEndNode().getProperty("id")).append(" AND i.To = 'Now'\n")
        //     .append("SET i.To = '").append(nowDateString).append("'");

        // // log.info("Queries\n" + toBeDeletedIndexNodesLeft.toString() + "\n" + toBeDeletedIndexNodesRight.toString());

        // db.execute(toBeDeletedIndexNodesLeft.toString());
        // db.execute(toBeDeletedIndexNodesRight.toString());

        StringBuilder deletedIndexNodes = new StringBuilder().append("MATCH (i:INDEX) WHERE i.Intermediate = ").append(edge.getEndNode().getProperty("id"))
        .append(" AND i.Source = ").append(edge.getStartNode().getProperty("id")).append(" AND i.To = 'Now'\n")
        .append("RETURN i\nUNION ALL\n")
        .append("MATCH (i:INDEX) WHERE i.Intermediate = ").append(edge.getStartNode().getProperty("id"))
            .append(" AND i.Destination = ").append(edge.getEndNode().getProperty("id")).append(" AND i.To = 'Now'\n")
            .append("RETURN i\n");


        log.info("Getting updated index nodes: \n" + deletedIndexNodes.toString());
        Result nodes = db.execute(deletedIndexNodes.toString());

        // Now delete concat relationships

        nodes.stream().forEach(row -> {
            Node iN = (Node) row.get("i");
            for (Relationship r : iN.getRelationships(RelationshipType.withName("concat"))) {
                String in = (String) r.getProperty("interval");
                r.setProperty("interval", in.replace("Now", nowDateString));
            }
            iN.setProperty("To", nowDateString);
        });
    }
}