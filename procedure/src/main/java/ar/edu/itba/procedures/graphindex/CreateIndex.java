package ar.edu.itba.procedures.graphindex;

import org.neo4j.logging.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;

import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

import ar.edu.itba.algorithms.utils.interval.Interval;
import ar.edu.itba.algorithms.utils.interval.IntervalParser;

@SuppressWarnings("unused")
public class CreateIndex {

    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="graphindex.createIndex", mode=Mode.WRITE)
    @Description("Create index for paths of length 2 for relationship r on given interval")
    public void createGraphIndex(
        @Name("relationship") String relationship,
        @Name("startTime") String startTime,
        @Name("endTime") String endTime,
        @Name("startId") Long startId,
        @Name("endId") Long endId
    ){
        // Check if index root already exists
        // Transaction metaNodeTransaction = db.beginTx();

        StringBuilder newIndexNodeBuilder = new StringBuilder()
            .append("MATCH (n:INDEX:META:").append(relationship).append(")\n")
            .append("WHERE n.From = $startTime and n.To = $endTime \n")
            .append("RETURN n");

        Map<String, Object> params = new HashMap<>();
            
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        Result newIndexNodeResult = db.execute(newIndexNodeBuilder.toString(), params);
        
        
        Node newIndexNode;
        
        if (!newIndexNodeResult.hasNext()) {
            StringBuilder createNewIndexNodeStatement = new StringBuilder()
            .append("CREATE (n:INDEX:META:").append(relationship).append("{From: $startTime, To: $endTime})\n")
            .append("RETURN n");
            newIndexNodeResult = db.execute(createNewIndexNodeStatement.toString(), params);
        }
        
        
        newIndexNode = (Node) newIndexNodeResult.stream().findFirst().get().get("n");
        
        log.info("got new index node");
        
        StringBuilder objectIdsQueryBuilder = new StringBuilder();
        
        objectIdsQueryBuilder.append("MATCH (o:Object)")
        .append("\nWHERE o.id >= ").append(startId).append(" AND o.id < ").append(endId)
        .append("\nRETURN o.id as objectId ORDER BY objectId");
        
        Result objectIdsResult = db.execute(objectIdsQueryBuilder.toString());
        
        log.info("got object ids");
        // metaNodeTransaction.success();

        objectIdsResult.stream().forEach(objectId -> {
            log.info("objectId: " + objectId);
            // Transaction tx = db.beginTx();
            Map<String, Object> cPathsParams = new HashMap<>();
            cPathsParams.put("objectId", objectId.get("objectId"));
            cPathsParams.put("relationship", relationship);
            
            // Get ids at distance 2
            // StringBuilder idsAtLQueryBuilder = new StringBuilder()
            //     .append("MATCH (u1:Object)-[:").append(relationship).append("*2]->(u2:Object)\n")
            //     .append("WHERE u1.id = $objectId\nRETURN DISTINCT u2.id as otherId ORDER BY otherId");

            // Result otherIdsResult = db.execute(idsAtLQueryBuilder.toString(), cPathsParams);
            
                StringBuilder cPathsQueryBuilder = new StringBuilder()
                    .append("MATCH (u1:Object)\n")
                    .append("WHERE u1.id = $objectId\n")
                    .append("CALL coexisting.coTemporalPaths(u1,null, 2, 2,{edgesLabel: $relationship, between: '").append(startTime).append("—").append(endTime).append("',").append("direction:'outgoing'})\n")
                        .append("YIELD path as internal_p0, interval as internal_i0\n")
                    .append("WITH {path: internal_p0, interval: internal_i0} as paths\n")
                    .append("RETURN paths");


                Result cPathsResults = db.execute(cPathsQueryBuilder.toString(), cPathsParams);

                if (!cPathsResults.hasNext()){
                    return;
                }
                else {
                    // log.info("results for id " + objectId.get("objectId") + " - " + otherId + " length: " + l);
                    cPathsResults.stream().forEach(pathsRecord -> {
                        // log.info("getting paths and such");
                        Map<String, Object> paths = (Map<String, Object>) pathsRecord.get("paths");
                        ArrayList<Map<String, Object>> path = (ArrayList<Map<String, Object>>) paths.get("path");
                        ArrayList<String> intervals = (ArrayList<String>) paths.get("interval");

                        /**
                         * path is an array of custom nodes
                         * [
                         *      {
                         *          "_id": Long,
                         *          "title": String,
                         *          "attributes": HashMap<String, Object>
                         *      }
                         * ]
                         */

                        // log.info("got paths and such");

                        // Get info for new index node
                        // id used to be _id, changed in order to have nonnative ID numbers so as to
                        // make it possible to make index ids always match ids of objetcs in db
                        long sourceId = (Long) path.get(0).get("id");
                        long intermediate = (Long) path.get(1).get("id");
                        long destinationId = (Long) path.get(2).get("id");

                        // log.info("Interval count: " + intervals.size());

                        for (String interval : intervals) {
                            String[] pathInterval = interval.split(" — ");
                            String pathFromDate = pathInterval[0];
                            String pathToDate = pathInterval[1];
    
                            // log.info("got parameters for node to be created");
    
                            // Check if index node already exists for this info
    
                            Map<String, Object> createdIndexNodeParams = new HashMap<>();
                            createdIndexNodeParams.put("sourceId", sourceId);
                            createdIndexNodeParams.put("destinationId", destinationId);
                            createdIndexNodeParams.put("pathLength", 2);
                            createdIndexNodeParams.put("intermediate", intermediate);
                            createdIndexNodeParams.put("pathFromDate", pathFromDate);
                            createdIndexNodeParams.put("pathToDate", pathToDate);
    
                            StringBuilder checkCreatedIndexNodeQuery = new StringBuilder() //This is only for better legibility
                                .append("MATCH (n:INDEX:").append(relationship).append("{Source: $sourceId,")
                                .append("Destination: $destinationId,")
                                .append("Length: $pathLength,")
                                .append("Intermediate: $intermediate,")
                                .append("From: $pathFromDate,")
                                .append("To: $pathToDate })\n")
                                .append("RETURN n");
    
                            // log.info("check created Node Query executing...");
                            
                            Result createdIndexNodeResult = db.execute(checkCreatedIndexNodeQuery.toString(), createdIndexNodeParams);
                            

                            if (!createdIndexNodeResult.hasNext()) {
                                // log.info("created Node Query does not exist, executing create query...");
                                StringBuilder createIndexNodeQuery = new StringBuilder()
                                    .append("CREATE (n:INDEX:").append(relationship).append("{Source: $sourceId,")
                                    .append("Destination: $destinationId,")
                                    .append("Length: $pathLength,")
                                    .append("Intermediate: $intermediate,")
                                    .append("From: $pathFromDate,")
                                    .append("To: $pathToDate })\n")
                                    .append("RETURN n");
                                createdIndexNodeResult = db.execute(createIndexNodeQuery.toString(), createdIndexNodeParams);
                            }
                        }
                    });
                }
            // tx.success();

        });
        // Index nodes created, now join them with edges
}

    @Procedure(value="graphindex.connectIndex", mode=Mode.WRITE)
    @Description("Connects index nodes via concat relationship")
    public void connectGraphIndex(
        @Name("relationship") String relationship,
        @Name("startTime") String startTime,
        @Name("endTime") String endTime,
        @Name("r1LowId") Long r1LowId,
        @Name("r1HighId") Long r1HighId,
        @Name("r2LowId") Long r2LowId,
        @Name("r2HighId") Long r2HighId
    ){
        // First, match relevant index nodes by date


        StringBuilder indexNodesQuery = new StringBuilder();
        indexNodesQuery
            
            // .append("WITH apoc.date.format(timestamp(), 'ms', \"YYYY-MM-dd'T'HH:ss\") as nowString\n")
            // .append("WITH datetime(replace('").append(startTime).append("', ' ', 'T')) as searchFrom, datetime(replace(replace('").append(endTime).append("', 'Now', nowString), ' ', 'T')) as searchTo, nowString\n")
            // .append("MATCH (i1:INDEX:Friend) MATCH (i2:INDEX:Friend)\n")
            // .append("WHERE ID(i1) >= ").append(r1LowId).append(" AND ID (i1) < ").append(r1HighId).append(" AND ID(i2) >= ").append(r2LowId).append(" AND ID(i2) < ").append(r2HighId).append(" AND i1.Destination = i2.Source\n")
            // .append("WITH i1, i2, datetime(replace(i1.From, ' ', 'T')) as i1From, datetime(replace(replace(i1.To, 'Now', nowString), ' ', 'T')) as i1To, datetime(replace(i2.From, ' ', 'T')) as i2From, datetime(replace(replace(i2.To, 'Now', nowString), ' ', 'T')) as i2To, searchFrom, searchTo\n")
            // .append("match (i1) match (i2) where  \n")
            // .append("(i1From >= searchFrom and i1From <= searchTo\n")
            // .append("or i1To >= searchFrom and i1To <= searchTo)\n")
            // .append("AND (i2From >= searchFrom and i2From <= searchTo\n")
            // .append("or i2To >= searchFrom and i2To <= searchTo)\n")
            // .append("RETURN i1, i2\n");

            // .append("WITH apoc.date.format(timestamp(), 'ms', \"YYYY-MM-dd'T'HH:ss\") as nowString\n")
            // .append("WITH datetime(replace('").append(startTime).append("', ' ', 'T')) as searchFrom, datetime(replace(replace('").append(endTime).append("', 'Now', nowString), ' ', 'T')) as searchTo, nowString\n")
            // .append("MATCH (i1:INDEX:Friend) MATCH (i2:INDEX:Friend)\n")
            // .append("WHERE ID(i1) >= ").append(r1LowId).append(" AND ID (i1) < ").append(r1HighId).append(" AND ID(i2) >= ").append(r2LowId).append(" AND ID(i2) < ").append(r2HighId).append("\n")
            // .append(" AND (datetime(replace(i1.From, ' ', 'T')) >= searchFrom and datetime(replace(i1.From, ' ', 'T')) <= searchTo\n")
            // .append("or datetime(replace(replace(i1.To, 'Now', nowString), ' ', 'T')) >= searchFrom and datetime(replace(replace(i1.To, 'Now', nowString), ' ', 'T')) <= searchTo)\n")
            // .append("AND (datetime(replace(i2.From, ' ', 'T')) >= searchFrom and datetime(replace(i2.From, ' ', 'T')) <= searchTo\n")
            // .append("or datetime(replace(replace(i2.To, 'Now', nowString), ' ', 'T')) >= searchFrom and datetime(replace(replace(i2.To, 'Now', nowString), ' ', 'T')) <= searchTo)\n")
            // .append("AND i1.Destination = i2.Source\n")
            // .append("RETURN i1, i2\n");

            .append("MATCH (i1:INDEX:").append(relationship).append(") ").append("MATCH (i2:INDEX:").append(relationship).append(")\n")
            .append("WHERE ID(i1) >= \n").append(r1LowId).append(" AND ID (i1) < ").append(r1HighId).append(" AND ID(i2) >= ").append(r2LowId).append(" AND ID(i2) < ").append(r2HighId)
            .append("\n    AND temporal.intersectsIntervalSplit('").append(startTime).append("', '").append(endTime).append("', i1.From, i1.To)\n")
            .append("    AND temporal.intersectsIntervalSplit('").append(startTime).append("', '").append(endTime).append("', i2.From, i2.To)\n")
            .append("    AND i1.Destination = i2.Source\n")
            .append("RETURN i1, i2");

        log.info("Executing pairs query " + indexNodesQuery.toString());

        Result indexNodes = db.execute(indexNodesQuery.toString());

        indexNodes.stream().forEach(row -> {
            Node i1 = (Node) row.get("i1");
            Node i2 = (Node) row.get("i2");

            Interval i1Period = IntervalParser.fromStringLimits((String) i1.getProperty("From"),(String) i1.getProperty("To"));
            Interval i2Period = IntervalParser.fromStringLimits((String) i2.getProperty("From"),(String) i2.getProperty("To"));
            Optional<Interval> intersection = i1Period.intersection(i2Period);
            if (!intersection.isPresent()) return;
            List<Long> pathIds1 = Arrays.asList((Long) i1.getProperty("Source"), ((Long) i1.getProperty("Intermediate")));
            List<Long> pathIds2 = Arrays.asList((Long) i2.getProperty("Source"), ((Long) i2.getProperty("Intermediate")), ((Long) i2.getProperty("Destination")));
            if (!Collections.disjoint(pathIds1, pathIds2)) return;
            // log.info("Creating rel for " + i1.getId() + " " + i2.getId());
            Relationship concatEdge = i1.createRelationshipTo(i2, RelationshipType.withName("concat"));
            concatEdge.setProperty("interval", intersection.get().toString());
        });
    }
}
