package ar.edu.itba.grammar.struct;

public class CreateOrUpdateQuery {
    
    private boolean isPresent;

    private String objectVariable1;
    private String objectVariable2;

    private String objectDef1;
    private String objectDef2;

    private String relationshipObjectName;
    private String relationshipType;

    private IndexType indexType;

    public String getObjectVariable1() {
        return objectVariable1;
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
    public String getRelationshipType() {
        return relationshipType;
    }
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
    public String getRelationshipObjectName() {
        return relationshipObjectName;
    }
    public void setRelationshipObjectName(String relationshipObjectName) {
        this.relationshipObjectName = relationshipObjectName;
    }
    public String getObjectDef2() {
        return objectDef2;
    }
    public void setObjectDef2(String objectDef2) {
        this.objectDef2 = objectDef2;
    }
    public String getObjectDef1() {
        return objectDef1;
    }
    public void setObjectDef1(String objectDef1) {
        this.objectDef1 = objectDef1;
    }
    public String getObjectVariable2() {
        return objectVariable2;
    }
    public void setObjectVariable2(String objectVariable2) {
        this.objectVariable2 = objectVariable2;
    }
    public void setObjectVariable1(String objectVariable1) {
        this.objectVariable1 = objectVariable1;
    }


}
