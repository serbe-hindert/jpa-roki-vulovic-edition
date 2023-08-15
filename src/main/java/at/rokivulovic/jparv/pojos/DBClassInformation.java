package at.rokivulovic.jparv.pojos;

import java.util.List;

public class DBClassInformation {
    private String tableName;
    private List<DBAttributeInformation> attributes;

    public DBClassInformation(String tableName, List<DBAttributeInformation> attributes) {
        this.tableName = tableName;
        this.attributes = attributes;
    }

    public DBClassInformation() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<DBAttributeInformation> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DBAttributeInformation> attributes) {
        this.attributes = attributes;
    }
}
