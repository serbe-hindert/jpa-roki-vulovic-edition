package at.rokivulovic.jparv.pojos;

public class DBAttributeInformation {
    private Boolean isPrimaryKey;
    private Boolean isSequence;
    private String attributeName;

    public DBAttributeInformation(Boolean isPrimaryKey, Boolean isSequence, String attributeName) {
        this.isPrimaryKey = isPrimaryKey;
        this.isSequence = isSequence;
        this.attributeName = attributeName;
    }

    public DBAttributeInformation() {
    }

    public Boolean getPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public Boolean getSequence() {
        return isSequence;
    }

    public void setSequence(Boolean sequence) {
        isSequence = sequence;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }
}
