package ar.edu.itba.population;

import ar.edu.itba.connector.Connector;
import ar.edu.itba.population.models.AttributeNode;
import ar.edu.itba.population.models.ObjectNode;
import ar.edu.itba.population.models.RelationshipType;
import ar.edu.itba.population.models.ValueNode;
import ar.edu.itba.util.Utils;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.neo4j.driver.v1.Values.parameters;

public class TransactionHelper {

    private final Logger logger = LoggerFactory.getLogger(TransactionHelper.class);

    private Session getSession() {
        return Connector.getInstance().getSession();
    }

    public void emptyDatabase() {
        getSession().writeTransaction((tx -> tx.run("MATCH (n) DETACH DELETE n;")));
        logger.info("Database emptied");
    }

    public int createValueNode(String timeInterval, String value) {
        return getSession().writeTransaction(tx -> tx.run(
                "CREATE (v:Value { value: $name, interval: [$timeInterval] })\n" +
                        "RETURN id(v)",
                parameters("name", value,
                        "timeInterval", timeInterval))).single().get(0).asInt();
    }

    public void createObjectAttributeEdge(int objectId, int attributeId) {
        getSession().writeTransaction(tx -> tx.run("MATCH (o:Object),(a:Attribute)\n" +
                        "WHERE id(o) = $objectId and id(a) = $attributeId\n" +
                        "CREATE (o)-[:Edge]->(a)",
                parameters("objectId", objectId, "attributeId", attributeId)));
    }

    public void createAttributeValueEdge(int attributeId, int valueId) {
        getSession().writeTransaction(tx -> tx.run("MATCH (a:Attribute),(v:Value)\n" +
                        "WHERE id(a) = $attributeId and id(v) = $valueId\n" +
                        "CREATE (a)-[:Edge]->(v)",
                parameters("attributeId", attributeId, "valueId", valueId)));
    }

    public int createObjectNode(String title, String timeInterval) {
        return getSession().writeTransaction(tx -> tx.run(
                "CREATE (o:Object { title: $title, interval: [$timeInterval]} )\n" +
                        "RETURN id(o)",
                parameters("title", title, "timeInterval", timeInterval)).single().get(0).asInt());
    }

    public int createAttributeNode(String attributeTitle, String timeInterval) {
        return getSession().writeTransaction(tx -> tx.run(
                "CREATE (a:Attribute { title: $title, interval: [$timeInterval] })\n" +
                        "RETURN id(a)",
                parameters("title", attributeTitle, "timeInterval", timeInterval))).single().get(0).asInt();
    }

    /**
     * Creates an edge between two nodes
     * @param firstNodeId id of the first node
     * @param secondNodeId id of the second node
     * @param edge edge name
     * @param intervalList intervals in which the relation is valid
     */
    public void createEdge(int firstNodeId, int secondNodeId, String edge, List<TimeInterval> intervalList) {
        final List<String> intervals = intervalList.stream().map(TimeInterval::toString).collect(Collectors.toList());
        getSession().writeTransaction(tx -> tx.run("MATCH (p:Object),(f:Object)\n" +
                        "WHERE id(p) = $firstNode and id(f) = $secondNode\n" +
                        "CREATE (p)-[:" + edge + " { interval: $intervals}]->(f)",
                parameters("firstNode", firstNodeId, "secondNode", secondNodeId, "intervals", intervals)));
    }

    public void createFlightEdge(int firstNodeId, int secondNodeId, List<DateTimeInterval> intervalList, List<String> flightNumbers) {
        final String edge = "Flight";
        final List<String> intervals = intervalList.stream().map(DateTimeInterval::toString).collect(Collectors.toList());
        getSession().writeTransaction(tx -> tx.run("MATCH (p:Object),(f:Object)\n" +
                        "WHERE id(p) = $firstNode and id(f) = $secondNode\n" +
                        "CREATE (p)-[:" + edge + " { interval: $interval, flightNumber: $flightNumber }]->(f)",
                parameters("firstNode", firstNodeId, "secondNode", secondNodeId, "interval", intervals,
                        "flightNumber", flightNumbers)));
    }

    public List<Record> randomAirports(Integer airportId, int maxAmount) {
        return getSession().writeTransaction(tx -> tx.run("MATCH (o:Object), (p:Object), (c1:Object), (c2:Object)\n" +
                        " WHERE o.title = 'Airport' and id(o) <> $id\n" +
                        " AND id(p) = $id\n" +
                        " AND c1.title='City' AND c2.title='City'" +
                        " AND (o)-[:LocatedAt]-(c1) AND (p)-[:LocatedAt]-(c2)" +
                        " AND id(c1) <> id(c2)" +
                        " AND NOT (o)-[:Flight]-(p) " +
                        " RETURN id(o), rand() as r ORDER BY r LIMIT $limit;",
                parameters("id", airportId,
                        "limit", Utils.randomInteger(1, maxAmount)))).list();
    }

    public void unwindNodes(List<Map<String, Object>> list, String title) {
        String query = "UNWIND $props AS properties " +
                "CREATE (n:" + title + ") " +
                "SET n = properties";
        getSession().writeTransaction(tx -> tx.run(query, parameters("props", list)));
    }

    public void unwindRelationships(List<Map<String, Object>> list, RelationshipType type) {
        Map<String, Object> relationship = list.get(0);
        String fromType = (String) relationship.get("fromLabel");
        String toType = (String) relationship.get("toLabel");
        String query = "UNWIND $props AS relationship " +
                "MATCH (a:" + fromType +"), (b:" + toType + ") " +
                "WHERE a.id = relationship.from AND b.id = relationship.to " +
                "CREATE (a)-[e:" + type.toString() + "]->(b) " +
                "SET e.interval = relationship.interval";
        getSession().writeTransaction(tx -> tx.run(query, parameters("props", list)));
    }

    public void createIndexes() {
        logger.info("Creating id indexes");
        createIndex(ObjectNode.LABEL, "id");
        createIndex(AttributeNode.LABEL, "id");
        createIndex(ValueNode.LABEL, "id");
        logger.info("Indexes created");
    }

    private void createIndex(String label, String property) {
        getSession().writeTransaction((tx -> tx.run("CREATE INDEX ON :" + label + "(" + property +")")));
    }
}
