package ar.edu.itba.procedures.graphindex;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class DeleteIndex {
    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure(value="graphindex.deleteTreeIndex", mode=Mode.WRITE)
    @Description("Deletes specified tree index")
    public void deleteTreeIndex(
        @Name("relationship") String relationship,
        @Name("startTime") String startTime,
        @Name("endTime") String endTime
    ){
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH (i:INDEX) - [r:type] -> (i2:INDEX:").append(relationship).append(")\n")
        .append("WHERE i2.From = '").append(startTime).append("' AND i2.To = '").append(endTime).append("'\n")
        .append("DELETE i, r");
        log.info("Deleting Tree Index\n" + queryBuilder.toString());
        db.execute(queryBuilder.toString());
        queryBuilder = new StringBuilder().append("MATCH (i2:INDEX:META:").append(relationship).append(")<-[r:meta]-(i3:INDEX:ROOT)\n")
        .append("WHERE i2.From = '").append(startTime).append("' AND i2.To = '").append(endTime).append("'\n")
        .append("DELETE r, i2");
        log.info("Deleting Meta Node\n" + queryBuilder.toString());
        db.execute(queryBuilder.toString());
    }

    @Procedure(value="graphindex.boundedDeleteTreeIndex", mode=Mode.WRITE)
    @Description("Deletes specified tree index")
    public void boundedDeleteTreeIndex(
        @Name("relationship") String relationship,
        @Name("startTime") String startTime,
        @Name("endTime") String endTime,
        @Name("startId") Long startId,
        @Name("endId") Long endId
    ){
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH (i:INDEX) - [r:type] -> (i2:INDEX:").append(relationship).append(")\n")
        .append("WHERE i2.From = '").append(startTime).append("' AND i2.To = '").append(endTime).append("'\n")
        .append("AND ID(i) >= ").append(startId).append(" AND ID(i) < ").append(endId).append("\n")
        .append("DELETE i, r");
        log.info("Deleting Tree Index With Bounds\n" + queryBuilder.toString());
        db.execute(queryBuilder.toString());
        db.execute("MATCH (i2:INDEX:META:" + relationship +
            ")\nWHERE i2.From = " + startTime.toString() +
            " AND i2.To = " + endTime.toString() +
            "\nDELETE i2");
    }

    @Procedure(value="graphindex.deleteGraphIndex", mode=Mode.WRITE)
    public void deleteGraphIndex(@Name("relationship") String relationship) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH (i:INDEX:").append(relationship).append(")-[r:concat]->(i2:INDEX:").append(relationship).append(") DELETE i, r, i2");
        log.info("Deleting Graph Index\n" + queryBuilder.toString());
        db.execute(queryBuilder.toString());
        db.execute("MATCH (i:INDEX:" + relationship + ") DELETE i");
    }

    @Procedure(value="graphindex.deleteAllIndices", mode=Mode.WRITE)
    public void deleteAllIndices() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH (i:INDEX)-[r]->(i2:INDEX) DELETE i, r, i2;");
        log.info("Deleting All Indices\n" + queryBuilder.toString());
        db.execute(queryBuilder.toString());
        db.execute("MATCH(i:INDEX) DELETE i;");
    }
    
    @Procedure(value="graphindex.boundedDeleteAllIndices", mode=Mode.WRITE)
    public void boundedDeleteAllIndices(
        @Name("startId") Long startId,
        @Name("endId") Long endId) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("MATCH (i:INDEX)-[r]->(i2:INDEX)\n")
        .append("AND ID(i) >= ").append(startId).append(" AND ID(i) < ").append(endId).append("\n")
        .append(" DELETE i, r, i2");
        log.info("Deleting All Indices With Bounds\n" + queryBuilder.toString());
        db.execute(queryBuilder.toString());
    }
}
