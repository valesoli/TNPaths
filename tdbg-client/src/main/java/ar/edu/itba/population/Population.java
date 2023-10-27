package ar.edu.itba.population;

import ar.edu.itba.connector.Connector;
import ar.edu.itba.population.models.*;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.edu.itba.util.interval.Interval;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class Population {

    private final Logger logger = LoggerFactory.getLogger(Population.class);

    private final DataFactory dataFactory = new DataFactory();

    private final TransactionHelper helper = new TransactionHelper();

    private List<AttributeNode> attributeNodes = new ArrayList<>();
    private List<ValueNode> valueNodes = new ArrayList<>();
    private List<Relationship> objectToAttributeRelationships = new ArrayList<>();
    private List<Relationship> attributeToValueRelationships = new ArrayList<>();

    private final IdGenerator idGenerator = new IdGenerator(1);

    public void create() {

        helper.emptyDatabase();
        logger.info("Populating database..");

        final long startTime = System.nanoTime();

        generateNodesAndRelationships();

        final long endTime = System.nanoTime();

        long elapsedMinutes = TimeUnit.NANOSECONDS.toMinutes(endTime - startTime);
        logger.info("Population completed in {}m {}s", elapsedMinutes,
                TimeUnit.NANOSECONDS.toSeconds(endTime - startTime) - elapsedMinutes * 60);

        Connector.getInstance().close();
    }

    protected abstract void generateNodesAndRelationships();

    @Deprecated
    protected int createObject(String title, String timeInterval){

        final int objectId = helper.createObjectNode(title, timeInterval);

        final int nameId = helper.createAttributeNode("Name", timeInterval);

        final int valueId = helper.createValueNode(timeInterval, dataFactory.getName(title));

        helper.createObjectAttributeEdge(objectId, nameId);

        helper.createAttributeValueEdge(nameId, valueId);

        return objectId;
    }

    protected void createAttribute(String name, ObjectNode objectNode, Interval interval) {
        AttributeNode nameAttributeNode = new AttributeNode(getNextId(), interval, name);
        addAttributeNode(nameAttributeNode);

        ValueNode nameValueNode = new ValueNode(getNextId(), interval, getDataFactory().getName(objectNode.getTitle()));
        addValueNode(nameValueNode);

        Relationship edge1 = new Relationship(RelationshipType.EDGE, objectNode, nameAttributeNode);
        Relationship edge2 = new Relationship(RelationshipType.EDGE, nameAttributeNode, nameValueNode);
        objectToAttributeRelationships.add(edge1);
        attributeToValueRelationships.add(edge2);
    }

    protected int getNextId() {
        return idGenerator.getNextId();
    }

    public void unwindAttributesAndValues() {
        logger.debug("Creating attribute nodes...");
        unwindNodes(attributeNodes, AttributeNode.LABEL);
        logger.debug("Creating value nodes...");
        unwindNodes(valueNodes, ValueNode.LABEL);
        getHelper().createIndexes();
        logger.debug("Creating edges...");
        unwindRelationships(objectToAttributeRelationships, RelationshipType.EDGE);
        unwindRelationships(attributeToValueRelationships, RelationshipType.EDGE);
    }

    protected void unwindNodes(List<? extends Node> nodes, String label) {
        int BATCH_SIZE = 3000;
        List<List<Map<String, Object>>> batches = Lists.partition(
                nodes.stream().map(Node::toMap).collect(Collectors.toList()), BATCH_SIZE);
        batches.forEach(batch -> getHelper().unwindNodes(batch, label));
    }

    protected void unwindRelationships(List<Relationship> relationships, RelationshipType type) {
        
        // Not enough heap for collect
        // int BATCH_SIZE = 10000;
        // List<List<Map<String, Object>>> batches = Lists.partition(
        //         relationships.stream().map(Relationship::toMap).collect(Collectors.toList()), BATCH_SIZE);
        // batches.forEach(batch -> getHelper().unwindRelationships(batch, type));

        List<Map<String, Object>> batch = new ArrayList<>();
        for (Relationship relationship : relationships) {
            batch.add(relationship.toMap());
            if (batch.size() == 10000) {
                getHelper().unwindRelationships(batch, type);
                batch.clear();
            }
        }
        if (!batch.isEmpty())
            getHelper().unwindRelationships(batch, type);
    }

    public void addAttributeNode(AttributeNode attributeNode) {
        attributeNodes.add(attributeNode);
    }

    public void addValueNode(ValueNode valueNode) {
        valueNodes.add(valueNode);
    }

    protected DataFactory getDataFactory(){
        return dataFactory;
    }

    protected TransactionHelper getHelper() {
        return helper;
    }
}
