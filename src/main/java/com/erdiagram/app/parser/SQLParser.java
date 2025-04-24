package com.erdiagram.app.parser;

import com.erdiagram.app.model.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ForeignKeyIndex;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.NamedConstraint;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses SQL statements to extract table structure and relationships for ER diagrams.
 */
public class SQLParser {
    private ERDiagram erDiagram;
    
    public SQLParser() {
        erDiagram = new ERDiagram("ER Diagram");
    }
    
    /**
     * Parses SQL CREATE TABLE statements and builds an ERDiagram model.
     * @param sqlText The SQL statements to parse
     * @return The constructed ERDiagram
     * @throws JSQLParserException If SQL parsing fails
     */
    public ERDiagram parseSQL(String sqlText) throws JSQLParserException {
        erDiagram = new ERDiagram("ER Diagram");
        
        // Split the input into separate SQL statements
        String[] statements = sqlText.split(";");
        
        // First pass: Extract all entities
        Map<String, List<ForeignKeyInfo>> foreignKeys = new HashMap<>();
        
        for (String statementStr : statements) {
            if (statementStr.trim().isEmpty()) {
                continue;
            }
            
            try {
                Statement statement = CCJSqlParserUtil.parse(statementStr.trim() + ";");
                
                if (statement instanceof CreateTable) {
                    CreateTable createTable = (CreateTable) statement;
                    processCreateTable(createTable, foreignKeys);
                }
            } catch (JSQLParserException e) {
                // Try to handle some common syntax variations that JSQLParser doesn't support
                if (statementStr.toUpperCase().contains("CREATE TABLE")) {
                    processRawCreateTable(statementStr, foreignKeys);
                }
            }
        }
        
        // Second pass: Create relationships based on foreign keys
        processForeignKeys(foreignKeys);
        
        // Auto-layout the diagram
        erDiagram.autoLayout();
        
        return erDiagram;
    }
    
    private void processCreateTable(CreateTable createTable, Map<String, List<ForeignKeyInfo>> foreignKeys) {
        String tableName = createTable.getTable().getName();
        Entity entity = new Entity(tableName);
        
        // Process column definitions
        List<ColumnDefinition> columnDefinitions = createTable.getColumnDefinitions();
        if (columnDefinitions != null) {
            for (ColumnDefinition columnDef : columnDefinitions) {
                String columnName = columnDef.getColumnName();
                ColDataType dataType = columnDef.getColDataType();
                String dataTypeString = dataType.getDataType();
                
                if (dataType.getArgumentsStringList() != null && !dataType.getArgumentsStringList().isEmpty()) {
                    dataTypeString += "(" + String.join(",", dataType.getArgumentsStringList()) + ")";
                }
                
                Attribute attribute = new Attribute(columnName, dataTypeString);
                
                // Check for NOT NULL constraint
                if (columnDef.getColumnSpecs() != null) {
                    for (String spec : columnDef.getColumnSpecs()) {
                        if (spec.equalsIgnoreCase("NOT NULL")) {
                            attribute.setNullable(false);
                        }
                    }
                }
                
                entity.addAttribute(attribute);
            }
        }
        
        // Process table constraints
        List<Index> indexes = createTable.getIndexes();
        if (indexes != null) {
            for (Index index : indexes) {
                if (index.getType().equalsIgnoreCase("PRIMARY KEY")) {
                    // Mark primary key attributes
                    for (String columnName : getColumnNames(index)) {
                        for (Attribute attr : entity.getAttributes()) {
                            if (attr.getName().equalsIgnoreCase(columnName)) {
                                attr.setPrimaryKey(true);
                                attr.setNullable(false);
                            }
                        }
                    }
                } else if (index instanceof ForeignKeyIndex) {
                    ForeignKeyIndex fkIndex = (ForeignKeyIndex) index;
                    String refTable = fkIndex.getTable().getName();
                    
                    List<String> columns = getColumnNames(fkIndex);
                    List<String> refColumns = new ArrayList<>();
                    
                    if (fkIndex.getReferencedColumnNames() != null) {
                        for (String col : fkIndex.getReferencedColumnNames()) {
                            refColumns.add(col);
                        }
                    }
                    
                    // Create foreign key info for later processing
                    if (!foreignKeys.containsKey(tableName)) {
                        foreignKeys.put(tableName, new ArrayList<>());
                    }
                    
                    for (int i = 0; i < columns.size(); i++) {
                        String column = columns.get(i);
                        String refColumn = (i < refColumns.size()) ? refColumns.get(i) : null;
                        
                        ForeignKeyInfo fkInfo = new ForeignKeyInfo(tableName, column, refTable, refColumn);
                        foreignKeys.get(tableName).add(fkInfo);
                        
                        // Mark the attribute as a foreign key
                        for (Attribute attr : entity.getAttributes()) {
                            if (attr.getName().equalsIgnoreCase(column)) {
                                attr.setForeignKey(true);
                                attr.setReferencedTable(refTable);
                                attr.setReferencedColumn(refColumn);
                            }
                        }
                    }
                }
            }
        }
        
        erDiagram.addEntity(entity);
    }
    
