package ar.edu.itba.grammar.struct;

public class DeleteQuery {
    
    private boolean isPresent;

    private String relationshipObjectName;
    
    private String objectDefinition1;
    private String objectDefinition2;
    private String relationshipType;
    private String relationshipObjectName2;

    private IndexType indexType;

    public String getRelationshipObjectName() {
        return relationshipObjectName;
    }
    public IndexType getIndexType() {
        return indexType;
    }
    public void setIndexType(IndexType indexType) {
        this.indexType = indexType;
    }
    public boolean isPresent() {
        return isPresent;
    }
    public void setPresent(boolean isPresent) {
        this.isPresent = isPresent;
    }
    public String getRelationshipObjectName2() {
        return relationshipObjectName2;
    }
    public void setRelationshipObjectName2(String relationshipObjectName2) {
        this.relationshipObjectName2 = relationshipObjectName2;
    }
    public String getRelationshipType() {
        return relationshipType;
    }
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
    public String getObjectDefinition2() {
        return objectDefinition2;
    }
    public void setObjectDefinition2(String objectDefinition2) {
        this.objectDefinition2 = objectDefinition2;
    }
    public String getObjectDefinition1() {
        return objectDefinition1;
    }
    public void setObjectDefinition1(String objectDefinition1) {
        this.objectDefinition1 = objectDefinition1;
    }
    public void setRelationshipObjectName(String relationshipObjectName) {
        this.relationshipObjectName = relationshipObjectName;
    }
}
