package com.erdiagram.app.model;

/**
 * Represents a relationship between two entities in an ER diagram.
 */
public class Relationship {
    public enum RelationshipType {
        ONE_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_ONE,
        MANY_TO_MANY
    }

    private Entity sourceEntity;
    private Entity targetEntity;
    private RelationshipType type;
    private String name;
    private Attribute sourceAttribute;
    private Attribute targetAttribute;
    
    public Relationship(Entity sourceEntity, Entity targetEntity, RelationshipType type) {
        this.sourceEntity = sourceEntity;
        this.targetEntity = targetEntity;
        this.type = type;
        this.name = generateDefaultName();
    }
    
    private String generateDefaultName() {
        return sourceEntity.getName() + "_" + targetEntity.getName();
    }
    
    public Entity getSourceEntity() {
        return sourceEntity;
    }
    
    public Entity getTargetEntity() {
        return targetEntity;
    }
    
    public RelationshipType getType() {
        return type;
    }
    
    public void setType(RelationshipType type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Attribute getSourceAttribute() {
        return sourceAttribute;
    }
    
    public void setSourceAttribute(Attribute sourceAttribute) {
        this.sourceAttribute = sourceAttribute;
    }
    
    public Attribute getTargetAttribute() {
        return targetAttribute;
    }
    
    public void setTargetAttribute(Attribute targetAttribute) {
        this.targetAttribute = targetAttribute;
    }
    
    public String getStyleClass() {
        switch (type) {
            case ONE_TO_ONE:
                return "one-to-one";
            case ONE_TO_MANY:
            case MANY_TO_ONE:
                return "one-to-many";
            case MANY_TO_MANY:
                return "many-to-many";
            default:
                return "relationship-line";
        }
    }
    
    @Override
    public String toString() {
        return sourceEntity.getName() + " " + formatRelationshipType() + " " + targetEntity.getName();
    }
    
    private String formatRelationshipType() {
        switch (type) {
            case ONE_TO_ONE:
                return "1:1";
            case ONE_TO_MANY:
                return "1:N";
            case MANY_TO_ONE:
                return "N:1";
            case MANY_TO_MANY:
                return "N:M";
            default:
                return "";
        }
    }
} 