    private void processRawCreateTable(String createTableSql, Map<String, List<ForeignKeyInfo>> foreignKeys) {
        // Extract table name using regex
        Pattern tablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(?:`|\")?([\\w_]+)(?:`|\")?", Pattern.CASE_INSENSITIVE);
        Matcher tableMatcher = tablePattern.matcher(createTableSql);
        
        if (!tableMatcher.find()) {
            return;
        }
        
        String tableName = tableMatcher.group(1);
        Entity entity = new Entity(tableName);
        
        // Extract column definitions
        Pattern columnPattern = Pattern.compile("([\\w_]+)\\s+([\\w\\s()\\d,]+)(?:\\s+(?:NOT\\s+NULL|PRIMARY\\s+KEY))*", 
                Pattern.CASE_INSENSITIVE);
        Matcher columnMatcher = columnPattern.matcher(createTableSql);
        
        while (columnMatcher.find()) {
            String columnName = columnMatcher.group(1);
            String dataType = columnMatcher.group(2);
            
            Attribute attribute = new Attribute(columnName, dataType.trim());
            
            // Check for constraints
            if (columnMatcher.group().toUpperCase().contains("NOT NULL")) {
                attribute.setNullable(false);
            }
            
            if (columnMatcher.group().toUpperCase().contains("PRIMARY KEY")) {
                attribute.setPrimaryKey(true);
                attribute.setNullable(false);
            }
            
            entity.addAttribute(attribute);
        }
        
        // Extract primary key constraints
        Pattern primaryKeyPattern = Pattern.compile("PRIMARY\\s+KEY\\s+\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher primaryKeyMatcher = primaryKeyPattern.matcher(createTableSql);
        
        if (primaryKeyMatcher.find()) {
            String[] primaryKeys = primaryKeyMatcher.group(1).split(",");
            for (String primaryKey : primaryKeys) {
                String pkColumn = primaryKey.trim().replaceAll("[`\"\']", "");
                
                for (Attribute attr : entity.getAttributes()) {
                    if (attr.getName().equalsIgnoreCase(pkColumn)) {
                        attr.setPrimaryKey(true);
                        attr.setNullable(false);
                    }
                }
            }
        }
        
        // Extract foreign key constraints
        Pattern foreignKeyPattern = Pattern.compile(
                "FOREIGN\\s+KEY\\s+\\(([^)]+)\\)\\s+REFERENCES\\s+([\\w_]+)\\s*(?:\\(([^)]+)\\))?", 
                Pattern.CASE_INSENSITIVE);
        Matcher foreignKeyMatcher = foreignKeyPattern.matcher(createTableSql);
        
        while (foreignKeyMatcher.find()) {
            String[] fkColumns = foreignKeyMatcher.group(1).split(",");
            String refTable = foreignKeyMatcher.group(2).trim().replaceAll("[`\"\']", "");
            String[] refColumns = foreignKeyMatcher.group(3) != null ? 
                    foreignKeyMatcher.group(3).split(",") : new String[0];
            
            if (!foreignKeys.containsKey(tableName)) {
                foreignKeys.put(tableName, new ArrayList<>());
            }
            
            for (int i = 0; i < fkColumns.length; i++) {
                String fkColumn = fkColumns[i].trim().replaceAll("[`\"\']", "");
                String refColumn = (i < refColumns.length) ? 
                        refColumns[i].trim().replaceAll("[`\"\']", "") : null;
                
                ForeignKeyInfo fkInfo = new ForeignKeyInfo(tableName, fkColumn, refTable, refColumn);
                foreignKeys.get(tableName).add(fkInfo);
                
                // Mark the attribute as a foreign key
                for (Attribute attr : entity.getAttributes()) {
                    if (attr.getName().equalsIgnoreCase(fkColumn)) {
                        attr.setForeignKey(true);
                        attr.setReferencedTable(refTable);
                        attr.setReferencedColumn(refColumn);
                    }
                }
            }
        }
        
        erDiagram.addEntity(entity);
    }
    
    private void processForeignKeys(Map<String, List<ForeignKeyInfo>> foreignKeys) {
        for (String tableName : foreignKeys.keySet()) {
            Entity sourceEntity = erDiagram.getEntityByName(tableName);
            
            if (sourceEntity == null) {
                continue;
            }
            
            for (ForeignKeyInfo fkInfo : foreignKeys.get(tableName)) {
                Entity targetEntity = erDiagram.getEntityByName(fkInfo.refTable);
                
                if (targetEntity == null) {
                    continue;
                }
                
                // Determine relationship type
                Relationship.RelationshipType relType = determineRelationshipType(
                        sourceEntity, fkInfo.column, targetEntity, fkInfo.refColumn);
                
                Relationship relationship = new Relationship(sourceEntity, targetEntity, relType);
                
                // Find the source and target attributes
                Attribute sourceAttr = findAttributeByName(sourceEntity, fkInfo.column);
                Attribute targetAttr = findAttributeByName(targetEntity, fkInfo.refColumn);
                
                relationship.setSourceAttribute(sourceAttr);
                relationship.setTargetAttribute(targetAttr);
                
                erDiagram.addRelationship(relationship);
            }
        }
    }
    
    private Relationship.RelationshipType determineRelationshipType(
            Entity sourceEntity, String sourceColumn, Entity targetEntity, String targetColumn) {
        
        boolean sourceIsPK = isColumnPrimaryKey(sourceEntity, sourceColumn);
        boolean targetIsPK = isColumnPrimaryKey(targetEntity, targetColumn);
        
        if (sourceIsPK && targetIsPK) {
            return Relationship.RelationshipType.ONE_TO_ONE;
        } else if (targetIsPK) {
            return Relationship.RelationshipType.MANY_TO_ONE;
        } else if (sourceIsPK) {
            return Relationship.RelationshipType.ONE_TO_MANY;
        } else {
            return Relationship.RelationshipType.MANY_TO_MANY;
        }
    }
    
    private boolean isColumnPrimaryKey(Entity entity, String columnName) {
        for (Attribute attr : entity.getAttributes()) {
            if (attr.getName().equalsIgnoreCase(columnName) && attr.isPrimaryKey()) {
                return true;
            }
        }
        return false;
    }
    
    private Attribute findAttributeByName(Entity entity, String columnName) {
        if (columnName == null) {
            return null;
        }
        
        for (Attribute attr : entity.getAttributes()) {
            if (attr.getName().equalsIgnoreCase(columnName)) {
                return attr;
            }
        }
        return null;
    }
    
    private List<String> getColumnNames(Index index) {
        List<String> columns = new ArrayList<>();
        
        if (index instanceof NamedConstraint) {
            NamedConstraint namedConstraint = (NamedConstraint) index;
            for (String columnName : namedConstraint.getColumnsNames()) {
                columns.add(columnName);
            }
        } else if (index.getColumnsNames() != null) {
            for (String columnName : index.getColumnsNames()) {
                columns.add(columnName);
            }
        }
        
        return columns;
    }
    
    /**
     * Helper class to store foreign key information for relationship creation.
     */
    private static class ForeignKeyInfo {
        String sourceTable;
        String column;
        String refTable;
        String refColumn;
        
        public ForeignKeyInfo(String sourceTable, String column, String refTable, String refColumn) {
            this.sourceTable = sourceTable;
            this.column = column;
            this.refTable = refTable;
            this.refColumn = refColumn;
        }
    }
} 