package com.erdiagram.app.model;

/**
 * Represents a column in a database table.
 */
public class Attribute {
    private String name;
    private String dataType;
    private boolean isPrimaryKey;
    private boolean isForeignKey;
    private String referencedTable;
    private String referencedColumn;
    private boolean isNullable;
    
    public Attribute(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
        this.isPrimaryKey = false;
        this.isForeignKey = false;
        this.isNullable = true;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
    
    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }
    
    public boolean isForeignKey() {
        return isForeignKey;
    }
    
    public void setForeignKey(boolean foreignKey) {
        isForeignKey = foreignKey;
    }
    
    public String getReferencedTable() {
        return referencedTable;
    }
    
    public void setReferencedTable(String referencedTable) {
        this.referencedTable = referencedTable;
    }
    
    public String getReferencedColumn() {
        return referencedColumn;
    }
    
    public void setReferencedColumn(String referencedColumn) {
        this.referencedColumn = referencedColumn;
    }
    
    public boolean isNullable() {
        return isNullable;
    }
    
    public void setNullable(boolean nullable) {
        isNullable = nullable;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (").append(dataType).append(")");
        
        if (isPrimaryKey) {
            sb.append(" PK");
        }
        
        if (isForeignKey) {
            sb.append(" FK");
        }
        
        if (!isNullable) {
            sb.append(" NOT NULL");
        }
        
        return sb.toString();
    }
} 