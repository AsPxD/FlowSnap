package com.erdiagram.app.model;

/**
 * Represents a relationship between UML classes.
 */
public class UMLRelationship {
    // Relationship types
    public static final String ASSOCIATION = "association";
    public static final String INHERITANCE = "inheritance";
    public static final String IMPLEMENTATION = "implementation";
    public static final String DEPENDENCY = "dependency";
    public static final String AGGREGATION = "aggregation";
    public static final String COMPOSITION = "composition";
    
    private UMLClass source;
    private UMLClass target;
    private String type;
    private String sourceLabel; // multiplicity or role
    private String targetLabel; // multiplicity or role
    
    public UMLRelationship(UMLClass source, UMLClass target, String type) {
        this.source = source;
        this.target = target;
        this.type = type;
    }
    
    public UMLRelationship(UMLClass source, UMLClass target, String type, String sourceLabel, String targetLabel) {
        this.source = source;
        this.target = target;
        this.type = type;
        this.sourceLabel = sourceLabel;
        this.targetLabel = targetLabel;
    }
    
    public UMLClass getSource() {
        return source;
    }
    
    public void setSource(UMLClass source) {
        this.source = source;
    }
    
    public UMLClass getTarget() {
        return target;
    }
    
    public void setTarget(UMLClass target) {
        this.target = target;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSourceLabel() {
        return sourceLabel;
    }
    
    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }
    
    public String getTargetLabel() {
        return targetLabel;
    }
    
    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }
    
    @Override
    public String toString() {
        return source.getName() + " --[" + type + "]--> " + target.getName();
    }
} 