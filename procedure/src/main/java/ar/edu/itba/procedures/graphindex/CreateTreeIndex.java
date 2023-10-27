package ar.edu.itba.procedures.graphindex;

import org.neo4j.logging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class CreateTreeIndex {
    
    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;


    @Procedure(value="graphindex.createTreeIndex", mode=Mode.WRITE)
    @Description("Create index for paths of max length L for relationship r on given interval")
    public void createTreeIndex(
        @Name("relationship") String relationship,
        @Name("maxLength") Long maxLength,
        @Name("startTime") String startTime,
        @Name("endTime") String endTime,
        @Name("startId") Long startId,
        @Name("endId") Long endId
    ){
        // Check if index root already exists
        Transaction metaNodeTransaction = db.beginTx();
        Result indexRootResult = db.execute("MATCH (indexRoot:INDEX:META:ROOT)\n" +
        "RETURN indexRoot");
        
        Node indexRootNode;

        // If it does not exist, create new root index node
        if (!indexRootResult.hasNext())
            indexRootNode = db.createNode(Label.label("INDEX"), Label.label("META"), Label.label("ROOT"));
        else
            indexRootNode = (Node) indexRootResult.stream().findFirst().get().get("indexRoot");

        StringBuilder newIndexNodeBuilder = new StringBuilder()
            .append("MATCH (n:INDEX:META:").append(relationship).append(")\n")
            .append("WHERE n.MaxLength = $maxLength and n.From = $startTime and n.To = $endTime \n")
            .append("RETURN n");

        Map<String, Object> params = new HashMap<>();
            
        params.put("maxLength", maxLength);
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        Result newIndexNodeResult = db.execute(newIndexNodeBuilder.toString(), params);
        
        metaNodeTransaction.success();

        Node newIndexNode;

        if (!newIndexNodeResult.hasNext()) {
            StringBuilder createNewIndexNodeStatement = new StringBuilder()
                .append("CREATE (n:INDEX:META:").append(relationship).append("{MaxLength: $maxLength, From: $startTime, To: $endTime})\n")
                .append("WITH n MATCH(m:INDEX:META:ROOT)\n")
                .append("CREATE (n)<-[rel:meta]-(m)\n")
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
                            Node createdIndexNode = (Node) createdIndexNodeResult.stream().findFirst().get().get("n");
                            createdIndexNode.createRelationshipTo(newIndexNode, RelationshipType.withName("type"));
                        }
                    });

                }
            // tx.success();

        });
        // Index nodes created, now join them with edges
    }
}